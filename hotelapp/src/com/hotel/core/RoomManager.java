package com.hotel.core;

import com.database.helper.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 统一房间管理器
 * 整合所有房间相关的CRUD操作和查询功能
 */
public class RoomManager {
    
    private static final Logger LOGGER = Logger.getLogger(RoomManager.class.getName());
    private static RoomManager instance;
    
    private RoomManager() {}
    
    public static RoomManager getInstance() {
        if (instance == null) {
            synchronized (RoomManager.class) {
                if (instance == null) {
                    instance = new RoomManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 添加房间
     * @param room 房间信息
     * @return 操作结果
     */
    public OperationResult addRoom(Room room) {
        OperationResult result = new OperationResult();
        
        if (room == null) {
            result.setErrorMessage("房间信息不能为空");
            return result;
        }
        
        if (room.getRNumber() == null || room.getRNumber().trim().isEmpty()) {
            result.setErrorMessage("房间号不能为空");
            return result;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                result.setErrorMessage("无法连接到数据库");
                return result;
            }
            
            // 检查房间是否已存在
            if (isRoomExists(conn, room.getRNumber())) {
                result.setErrorMessage("房间号 " + room.getRNumber() + " 已存在");
                return result;
            }
            
            // 插入房间信息
            String insertSql = "INSERT INTO rooms (rNumber, rType, rPrice) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(insertSql);
            stmt.setString(1, room.getRNumber());
            stmt.setString(2, room.getRType());
            stmt.setString(3, room.getRPrice());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // 初始化房间状态为可预订
                initializeRoomState(conn, room.getRNumber(), "可预订");
                
                result.setSuccess(true);
                result.setMessage("房间添加成功");
                LOGGER.info("房间添加成功: " + room.getRNumber());
            } else {
                result.setErrorMessage("房间添加失败");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "添加房间时发生数据库错误", e);
            result.setErrorMessage("添加房间时发生错误: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
        
        return result;
    }
    
    /**
     * 更新房间信息
     * @param room 房间信息
     * @return 操作结果
     */
    public OperationResult updateRoom(Room room) {
        OperationResult result = new OperationResult();
        
        if (room == null || room.getRNumber() == null) {
            result.setErrorMessage("房间信息不完整");
            return result;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                result.setErrorMessage("无法连接到数据库");
                return result;
            }
            
            // 检查房间是否存在
            if (!isRoomExists(conn, room.getRNumber())) {
                result.setErrorMessage("房间号 " + room.getRNumber() + " 不存在");
                return result;
            }
            
            // 更新房间信息
            String updateSql = "UPDATE rooms SET rType = ?, rPrice = ? WHERE rNumber = ?";
            stmt = conn.prepareStatement(updateSql);
            stmt.setString(1, room.getRType());
            stmt.setString(2, room.getRPrice());
            stmt.setString(3, room.getRNumber());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                result.setSuccess(true);
                result.setMessage("房间信息更新成功");
                LOGGER.info("房间信息更新成功: " + room.getRNumber());
            } else {
                result.setErrorMessage("房间信息更新失败");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "更新房间时发生数据库错误", e);
            result.setErrorMessage("更新房间时发生错误: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
        
        return result;
    }
    
    /**
     * 删除房间
     * @param roomNumber 房间号
     * @return 操作结果
     */
    public OperationResult deleteRoom(String roomNumber) {
        OperationResult result = new OperationResult();
        
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            result.setErrorMessage("房间号不能为空");
            return result;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                result.setErrorMessage("无法连接到数据库");
                return result;
            }
            
            conn.setAutoCommit(false);
            
            try {
                // 检查房间是否有未完成的预订
                if (hasActiveBookings(conn, roomNumber)) {
                    result.setErrorMessage("房间有未完成的预订，无法删除");
                    conn.rollback();
                    return result;
                }
                
                // 删除房间状态记录
                String deleteStateSql = "DELETE FROM roomstate WHERE rNumber = ?";
                stmt = conn.prepareStatement(deleteStateSql);
                stmt.setString(1, roomNumber);
                stmt.executeUpdate();
                stmt.close();
                
                // 删除房间记录
                String deleteRoomSql = "DELETE FROM rooms WHERE rNumber = ?";
                stmt = conn.prepareStatement(deleteRoomSql);
                stmt.setString(1, roomNumber);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    result.setSuccess(true);
                    result.setMessage("房间删除成功");
                    LOGGER.info("房间删除成功: " + roomNumber);
                } else {
                    conn.rollback();
                    result.setErrorMessage("房间不存在或删除失败");
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "删除房间时发生数据库错误", e);
            result.setErrorMessage("删除房间时发生错误: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "恢复自动提交模式失败", e);
            }
            DatabaseHelper.closeResources(conn, stmt, null);
        }
        
        return result;
    }
    
    /**
     * 查询所有房间
     * @return 房间列表
     */
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                LOGGER.warning("无法连接到数据库");
                return rooms;
            }
            
            String query = "SELECT r.rNumber, r.rType, r.rPrice, " +
                          "COALESCE(rs.state, '可预订') as state " +
                          "FROM rooms r " +
                          "LEFT JOIN roomstate rs ON r.rNumber = rs.rNumber " +
                          "ORDER BY r.rNumber";
            
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Room room = new Room();
                room.setRNumber(rs.getString("rNumber"));
                room.setRType(rs.getString("rType"));
                room.setRPrice(rs.getString("rPrice"));
                room.setState(rs.getString("state"));
                rooms.add(room);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询房间列表时发生数据库错误", e);
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return rooms;
    }
    
