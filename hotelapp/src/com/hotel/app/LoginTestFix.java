package com.hotel.app;

import com.database.helper.DatabaseHelper;
import com.hotel.auth.UserAuthManager;

import javax.swing.*;
import java.sql.*;
import java.util.logging.Logger;

/**
 * 登录功能和数据库连接测试
 * 验证app模块是否能正常连接数据库
 */
public class LoginTestFix {
    
    private static final Logger LOGGER = Logger.getLogger(LoginTestFix.class.getName());
    
    public static void main(String[] args) {
        System.out.println("=== 登录功能和数据库连接测试 ===");
        
        // 1. 测试数据库连接
        System.out.println("1. 测试数据库连接...");
        if (!DatabaseHelper.testConnection()) {
            System.out.println("❌ 数据库连接失败");
            return;
        }
        System.out.println("✅ 数据库连接正常");
        
        // 2. 创建测试用户数据
        System.out.println("\n2. 创建测试用户数据...");
        createTestUserData();
        
        // 3. 测试用户认证管理器
        System.out.println("\n3. 测试用户认证管理器...");
        testUserAuthManager();
        
        // 4. 测试LoginPage（非UI部分）
        System.out.println("\n4. 测试LoginPage核心功能...");
        testLoginPageCore();
        
        System.out.println("\n=== 测试完成 ===");
    }
    
    /**
     * 创建测试用户数据
     */
    private static void createTestUserData() {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                System.out.println("❌ 无法获取数据库连接");
                return;
            }
            
            // 创建测试客户
            String insertClientSql = "INSERT IGNORE INTO client (cNumber, cName, cAge, cSex, cTel) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(insertClientSql);
            stmt.setString(1, "USER001");
            stmt.setString(2, "测试用户");
            stmt.setString(3, "25");
            stmt.setString(4, "男");
            stmt.setString(5, "13800138000");
            stmt.executeUpdate();
            stmt.close();
            
            // 创建测试管理员
            String insertLeaderSql = "INSERT IGNORE INTO leader (lNumber, lName, lSex, lTel) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(insertLeaderSql);
            stmt.setString(1, "ADMIN001");
            stmt.setString(2, "测试管理员");
            stmt.setString(3, "女");
            stmt.setString(4, "13900139000");
            stmt.executeUpdate();
            
            System.out.println("✅ 测试用户数据创建成功");
            
        } catch (SQLException e) {
            System.out.println("❌ 创建测试用户数据失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
    }
    
    /**
     * 测试用户认证管理器
     */
    private static void testUserAuthManager() {
        try {
            // 测试设置用户
            UserAuthManager.setCurrentUser("USER001", "测试用户", UserAuthManager.USER_TYPE_CLIENT, false);
            
            // 验证用户信息
            if ("USER001".equals(UserAuthManager.getCurrentUserId()) &&
                "测试用户".equals(UserAuthManager.getCurrentUserName()) &&
                !UserAuthManager.isCurrentUserAdmin()) {
                System.out.println("✅ 用户认证管理器功能正常");
            } else {
                System.out.println("❌ 用户认证管理器功能异常");
            }
            
            // 测试管理员
            UserAuthManager.setCurrentUser("ADMIN001", "测试管理员", UserAuthManager.USER_TYPE_LEADER, false);
            
            if (UserAuthManager.isCurrentUserAdmin()) {
                System.out.println("✅ 管理员权限验证正常");
            } else {
                System.out.println("❌ 管理员权限验证异常");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 用户认证管理器测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试LoginPage核心功能（数据库查询）
     */
    private static void testLoginPageCore() {
        try {
            // 测试客户端用户验证
            boolean clientExists = checkUserExists("USER001", "client", "cNumber");
            if (clientExists) {
                System.out.println("✅ 客户端用户查询正常");
            } else {
                System.out.println("❌ 客户端用户查询失败");
            }
            
            // 测试管理员用户验证
            boolean leaderExists = checkUserExists("ADMIN001", "leader", "lNumber");
            if (leaderExists) {
                System.out.println("✅ 管理员用户查询正常");
            } else {
                System.out.println("❌ 管理员用户查询失败");
            }
            
        } catch (Exception e) {
            System.out.println("❌ LoginPage核心功能测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检查用户是否存在
     */
    private static boolean checkUserExists(String userId, String tableName, String idField) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return false;
            }
            
            String query = "SELECT * FROM " + tableName + " WHERE " + idField + " = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userId);
            stmt.setQueryTimeout(3);
            
            rs = stmt.executeQuery();
            return rs.next();
            
        } catch (SQLException e) {
            System.out.println("检查用户存在性失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * 测试表结构
     */
    private static void testTableStructure() {
        System.out.println("\n测试表结构:");
        String[] tables = {"client", "leader", "rooms", "bookroom", "roomstate"};
        
        for (String table : tables) {
            if (checkTableExists(table)) {
                System.out.println("✅ 表 " + table + " 存在");
            } else {
                System.out.println("❌ 表 " + table + " 不存在");
            }
        }
    }
    
    /**
     * 检查表是否存在
     */
    private static boolean checkTableExists(String tableName) {
        Connection conn = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) return false;
            
            DatabaseMetaData meta = conn.getMetaData();
            rs = meta.getTables(null, null, tableName, new String[]{"TABLE"});
            return rs.next();
            
        } catch (SQLException e) {
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, null, rs);
        }
    }
} 