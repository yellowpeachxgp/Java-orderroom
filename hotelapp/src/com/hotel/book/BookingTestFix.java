package com.hotel.book;

import com.database.helper.DatabaseHelper;
import com.hotel.auth.UserAuthManager;

import javax.swing.*;
import java.sql.*;
import java.util.logging.Logger;

/**
 * 预订功能修复测试
 * 验证登录用户预订功能是否正常
 */
public class BookingTestFix {
    
    private static final Logger LOGGER = Logger.getLogger(BookingTestFix.class.getName());
    
    public static void main(String[] args) {
        System.out.println("=== 预订功能修复测试 ===");
        
        // 1. 测试数据库连接
        System.out.println("1. 测试数据库连接...");
        if (!DatabaseHelper.testConnection()) {
            System.out.println("❌ 数据库连接失败");
            return;
        }
        System.out.println("✅ 数据库连接正常");
        
        // 2. 模拟用户登录
        System.out.println("\n2. 模拟用户登录...");
        String testUserId = "USER001";
        String testUserName = "测试用户";
        UserAuthManager.setCurrentUser(testUserId, testUserName, UserAuthManager.USER_TYPE_CLIENT, false);
        System.out.println("✅ 用户登录成功: " + testUserId);
        
        // 3. 测试GUEST预订功能
        System.out.println("\n3. 测试GUEST预订功能...");
        testBookingWithUser("GUEST");
        
        // 4. 测试登录用户预订功能
        System.out.println("\n4. 测试登录用户预订功能...");
        testBookingWithUser(testUserId);
        
        // 5. 测试空用户名预订功能
        System.out.println("\n5. 测试空用户名预订功能...");
        testBookingWithUser(null);
        
        System.out.println("\n=== 测试完成 ===");
    }
    
    private static void testBookingWithUser(String username) {
        try {
            System.out.println("正在为用户 " + username + " 创建预订界面...");
            
            // 在事件分发线程中创建UI
            SwingUtilities.invokeAndWait(() -> {
                try {
                    RoomBooking booking = new RoomBooking(username);
                    System.out.println("✅ 预订界面创建成功");
                    
                    // 等待一秒钟让界面完全加载
                    Thread.sleep(1000);
                    
                    // 关闭界面
                    booking.dispose();
                    System.out.println("✅ 预订界面关闭成功");
                    
                } catch (Exception e) {
                    System.out.println("❌ 预订界面创建失败: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (Exception e) {
            System.out.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 验证数据库中是否有房间数据
     */
    private static boolean hasRoomData() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) return false;
            
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM rooms");
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.out.println("检查房间数据失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return false;
    }
    
    /**
     * 创建测试房间数据
     */
    private static void createTestRoomData() {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) return;
            
            // 插入测试房间
            String insertSql = "INSERT IGNORE INTO rooms (rNumber, rType, rPrice) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(insertSql);
            
            stmt.setString(1, "TEST001");
            stmt.setString(2, "标准间");
            stmt.setString(3, "200");
            stmt.executeUpdate();
            
            System.out.println("✅ 测试房间数据创建成功");
            
        } catch (SQLException e) {
            System.out.println("❌ 创建测试房间数据失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
    }
} 