    /**
     * 查询可预订的房间
     * @return 可预订房间列表
     */
    public List<Room> getAvailableRooms() {
        List<Room> rooms = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                LOGGER.warning("无法连接到数据库");
                return rooms;
            }
            
            String query = "SELECT r.rNumber, r.rType, r.rPrice, " +
                          "COALESCE(rs.state, '可预订') as state " +
                          "FROM rooms r " +
                          "LEFT JOIN roomstate rs ON r.rNumber = rs.rNumber " +
                          "WHERE r.rNumber NOT IN (" +
                          "    SELECT b.rNumber FROM bookroom b WHERE b.checkin = '未入住'" +
                          ") " +
                          "ORDER BY r.rNumber";
            
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Room room = new Room();
                room.setRNumber(rs.getString("rNumber"));
                room.setRType(rs.getString("rType"));
                room.setRPrice(rs.getString("rPrice"));
                room.setState("可预订");
                rooms.add(room);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询可预订房间时发生数据库错误", e);
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return rooms;
    }
    
    /**
     * 根据房间号查询房间信息
     * @param roomNumber 房间号
     * @return 房间信息
     */
    public Room getRoomByNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            return null;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return null;
            }
            
            String query = "SELECT r.rNumber, r.rType, r.rPrice, " +
                          "COALESCE(rs.state, '可预订') as state " +
                          "FROM rooms r " +
                          "LEFT JOIN roomstate rs ON r.rNumber = rs.rNumber " +
                          "WHERE r.rNumber = ?";
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, roomNumber);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Room room = new Room();
                room.setRNumber(rs.getString("rNumber"));
                room.setRType(rs.getString("rType"));
                room.setRPrice(rs.getString("rPrice"));
                room.setState(rs.getString("state"));
                return room;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询房间信息时发生数据库错误", e);
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return null;
    }
    
    /**
     * 更新房间状态
     * @param roomNumber 房间号
     * @param state 新状态
     * @return 操作结果
     */
    public OperationResult updateRoomState(String roomNumber, String state) {
        OperationResult result = new OperationResult();
        
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            result.setErrorMessage("房间号不能为空");
            return result;
        }
        
        if (state == null || state.trim().isEmpty()) {
            result.setErrorMessage("房间状态不能为空");
            return result;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                result.setErrorMessage("无法连接到数据库");
                return result;
            }
            
            // 使用 INSERT ... ON DUPLICATE KEY UPDATE 语法
            String sql = "INSERT INTO roomstate (rNumber, state) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE state = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, roomNumber);
            stmt.setString(2, state);
            stmt.setString(3, state);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                result.setSuccess(true);
                result.setMessage("房间状态更新成功");
                LOGGER.info("房间状态更新成功: " + roomNumber + " -> " + state);
            } else {
                result.setErrorMessage("房间状态更新失败");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "更新房间状态时发生数据库错误", e);
            result.setErrorMessage("更新房间状态时发生错误: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
        
        return result;
    }
    
    /**
     * 获取房间统计信息
     * @return 房间统计
     */
    public RoomStats getRoomStatistics() {
        RoomStats stats = new RoomStats();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return stats;
            }
            
            // 查询房间总数
            String totalQuery = "SELECT COUNT(*) as total FROM rooms";
            stmt = conn.prepareStatement(totalQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setTotalRooms(rs.getInt("total"));
            }
            rs.close();
            stmt.close();
            
            // 查询已预订房间数
            String bookedQuery = "SELECT COUNT(DISTINCT b.rNumber) as booked " +
                                "FROM bookroom b WHERE b.checkin = '未入住'";
            stmt = conn.prepareStatement(bookedQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setBookedRooms(rs.getInt("booked"));
            }
            rs.close();
            stmt.close();
            
            // 查询已入住房间数
            String occupiedQuery = "SELECT COUNT(DISTINCT b.rNumber) as occupied " +
                                  "FROM bookroom b WHERE b.checkin = '已入住'";
            stmt = conn.prepareStatement(occupiedQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setOccupiedRooms(rs.getInt("occupied"));
            }
            rs.close();
            stmt.close();
            
            // 计算可用房间数
            stats.setAvailableRooms(stats.getTotalRooms() - stats.getBookedRooms() - stats.getOccupiedRooms());
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询房间统计时发生数据库错误", e);
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return stats;
    }
    
    /**
     * 检查房间是否存在
     */
    private boolean isRoomExists(Connection conn, String roomNumber) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String query = "SELECT COUNT(*) FROM rooms WHERE rNumber = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, roomNumber);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        
        return false;
    }
    
    /**
     * 检查房间是否有活跃的预订
     */
    private boolean hasActiveBookings(Connection conn, String roomNumber) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String query = "SELECT COUNT(*) FROM bookroom WHERE rNumber = ? AND checkin IN ('未入住', '已入住')";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, roomNumber);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        
        return false;
    }
    
    /**
     * 初始化房间状态
     */
    private void initializeRoomState(Connection conn, String roomNumber, String state) {
        PreparedStatement stmt = null;
        
        try {
            String sql = "INSERT INTO roomstate (rNumber, state) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE state = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, roomNumber);
            stmt.setString(2, state);
            stmt.setString(3, state);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "初始化房间状态失败", e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "关闭PreparedStatement失败", e);
                }
            }
        }
    }
    
    /**
     * 房间信息类
     */
    public static class Room {
        private String rNumber;
        private String rType;
        private String rPrice;
        private String state;
        
        // Constructors
        public Room() {}
        
        public Room(String rNumber, String rType, String rPrice) {
            this.rNumber = rNumber;
            this.rType = rType;
            this.rPrice = rPrice;
        }
        
        // Getters and Setters
        public String getRNumber() { return rNumber; }
        public void setRNumber(String rNumber) { this.rNumber = rNumber; }
        
        public String getRType() { return rType; }
        public void setRType(String rType) { this.rType = rType; }
        
        public String getRPrice() { return rPrice; }
        public void setRPrice(String rPrice) { this.rPrice = rPrice; }
        
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        
        @Override
        public String toString() {
            return "Room{" +
                   "rNumber='" + rNumber + '\'' +
                   ", rType='" + rType + '\'' +
                   ", rPrice='" + rPrice + '\'' +
                   ", state='" + state + '\'' +
                   '}';
        }
    }
    
    /**
     * 房间统计信息类
     */
    public static class RoomStats {
        private int totalRooms = 0;
        private int availableRooms = 0;
        private int bookedRooms = 0;
        private int occupiedRooms = 0;
        
        // Getters and Setters
        public int getTotalRooms() { return totalRooms; }
        public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }
        
        public int getAvailableRooms() { return availableRooms; }
        public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }
        
        public int getBookedRooms() { return bookedRooms; }
        public void setBookedRooms(int bookedRooms) { this.bookedRooms = bookedRooms; }
        
        public int getOccupiedRooms() { return occupiedRooms; }
        public void setOccupiedRooms(int occupiedRooms) { this.occupiedRooms = occupiedRooms; }
    }
    
    /**
     * 操作结果类
     */
    public static class OperationResult {
        private boolean success = false;
        private String message;
        private String errorMessage;
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
} 