package com.database.helper;

import java.sql.*;
import java.util.logging.Logger;

/**
 * 数据库诊断工具
 * 用于检查数据库表结构和数据完整性
 */
public class DatabaseDiagnostic {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseDiagnostic.class.getName());
    
    public static void main(String[] args) {
        System.out.println("=== 数据库诊断工具 ===");
        
        if (!DatabaseHelper.testConnection()) {
            System.out.println("数据库连接失败，无法进行诊断");
            return;
        }
        
        System.out.println("数据库连接正常\n");
        
        // 检查所有表
        checkAllTables();
        
        // 检查表结构
        checkTableStructures();
        
        // 检查数据完整性
        checkDataIntegrity();
        
        System.out.println("\n=== 诊断完成 ===");
    }
    
    private static void checkAllTables() {
        System.out.println("=== 检查数据库表 ===");
        
        Connection conn = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            
            // 获取所有表
            rs = meta.getTables(null, null, "%", new String[]{"TABLE"});
            
            System.out.println("现有数据库表:");
            boolean hasTable = false;
            while (rs.next()) {
                hasTable = true;
                String tableName = rs.getString("TABLE_NAME");
                System.out.println("- " + tableName);
            }
            
            if (!hasTable) {
                System.out.println("没有找到任何表");
            }
            
        } catch (SQLException e) {
            System.out.println("检查表时出错: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, null, rs);
        }
        
        System.out.println();
    }
    
    private static void checkTableStructures() {
        System.out.println("=== 检查表结构 ===");
        
        String[] tables = {"rooms", "roomstate", "bookroom", "client", "leader", "user_auth"};
        
        for (String tableName : tables) {
            checkTableStructure(tableName);
        }
        
        System.out.println();
    }
    
    private static void checkTableStructure(String tableName) {
        Connection conn = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            
            // 检查表是否存在
            rs = meta.getTables(null, null, tableName, new String[]{"TABLE"});
            if (!rs.next()) {
                System.out.println("表 " + tableName + " 不存在");
                return;
            }
            rs.close();
            
            System.out.println("表 " + tableName + " 结构:");
            
            // 获取列信息
            rs = meta.getColumns(null, null, tableName, "%");
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                boolean nullable = rs.getBoolean("NULLABLE");
                
                System.out.println("  - " + columnName + " " + dataType + "(" + columnSize + ")" + 
                                 (nullable ? " NULL" : " NOT NULL"));
            }
            
            // 检查数据行数
            rs.close();
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM " + tableName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("  数据行数: " + count);
            }
            stmt.close();
            
        } catch (SQLException e) {
            System.out.println("检查表 " + tableName + " 时出错: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, null, rs);
        }
        
        System.out.println();
    }
    
    private static void checkDataIntegrity() {
        System.out.println("=== 检查数据完整性 ===");
        
        // 检查房间数据
        checkRoomsData();
        
        // 检查房间状态数据
        checkRoomStateData();
        
        // 检查预订数据
        checkBookingData();
        
        System.out.println();
    }
    
    private static void checkRoomsData() {
        System.out.println("检查房间数据:");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            
            // 检查rooms表数据
            stmt = conn.prepareStatement("SELECT * FROM rooms LIMIT 5");
            rs = stmt.executeQuery();
            
            System.out.println("房间数据示例:");
            int count = 0;
            while (rs.next() && count < 5) {
                count++;
                System.out.println("  房间号: " + rs.getString("rNumber") + 
                                 ", 类型: " + rs.getString("rType") + 
                                 ", 价格: " + rs.getString("rPrice"));
            }
            
            if (count == 0) {
                System.out.println("  没有房间数据");
            }
            
        } catch (SQLException e) {
            System.out.println("检查房间数据时出错: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        System.out.println();
    }
    
    private static void checkRoomStateData() {
        System.out.println("检查房间状态数据:");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            
            // 检查roomstate表是否存在
            DatabaseMetaData meta = conn.getMetaData();
            rs = meta.getTables(null, null, "roomstate", new String[]{"TABLE"});
            if (!rs.next()) {
                System.out.println("  roomstate表不存在");
                return;
            }
            rs.close();
            
            // 检查roomstate表数据
            stmt = conn.prepareStatement("SELECT * FROM roomstate");
            rs = stmt.executeQuery();
            
            System.out.println("房间状态数据:");
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.println("  房间号: " + rs.getString("rNumber") + 
                                 ", 状态: " + rs.getString("state"));
            }
            
            if (!hasData) {
                System.out.println("  没有房间状态数据");
            }
            
        } catch (SQLException e) {
            System.out.println("检查房间状态数据时出错: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        System.out.println();
    }
    
    private static void checkBookingData() {
        System.out.println("检查预订数据:");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            
            // 检查bookroom表是否存在
            DatabaseMetaData meta = conn.getMetaData();
            rs = meta.getTables(null, null, "bookroom", new String[]{"TABLE"});
            if (!rs.next()) {
                System.out.println("  bookroom表不存在");
                return;
            }
            rs.close();
            
            // 检查bookroom表数据
            stmt = conn.prepareStatement("SELECT * FROM bookroom ORDER BY bookdate DESC LIMIT 5");
            rs = stmt.executeQuery();
            
            System.out.println("预订数据示例:");
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.println("  预订号: " + rs.getString("bookNumber") + 
                                 ", 客户: " + rs.getString("cNumber") + 
                                 ", 房间: " + rs.getString("rNumber") + 
                                 ", 日期: " + rs.getString("bookdate") + 
                                 ", 状态: " + rs.getString("checkin"));
            }
            
            if (!hasData) {
                System.out.println("  没有预订数据");
            }
            
        } catch (SQLException e) {
            System.out.println("检查预订数据时出错: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        System.out.println();
    }
} 