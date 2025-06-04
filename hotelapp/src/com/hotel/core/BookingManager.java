package com.hotel.core;

import com.database.helper.DatabaseHelper;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 统一预订管理器
 * 整合所有预订相关功能，包括预订、取消、入住、退房等
 */
public class BookingManager {
    
    private static final Logger LOGGER = Logger.getLogger(BookingManager.class.getName());
    private static BookingManager instance;
    
    private BookingManager() {}
    
    public static BookingManager getInstance() {
        if (instance == null) {
            synchronized (BookingManager.class) {
                if (instance == null) {
                    instance = new BookingManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 创建房间预订
     * @param booking 预订信息
     * @return 预订结果
     */
    public BookingResult createBooking(Booking booking) {
        BookingResult result = new BookingResult();
        
        if (booking == null) {
            result.setErrorMessage("预订信息不能为空");
            return result;
        }
        
        if (booking.getCustomerId() == null || booking.getCustomerId().trim().isEmpty()) {
            result.setErrorMessage("客户ID不能为空");
            return result;
        }
        
        if (booking.getRoomNumber() == null || booking.getRoomNumber().trim().isEmpty()) {
            result.setErrorMessage("房间号不能为空");
            return result;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                result.setErrorMessage("无法连接到数据库");
                return result;
            }
            
            conn.setAutoCommit(false);
            
            try {
                // 1. 检查房间是否存在
                if (!isRoomExists(conn, booking.getRoomNumber())) {
                    result.setErrorMessage("房间号 " + booking.getRoomNumber() + " 不存在");
                    conn.rollback();
                    return result;
                }
                
                // 2. 检查房间是否可预订
                if (!isRoomAvailable(conn, booking.getRoomNumber())) {
                    result.setErrorMessage("房间已被预订或入住，无法预订");
                    conn.rollback();
                    return result;
                }
                
                // 3. 生成预订号
                String bookNumber = generateBookNumber();
                booking.setBookNumber(bookNumber);
                
                // 4. 插入预订记录
                String insertBookSql = "INSERT INTO bookroom (bookNumber, cNumber, rNumber, bookdate, checkin) " +
                                      "VALUES (?, ?, ?, ?, '未入住')";
                stmt = conn.prepareStatement(insertBookSql);
                stmt.setString(1, bookNumber);
                stmt.setString(2, booking.getCustomerId());
                stmt.setString(3, booking.getRoomNumber());
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String bookDate = booking.getBookDate() != null ? 
                    sdf.format(booking.getBookDate()) : sdf.format(new Date());
                stmt.setString(4, bookDate);
                
                int bookResult = stmt.executeUpdate();
                stmt.close();
                
                if (bookResult <= 0) {
                    result.setErrorMessage("插入预订记录失败");
                    conn.rollback();
                    return result;
                }
                
                // 5. 更新房间状态
                RoomManager roomManager = RoomManager.getInstance();
                RoomManager.OperationResult stateResult = roomManager.updateRoomState(booking.getRoomNumber(), "已预订");
                
                if (!stateResult.isSuccess()) {
                    result.setErrorMessage("更新房间状态失败: " + stateResult.getErrorMessage());
                    conn.rollback();
                    return result;
                }
                
                conn.commit();
                
                result.setSuccess(true);
                result.setBookNumber(bookNumber);
                result.setMessage("预订成功");
                
                LOGGER.info("预订创建成功: " + bookNumber + ", 客户: " + booking.getCustomerId() + 
                           ", 房间: " + booking.getRoomNumber());
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "创建预订时发生数据库错误", e);
            result.setErrorMessage("创建预订时发生错误: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "恢复自动提交模式失败", e);
            }
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return result;
    }
    
    /**
     * 取消预订
     * @param bookNumber 预订号
     * @return 操作结果
     */
    public BookingResult cancelBooking(String bookNumber) {
        BookingResult result = new BookingResult();
        
        if (bookNumber == null || bookNumber.trim().isEmpty()) {
            result.setErrorMessage("预订号不能为空");
            return result;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                result.setErrorMessage("无法连接到数据库");
                return result;
            }
            
            conn.setAutoCommit(false);
            
            try {
                // 1. 查询预订信息
                String queryBookSql = "SELECT rNumber, checkin FROM bookroom WHERE bookNumber = ?";
                stmt = conn.prepareStatement(queryBookSql);
                stmt.setString(1, bookNumber);
                rs = stmt.executeQuery();
                
                if (!rs.next()) {
                    result.setErrorMessage("预订号 " + bookNumber + " 不存在");
                    conn.rollback();
                    return result;
                }
                
                String roomNumber = rs.getString("rNumber");
                String checkinStatus = rs.getString("checkin");
                
                rs.close();
                stmt.close();
                
                // 2. 检查预订状态
                if ("已入住".equals(checkinStatus)) {
                    result.setErrorMessage("客户已入住，无法取消预订");
                    conn.rollback();
                    return result;
                } else if ("已退房".equals(checkinStatus)) {
                    result.setErrorMessage("客户已退房，无法取消预订");
                    conn.rollback();
                    return result;
                }
                
                // 3. 删除预订记录
                String deleteBookSql = "DELETE FROM bookroom WHERE bookNumber = ?";
                stmt = conn.prepareStatement(deleteBookSql);
                stmt.setString(1, bookNumber);
                int deleteResult = stmt.executeUpdate();
                stmt.close();
                
                if (deleteResult <= 0) {
                    result.setErrorMessage("删除预订记录失败");
                    conn.rollback();
                    return result;
                }
                
                // 4. 更新房间状态为可预订
                RoomManager roomManager = RoomManager.getInstance();
                RoomManager.OperationResult stateResult = roomManager.updateRoomState(roomNumber, "可预订");
                
                if (!stateResult.isSuccess()) {
                    result.setErrorMessage("更新房间状态失败: " + stateResult.getErrorMessage());
                    conn.rollback();
                    return result;
                }
                
                conn.commit();
                
                result.setSuccess(true);
                result.setMessage("预订取消成功");
                
                LOGGER.info("预订取消成功: " + bookNumber + ", 房间: " + roomNumber);
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "取消预订时发生数据库错误", e);
            result.setErrorMessage("取消预订时发生错误: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "恢复自动提交模式失败", e);
            }
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return result;
    }
    
    /**
     * 办理入住
     * @param bookNumber 预订号
     * @return 操作结果
     */
    public BookingResult checkIn(String bookNumber) {
        BookingResult result = new BookingResult();
        
        if (bookNumber == null || bookNumber.trim().isEmpty()) {
            result.setErrorMessage("预订号不能为空");
            return result;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                result.setErrorMessage("无法连接到数据库");
                return result;
            }
            
            conn.setAutoCommit(false);
            
            try {
                // 1. 查询预订信息
                String queryBookSql = "SELECT rNumber, checkin FROM bookroom WHERE bookNumber = ?";
                stmt = conn.prepareStatement(queryBookSql);
                stmt.setString(1, bookNumber);
                rs = stmt.executeQuery();
                
                if (!rs.next()) {
                    result.setErrorMessage("预订号 " + bookNumber + " 不存在");
                    conn.rollback();
                    return result;
                }
                
                String roomNumber = rs.getString("rNumber");
                String checkinStatus = rs.getString("checkin");
                
                rs.close();
                stmt.close();
                
                // 2. 检查预订状态
                if (!"未入住".equals(checkinStatus)) {
                    result.setErrorMessage("预订状态异常，当前状态: " + checkinStatus);
                    conn.rollback();
                    return result;
                }
                
                // 3. 更新预订状态为已入住
                String updateBookSql = "UPDATE bookroom SET checkin = '已入住' WHERE bookNumber = ?";
                stmt = conn.prepareStatement(updateBookSql);
                stmt.setString(1, bookNumber);
                int updateResult = stmt.executeUpdate();
                stmt.close();
                
                if (updateResult <= 0) {
                    result.setErrorMessage("更新预订状态失败");
                    conn.rollback();
                    return result;
                }
                
                // 4. 更新房间状态为已入住
                RoomManager roomManager = RoomManager.getInstance();
                RoomManager.OperationResult stateResult = roomManager.updateRoomState(roomNumber, "已入住");
                
                if (!stateResult.isSuccess()) {
                    result.setErrorMessage("更新房间状态失败: " + stateResult.getErrorMessage());
                    conn.rollback();
                    return result;
                }
                
                conn.commit();
                
                result.setSuccess(true);
                result.setMessage("入住办理成功");
                
                LOGGER.info("入住办理成功: " + bookNumber + ", 房间: " + roomNumber);
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "办理入住时发生数据库错误", e);
            result.setErrorMessage("办理入住时发生错误: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "恢复自动提交模式失败", e);
            }
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return result;
    }
    
    /**
     * 办理退房
     * @param bookNumber 预订号
     * @return 操作结果
     */
    public BookingResult checkOut(String bookNumber) {
        BookingResult result = new BookingResult();
        
        if (bookNumber == null || bookNumber.trim().isEmpty()) {
            result.setErrorMessage("预订号不能为空");
            return result;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                result.setErrorMessage("无法连接到数据库");
                return result;
            }
            
            conn.setAutoCommit(false);
            
            try {
                // 1. 查询预订信息
                String queryBookSql = "SELECT rNumber, checkin FROM bookroom WHERE bookNumber = ?";
                stmt = conn.prepareStatement(queryBookSql);
                stmt.setString(1, bookNumber);
                rs = stmt.executeQuery();
                
                if (!rs.next()) {
                    result.setErrorMessage("预订号 " + bookNumber + " 不存在");
                    conn.rollback();
                    return result;
                }
                
                String roomNumber = rs.getString("rNumber");
                String checkinStatus = rs.getString("checkin");
                
                rs.close();
                stmt.close();
                
                // 2. 检查预订状态
                if (!"已入住".equals(checkinStatus)) {
                    result.setErrorMessage("客户尚未入住，无法办理退房。当前状态: " + checkinStatus);
                    conn.rollback();
                    return result;
                }
                
                // 3. 更新预订状态为已退房
                String updateBookSql = "UPDATE bookroom SET checkin = '已退房' WHERE bookNumber = ?";
                stmt = conn.prepareStatement(updateBookSql);
                stmt.setString(1, bookNumber);
                int updateResult = stmt.executeUpdate();
                stmt.close();
                
                if (updateResult <= 0) {
                    result.setErrorMessage("更新预订状态失败");
                    conn.rollback();
                    return result;
                }
                
                // 4. 更新房间状态为可预订
                RoomManager roomManager = RoomManager.getInstance();
                RoomManager.OperationResult stateResult = roomManager.updateRoomState(roomNumber, "可预订");
                
                if (!stateResult.isSuccess()) {
                    result.setErrorMessage("更新房间状态失败: " + stateResult.getErrorMessage());
                    conn.rollback();
                    return result;
                }
                
                conn.commit();
                
                result.setSuccess(true);
                result.setMessage("退房办理成功");
                
                LOGGER.info("退房办理成功: " + bookNumber + ", 房间: " + roomNumber);
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "办理退房时发生数据库错误", e);
            result.setErrorMessage("办理退房时发生错误: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "恢复自动提交模式失败", e);
            }
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return result;
    }
    
    /**
     * 查询所有预订记录
     * @return 预订列表
     */
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                LOGGER.warning("无法连接到数据库");
                return bookings;
            }
            
            String query = "SELECT b.bookNumber, b.cNumber, b.rNumber, b.bookdate, b.checkin, " +
                          "c.cName, r.rType, r.rPrice " +
                          "FROM bookroom b " +
                          "LEFT JOIN client c ON b.cNumber = c.cNumber " +
                          "LEFT JOIN rooms r ON b.rNumber = r.rNumber " +
                          "ORDER BY b.bookdate DESC";
            
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookNumber(rs.getString("bookNumber"));
                booking.setCustomerId(rs.getString("cNumber"));
                booking.setRoomNumber(rs.getString("rNumber"));
                booking.setBookDate(rs.getDate("bookdate"));
                booking.setStatus(rs.getString("checkin"));
                booking.setCustomerName(rs.getString("cName"));
                booking.setRoomType(rs.getString("rType"));
                booking.setRoomPrice(rs.getString("rPrice"));
                bookings.add(booking);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询预订列表时发生数据库错误", e);
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return bookings;
    }
    
    /**
     * 根据客户ID查询预订记录
     * @param customerId 客户ID
     * @return 预订列表
     */
    public List<Booking> getBookingsByCustomer(String customerId) {
        List<Booking> bookings = new ArrayList<>();
        
        if (customerId == null || customerId.trim().isEmpty()) {
            return bookings;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                LOGGER.warning("无法连接到数据库");
                return bookings;
            }
            
            String query = "SELECT b.bookNumber, b.cNumber, b.rNumber, b.bookdate, b.checkin, " +
                          "c.cName, r.rType, r.rPrice " +
                          "FROM bookroom b " +
                          "LEFT JOIN client c ON b.cNumber = c.cNumber " +
                          "LEFT JOIN rooms r ON b.rNumber = r.rNumber " +
                          "WHERE b.cNumber = ? " +
                          "ORDER BY b.bookdate DESC";
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, customerId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookNumber(rs.getString("bookNumber"));
                booking.setCustomerId(rs.getString("cNumber"));
                booking.setRoomNumber(rs.getString("rNumber"));
                booking.setBookDate(rs.getDate("bookdate"));
                booking.setStatus(rs.getString("checkin"));
                booking.setCustomerName(rs.getString("cName"));
                booking.setRoomType(rs.getString("rType"));
                booking.setRoomPrice(rs.getString("rPrice"));
                bookings.add(booking);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询客户预订记录时发生数据库错误", e);
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return bookings;
    }
    
    /**
     * 根据预订号查询预订信息
     * @param bookNumber 预订号
     * @return 预订信息
     */
    public Booking getBookingByNumber(String bookNumber) {
        if (bookNumber == null || bookNumber.trim().isEmpty()) {
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
            
            String query = "SELECT b.bookNumber, b.cNumber, b.rNumber, b.bookdate, b.checkin, " +
                          "c.cName, r.rType, r.rPrice " +
                          "FROM bookroom b " +
                          "LEFT JOIN client c ON b.cNumber = c.cNumber " +
                          "LEFT JOIN rooms r ON b.rNumber = r.rNumber " +
                          "WHERE b.bookNumber = ?";
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, bookNumber);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Booking booking = new Booking();
                booking.setBookNumber(rs.getString("bookNumber"));
                booking.setCustomerId(rs.getString("cNumber"));
                booking.setRoomNumber(rs.getString("rNumber"));
                booking.setBookDate(rs.getDate("bookdate"));
                booking.setStatus(rs.getString("checkin"));
                booking.setCustomerName(rs.getString("cName"));
                booking.setRoomType(rs.getString("rType"));
                booking.setRoomPrice(rs.getString("rPrice"));
                return booking;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询预订信息时发生数据库错误", e);
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return null;
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
     * 检查房间是否可预订
     */
    private boolean isRoomAvailable(Connection conn, String roomNumber) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // 检查是否有未入住或已入住的预订
            String query = "SELECT COUNT(*) FROM bookroom WHERE rNumber = ? AND checkin IN ('未入住', '已入住')";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, roomNumber);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) == 0; // 如果没有活跃预订，则可预订
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        
        return false;
    }
    
