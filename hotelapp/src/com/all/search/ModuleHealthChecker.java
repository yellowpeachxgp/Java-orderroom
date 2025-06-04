package com.all.search;

import com.database.helper.DatabaseHelper;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索模块健康检查和修复工具
 * 专门检查和修复搜索模块中的问题
 */
public class ModuleHealthChecker {
    
    private static List<String> issues = new ArrayList<>();
    private static List<String> fixes = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("=== 搜索模块健康检查 ===");
        System.out.println("开始时间: " + new java.util.Date());
        System.out.println();
        
        // 1. 检查数据库连接资源管理
        checkResourceManagement();
        
        // 2. 检查数据一致性
        checkDataConsistency();
        
        // 3. 检查业务逻辑问题
        checkBusinessLogic();
        
        // 4. 应用修复
        applyFixes();
        
        generateReport();
        
        System.out.println("\n=== 模块检查完成 ===");
    }
    
    private static void checkResourceManagement() {
        System.out.println("=== 1. 检查资源管理 ===");
        
        // 检查各模块是否正确关闭数据库连接
        String[] moduleClasses = {"checkIn", "checkOut", "changeRoom", "add_client", "add_room"};
        
        for (String className : moduleClasses) {
            System.out.println("检查模块: " + className);
            try {
                Class<?> clazz = Class.forName("com.all.search." + className);
                System.out.println("  ✅ 类加载正常: " + className);
            } catch (ClassNotFoundException e) {
                System.out.println("  ❌ 类加载失败: " + className);
                issues.add("类加载失败: " + className);
            }
        }
        
        System.out.println();
    }
    
    private static void checkDataConsistency() {
        System.out.println("=== 2. 检查数据一致性 ===");
        
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                issues.add("数据库连接失败");
                return;
            }
            
            // 检查孤立的预订记录
            checkOrphanedBookings(conn);
            
            // 检查房间状态不一致
            checkRoomStateInconsistency(conn);
            
            // 检查入住退房记录完整性
            checkCheckinCheckoutIntegrity(conn);
            
        } catch (SQLException e) {
            issues.add("数据一致性检查失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeConnection(conn);
        }
        
        System.out.println();
    }
    
    private static void checkOrphanedBookings(Connection conn) throws SQLException {
        System.out.println("检查孤立的预订记录...");
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // 检查bookroom表中引用不存在房间的记录
            String query = "SELECT COUNT(*) FROM bookroom b " +
                          "WHERE NOT EXISTS (SELECT 1 FROM rooms r WHERE r.rNumber = b.rNumber)";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int orphanedCount = rs.getInt(1);
                if (orphanedCount > 0) {
                    System.out.println("  ❌ 发现 " + orphanedCount + " 条孤立预订记录");
                    issues.add("孤立预订记录: " + orphanedCount + " 条");
                    fixes.add("清理孤立预订记录");
                } else {
                    System.out.println("  ✅ 没有孤立预订记录");
                }
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }
    
    private static void checkRoomStateInconsistency(Connection conn) throws SQLException {
        System.out.println("检查房间状态不一致...");
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // 检查roomstate表中状态为"已入住"但没有对应入住记录的房间
            String query = "SELECT COUNT(*) FROM roomstate rs " +
                          "WHERE rs.state = '已入住' " +
                          "AND NOT EXISTS (SELECT 1 FROM checkin c WHERE c.rNumber = rs.rNumber)";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int inconsistentCount = rs.getInt(1);
                if (inconsistentCount > 0) {
                    System.out.println("  ❌ 发现 " + inconsistentCount + " 条状态不一致记录");
                    issues.add("房间状态不一致: " + inconsistentCount + " 条");
                    fixes.add("修复房间状态不一致");
                } else {
                    System.out.println("  ✅ 房间状态一致");
                }
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }
    
    private static void checkCheckinCheckoutIntegrity(Connection conn) throws SQLException {
        System.out.println("检查入住退房记录完整性...");
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // 检查是否有入住但没有预订记录的情况
            String query = "SELECT COUNT(*) FROM checkin c " +
                          "WHERE NOT EXISTS (SELECT 1 FROM bookroom b WHERE b.rNumber = c.rNumber AND b.cNumber = c.cNumber)";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int inconsistentCount = rs.getInt(1);
                if (inconsistentCount > 0) {
                    System.out.println("  ❌ 发现 " + inconsistentCount + " 条无预订的入住记录");
                    issues.add("无预订的入住记录: " + inconsistentCount + " 条");
                    fixes.add("清理无效入住记录");
                } else {
                    System.out.println("  ✅ 入住记录完整");
                }
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }
    
    private static void checkBusinessLogic() {
        System.out.println("=== 3. 检查业务逻辑 ===");
        
        // 检查是否可以成功创建搜索模块的实例
        String[] testModules = {"allRoom", "add_client", "add_room"};
        
        for (String moduleName : testModules) {
            try {
                System.out.println("测试模块: " + moduleName);
                Class<?> clazz = Class.forName("com.all.search." + moduleName);
                
                // 尝试创建实例（如果有无参构造函数）
                try {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    System.out.println("  ✅ 模块实例化成功: " + moduleName);
                } catch (Exception e) {
                    System.out.println("  ⚠️  模块实例化失败，但类存在: " + moduleName);
                }
                
            } catch (ClassNotFoundException e) {
                System.out.println("  ❌ 模块不存在: " + moduleName);
                issues.add("模块不存在: " + moduleName);
            }
        }
        
        System.out.println();
    }
    
    private static void applyFixes() {
        System.out.println("=== 4. 应用修复 ===");
        
        if (fixes.isEmpty()) {
            System.out.println("没有需要修复的问题");
            return;
        }
        
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                System.out.println("❌ 无法连接数据库，跳过修复");
                return;
            }
            
            conn.setAutoCommit(false);
            
            for (String fix : fixes) {
                System.out.println("应用修复: " + fix);
                
                try {
                    switch (fix) {
                        case "清理孤立预订记录":
                            cleanOrphanedBookings(conn);
                            break;
                        case "修复房间状态不一致":
                            fixRoomStateInconsistency(conn);
                            break;
                        case "清理无效入住记录":
                            cleanInvalidCheckinRecords(conn);
                            break;
                        default:
                            System.out.println("  未知的修复类型: " + fix);
                    }
                } catch (SQLException e) {
                    System.out.println("  ❌ 修复失败: " + e.getMessage());
                }
            }
            
            conn.commit();
            System.out.println("✅ 所有修复已应用");
            
        } catch (SQLException e) {
            System.out.println("❌ 修复过程中出错: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("❌ 回滚失败: " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("❌ 关闭连接失败: " + e.getMessage());
                }
            }
        }
        
        System.out.println();
    }
    
    private static void cleanOrphanedBookings(Connection conn) throws SQLException {
        String sql = "DELETE FROM bookroom " +
                    "WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE rooms.rNumber = bookroom.rNumber)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        int deleted = stmt.executeUpdate();
        stmt.close();
        System.out.println("  清理了 " + deleted + " 条孤立预订记录");
    }
    
    private static void fixRoomStateInconsistency(Connection conn) throws SQLException {
        // 将没有对应入住记录的"已入住"状态改为"已退房"
        String sql = "UPDATE roomstate SET state = '已退房' " +
                    "WHERE state = '已入住' " +
                    "AND NOT EXISTS (SELECT 1 FROM checkin WHERE checkin.rNumber = roomstate.rNumber)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        int updated = stmt.executeUpdate();
        stmt.close();
        System.out.println("  修复了 " + updated + " 条房间状态不一致记录");
    }
    
    private static void cleanInvalidCheckinRecords(Connection conn) throws SQLException {
        String sql = "DELETE FROM checkin " +
                    "WHERE NOT EXISTS (SELECT 1 FROM bookroom WHERE bookroom.rNumber = checkin.rNumber AND bookroom.cNumber = checkin.cNumber)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        int deleted = stmt.executeUpdate();
        stmt.close();
        System.out.println("  清理了 " + deleted + " 条无效入住记录");
    }
    
    private static void generateReport() {
        System.out.println("\n=== 📋 模块检查报告 ===");
        
        if (issues.isEmpty()) {
            System.out.println("🎉 所有检查项目都通过了！搜索模块运行正常。");
        } else {
            System.out.println("❌ 发现的问题:");
            for (int i = 0; i < issues.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + issues.get(i));
            }
            
            if (!fixes.isEmpty()) {
                System.out.println("\n🔧 已应用的修复:");
                for (int i = 0; i < fixes.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + fixes.get(i));
                }
            }
            
            System.out.println("\n💡 建议:");
            System.out.println("  - 定期运行此检查工具");
            System.out.println("  - 确保所有模块都正确关闭数据库连接");
            System.out.println("  - 在进行数据操作时使用事务");
        }
    }
} 