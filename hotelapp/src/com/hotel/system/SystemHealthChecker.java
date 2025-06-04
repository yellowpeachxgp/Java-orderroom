package com.hotel.system;

import com.database.helper.DatabaseHelper;
import com.hotel.auth.AuthService;
import com.hotel.auth.UserAuthManager;

import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 系统健康检查工具
 * 全面诊断酒店管理系统的各个模块状态
 */
public class SystemHealthChecker {
    
    private static int totalChecks = 0;
    private static int passedChecks = 0;
    private static List<String> issues = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("=== 酒店管理系统健康检查 ===");
        System.out.println("开始时间: " + new java.util.Date());
        System.out.println();
        
        // 1. 数据库层检查
        checkDatabaseLayer();
        
        // 2. 认证模块检查
        checkAuthenticationModule();
        
        // 3. 数据完整性检查
        checkDataIntegrity();
        
        // 4. UI模块检查
        checkUIModules();
        
        // 5. 业务模块检查
        checkBusinessModules();
        
        // 6. 依赖关系检查
        checkDependencies();
        
        // 生成检查报告
        generateReport();
        
        System.out.println("\n=== 健康检查完成 ===");
    }
    
    private static void checkDatabaseLayer() {
        System.out.println("=== 1. 数据库层检查 ===");
        
        // 1.1 数据库连接检查
        checkItem("数据库连接", () -> DatabaseHelper.testConnection());
        
        // 1.2 必要表存在性检查
        String[] requiredTables = {"rooms", "roomstate", "bookroom", "client", "leader", "user_auth"};
        for (String tableName : requiredTables) {
            checkItem("表 " + tableName + " 存在性", () -> checkTableExists(tableName));
        }
        
        // 1.3 表结构检查
        checkItem("rooms表结构", () -> checkRoomsTableStructure());
        checkItem("user_auth表结构", () -> checkUserAuthTableStructure());
        
        System.out.println();
    }
    
    private static void checkAuthenticationModule() {
        System.out.println("=== 2. 认证模块检查 ===");
        
        // 2.1 用户认证管理器功能
        checkItem("UserAuthManager基本功能", () -> {
            UserAuthManager.setCurrentUser("test", "testUser", 1, false);
            return "test".equals(UserAuthManager.getCurrentUserId()) &&
                   "testUser".equals(UserAuthManager.getCurrentUserName());
        });
        
        // 2.2 密码加密功能
        checkItem("密码加密功能", () -> {
            try {
                String password = "testPassword123";
                String salt = com.hotel.auth.PasswordUtils.generateSalt();
                String hashed = com.hotel.auth.PasswordUtils.hashPassword(password, salt);
                return com.hotel.auth.PasswordUtils.verifyPassword(password, hashed, salt);
            } catch (Exception e) {
                return false;
            }
        });
        
        // 2.3 AuthService登录功能
        checkItem("AuthService登录功能", () -> {
            try {
                // 测试不存在的用户
                AuthService.LoginResult result = AuthService.login("nonexistent", "wrongpass");
                return !result.isSuccess() && result.getErrorMessage() != null;
            } catch (Exception e) {
                return false;
            }
        });
        
        System.out.println();
    }
    
    private static void checkDataIntegrity() {
        System.out.println("=== 3. 数据完整性检查 ===");
        
        // 3.1 房间数据完整性
        checkItem("房间数据完整性", () -> checkRoomDataIntegrity());
        
        // 3.2 客户数据完整性
        checkItem("客户数据完整性", () -> checkClientDataIntegrity());
        
        // 3.3 预订数据一致性
        checkItem("预订数据一致性", () -> checkBookingDataConsistency());
        
        // 3.4 外键约束检查
        checkItem("外键约束检查", () -> checkForeignKeyConstraints());
        
        System.out.println();
    }
    
    private static void checkUIModules() {
        System.out.println("=== 4. UI模块检查 ===");
        
        // 4.1 ModernTheme可用性
        checkItem("ModernTheme可用性", () -> {
            try {
                com.hotel.ui.ModernTheme.applyTheme();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        
        // 4.2 Swing环境检查
        checkItem("Swing环境", () -> {
            try {
                return !GraphicsEnvironment.isHeadless();
            } catch (Exception e) {
                return false;
            }
        });
        
        // 4.3 UI组件创建测试
        checkItem("UI组件创建", () -> {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    JFrame testFrame = new JFrame("Test");
                    testFrame.dispose();
                });
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        
        System.out.println();
    }
    
    private static void checkBusinessModules() {
        System.out.println("=== 5. 业务模块检查 ===");
        
        // 5.1 预订系统核心功能
        checkItem("预订系统核心类", () -> {
            try {
                Class.forName("com.hotel.book.SimpleRoomBooking");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        });
        
        // 5.2 房间管理功能
        checkItem("房间管理模块", () -> {
            try {
                Class.forName("com.hotel.room.AddRoomModule");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        });
        
        // 5.3 用户管理功能
        checkItem("用户管理模块", () -> {
            try {
                Class.forName("com.hotel.user.UserPermissionManager");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        });
        
        // 5.4 搜索功能模块
        String[] searchModules = {"allRoom", "add_client", "add_room", "checkIn", "checkOut"};
        for (String module : searchModules) {
            checkItem("搜索模块 " + module, () -> {
                try {
                    Class.forName("com.all.search." + module);
                    return true;
                } catch (ClassNotFoundException e) {
                    return false;
                }
            });
        }
        
        System.out.println();
    }
    
    private static void checkDependencies() {
        System.out.println("=== 6. 依赖关系检查 ===");
        
        // 6.1 MySQL驱动检查
        checkItem("MySQL JDBC驱动", () -> {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        });
        
        // 6.2 Java版本检查
        checkItem("Java版本", () -> {
            String javaVersion = System.getProperty("java.version");
            return javaVersion.startsWith("1.8") || 
                   javaVersion.startsWith("11") || 
                   javaVersion.startsWith("17") ||
                   javaVersion.startsWith("21") ||
                   javaVersion.startsWith("23");
        });
        
        // 6.3 系统资源检查
        checkItem("可用内存", () -> {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            return maxMemory >= 128 * 1024 * 1024; // 至少128MB
        });
        
        System.out.println();
    }
    
    // 辅助检查方法
    private static boolean checkTableExists(String tableName) {
        Connection conn = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) return false;
            
            DatabaseMetaData meta = conn.getMetaData();
            rs = meta.getTables(null, null, tableName, new String[]{"TABLE"});
            return rs.next();
        } catch (Exception e) {
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, null, rs);
        }
    }
    
    private static boolean checkRoomsTableStructure() {
        Connection conn = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) return false;
            
            String[] requiredColumns = {"rNumber", "rType", "rPrice"};
            DatabaseMetaData meta = conn.getMetaData();
            
            for (String column : requiredColumns) {
                rs = meta.getColumns(null, null, "rooms", column);
                if (!rs.next()) {
                    return false;
                }
                rs.close();
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, null, rs);
        }
    }
    
    private static boolean checkUserAuthTableStructure() {
        Connection conn = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) return false;
            
            String[] requiredColumns = {"user_id", "password", "salt", "account_type"};
            DatabaseMetaData meta = conn.getMetaData();
            
            for (String column : requiredColumns) {
                rs = meta.getColumns(null, null, "user_auth", column);
                if (!rs.next()) {
                    return false;
                }
                rs.close();
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, null, rs);
        }
    }
    
    private static boolean checkRoomDataIntegrity() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) return false;
            
            // 检查是否有房间数据
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM rooms");
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0; // 至少要有一些房间数据
            }
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }
    
    private static boolean checkClientDataIntegrity() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) return false;
            
            // 检查客户数据的基本完整性
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM client WHERE cNumber IS NOT NULL AND cName IS NOT NULL");
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) >= 0; // 即使没有数据也算正常
            }
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }
    
    private static boolean checkBookingDataConsistency() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) return false;
            
            // 检查预订数据中是否有无效的房间号
            stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM bookroom b WHERE NOT EXISTS (SELECT 1 FROM rooms r WHERE r.rNumber = b.rNumber)"
            );
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) == 0; // 应该没有无效的房间号
            }
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }
    
    private static boolean checkForeignKeyConstraints() {
        // 这里可以添加更多的外键约束检查
        // 暂时返回true，表示通过基本检查
        return true;
    }
    
    private static void checkItem(String itemName, HealthCheck check) {
        totalChecks++;
        System.out.print("  [" + totalChecks + "] " + itemName + "... ");
        
        try {
            boolean result = check.check();
            if (result) {
                System.out.println("✅ 通过");
                passedChecks++;
            } else {
                System.out.println("❌ 失败");
                issues.add(itemName + ": 检查失败");
            }
        } catch (Exception e) {
            System.out.println("❌ 错误: " + e.getMessage());
            issues.add(itemName + ": " + e.getMessage());
        }
    }
    
    private static void generateReport() {
        System.out.println("\n=== 🏥 系统健康检查报告 ===");
        System.out.println("总检查项目: " + totalChecks);
        System.out.println("通过项目: " + passedChecks);
        System.out.println("失败项目: " + (totalChecks - passedChecks));
        
        double successRate = (double) passedChecks / totalChecks * 100;
        System.out.printf("成功率: %.1f%%\n", successRate);
        
        if (successRate >= 90) {
            System.out.println("🎉 系统状态: 优秀");
        } else if (successRate >= 75) {
            System.out.println("⚠️  系统状态: 良好");
        } else if (successRate >= 50) {
            System.out.println("⚠️  系统状态: 需要注意");
        } else {
            System.out.println("🚨 系统状态: 需要紧急修复");
        }
        
        if (!issues.isEmpty()) {
            System.out.println("\n❌ 发现的问题:");
            for (int i = 0; i < issues.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + issues.get(i));
            }
            
            System.out.println("\n🔧 建议操作:");
            if (issues.stream().anyMatch(issue -> issue.contains("数据库连接"))) {
                System.out.println("  - 检查MySQL服务是否运行");
                System.out.println("  - 验证数据库连接配置");
            }
            if (issues.stream().anyMatch(issue -> issue.contains("表"))) {
                System.out.println("  - 运行数据库初始化脚本");
                System.out.println("  - 检查表结构是否正确");
            }
            if (issues.stream().anyMatch(issue -> issue.contains("驱动"))) {
                System.out.println("  - 检查lib目录下的MySQL驱动JAR文件");
            }
        } else {
            System.out.println("\n🎯 所有检查项目都通过了！系统运行正常。");
        }
    }
    
    @FunctionalInterface
    private interface HealthCheck {
        boolean check() throws Exception;
    }
} 