    /**
     * 生成预订号
     */
    private String generateBookNumber() {
        return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
    
    /**
     * 预订信息类
     */
    public static class Booking {
        private String bookNumber;
        private String customerId;
        private String customerName;
        private String roomNumber;
        private String roomType;
        private String roomPrice;
        private Date bookDate;
        private String status;
        
        // Constructors
        public Booking() {}
        
        public Booking(String customerId, String roomNumber) {
            this.customerId = customerId;
            this.roomNumber = roomNumber;
            this.bookDate = new Date();
        }
        
        // Getters and Setters
        public String getBookNumber() { return bookNumber; }
        public void setBookNumber(String bookNumber) { this.bookNumber = bookNumber; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        
        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
        
        public String getRoomType() { return roomType; }
        public void setRoomType(String roomType) { this.roomType = roomType; }
        
        public String getRoomPrice() { return roomPrice; }
        public void setRoomPrice(String roomPrice) { this.roomPrice = roomPrice; }
        
        public Date getBookDate() { return bookDate; }
        public void setBookDate(Date bookDate) { this.bookDate = bookDate; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        @Override
        public String toString() {
            return "Booking{" +
                   "bookNumber='" + bookNumber + '\'' +
                   ", customerId='" + customerId + '\'' +
                   ", roomNumber='" + roomNumber + '\'' +
                   ", bookDate=" + bookDate +
                   ", status='" + status + '\'' +
                   '}';
        }
    }
    
    /**
     * 预订结果类
     */
    public static class BookingResult {
        private boolean success = false;
        private String bookNumber;
        private String message;
        private String errorMessage;
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getBookNumber() { return bookNumber; }
        public void setBookNumber(String bookNumber) { this.bookNumber = bookNumber; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
} 