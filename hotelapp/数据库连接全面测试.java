import com.database.helper.DatabaseHelper;
import com.all.search.*;
import com.hotel.app.*;
import com.hotel.auth.*;
import com.hotel.book.*;
import com.hotel.room.*;
import com.hotel.system.*;
import com.hotel.user.*;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 数据库连接全面测试工具
 * 检查所有模块的数据库连接功能
 */
public class 数据库连接全面测试 {
    
    private static final Logger LOGGER = Logger.getLogger("数据库连接全面测试".getClass().getName());
    private static int 通过测试数 = 0;
    private static int 失败测试数 = 0;
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("      酒店管理系统 - 数据库连接全面测试");
        System.out.println("===========================================");
        
        // 1. 基础数据库连接测试
        System.out.println("\n📊 1. 基础数据库连接测试");
        测试基础数据库连接();
        
        // 2. 核心表结构测试
        System.out.println("\n📋 2. 数据库表结构测试");
        测试数据库表结构();
        
        // 3. DatabaseHelper功能测试
        System.out.println("\n🔧 3. DatabaseHelper功能测试");
        测试DatabaseHelper功能();
        
        // 4. 各模块数据库连接测试
        System.out.println("\n🏗️ 4. 各模块数据库连接测试");
        测试各模块数据库连接();
        
        // 5. 并发连接测试
        System.out.println("\n⚡ 5. 并发连接测试");
        测试并发数据库连接();
        
        // 6. 长时间连接稳定性测试
        System.out.println("\n⏱️ 6. 连接稳定性测试");
        测试连接稳定性();
        
        // 输出测试结果汇总
        输出测试结果汇总();
    }
    
    /**
     * 测试基础数据库连接
     */
    private static void 测试基础数据库连接() {
        try {
            // 测试基本连接
            Connection conn = DatabaseHelper.getConnection();
            if (conn != null) {
                if (DatabaseHelper.isConnectionValid(conn)) {
                    System.out.println("✅ 基础数据库连接正常");
                    通过测试数++;
                    
                    // 获取数据库信息
                    DatabaseMetaData meta = conn.getMetaData();
                    System.out.println("   数据库产品: " + meta.getDatabaseProductName());
                    System.out.println("   数据库版本: " + meta.getDatabaseProductVersion());
                    System.out.println("   JDBC驱动: " + meta.getDriverName());
                    System.out.println("   驱动版本: " + meta.getDriverVersion());
                } else {
                    System.out.println("❌ 数据库连接无效");
                    失败测试数++;
                }
                DatabaseHelper.closeConnection(conn);
            } else {
                System.out.println("❌ 无法获取数据库连接");
                失败测试数++;
            }
        } catch (Exception e) {
            System.out.println("❌ 基础连接测试失败: " + e.getMessage());
            失败测试数++;
        }
    }
    
    /**
     * 测试数据库表结构
     */
    private static void 测试数据库表结构() {
        String[] 核心表名 = {"rooms", "client", "leader", "bookroom", "roomstate"};
        String[] 认证表名 = {"user_auth", "permission_group", "user_group"};
        
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                System.out.println("❌ 无法连接数据库进行表结构测试");
                失败测试数++;
                return;
            }
            
            DatabaseMetaData meta = conn.getMetaData();
            
            // 检查核心表
            System.out.println("   检查核心业务表:");
            for (String tableName : 核心表名) {
                if (检查表是否存在(meta, tableName)) {
                    System.out.println("   ✅ " + tableName + " 表存在");
                    通过测试数++;
                } else {
                    System.out.println("   ⚠️ " + tableName + " 表不存在（可能需要创建）");
                    // 不算失败，因为DatabaseHelper会自动创建表
                }
            }
            
            // 检查认证表
            System.out.println("   检查认证相关表:");
            for (String tableName : 认证表名) {
                if (检查表是否存在(meta, tableName)) {
                    System.out.println("   ✅ " + tableName + " 表存在");
                    通过测试数++;
                } else {
                    System.out.println("   ⚠️ " + tableName + " 表不存在（可能需要创建）");
                }
            }
            
        } catch (SQLException e) {
            System.out.println("❌ 表结构检查失败: " + e.getMessage());
            失败测试数++;
        } finally {
            DatabaseHelper.closeConnection(conn);
        }
    }
    
    /**
     * 检查表是否存在
     */
    private static boolean 检查表是否存在(DatabaseMetaData meta, String tableName) throws SQLException {
        ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"});
        boolean exists = rs.next();
        rs.close();
        return exists;
    }
    
    /**
     * 测试DatabaseHelper功能
     */
    private static void 测试DatabaseHelper功能() {
        // 测试testConnection方法
        if (DatabaseHelper.testConnection()) {
            System.out.println("✅ DatabaseHelper.testConnection() 正常");
            通过测试数++;
        } else {
            System.out.println("❌ DatabaseHelper.testConnection() 失败");
            失败测试数++;
        }
        
        // 测试连接池功能（多次获取连接）
        System.out.println("   测试多次连接获取:");
        boolean 多次连接成功 = true;
        for (int i = 0; i < 5; i++) {
            Connection conn = DatabaseHelper.getConnection();
            if (conn == null || !DatabaseHelper.isConnectionValid(conn)) {
                多次连接成功 = false;
                System.out.println("   ❌ 第" + (i+1) + "次连接失败");
            }
            DatabaseHelper.closeConnection(conn);
        }
        
        if (多次连接成功) {
            System.out.println("   ✅ 多次连接获取正常");
            通过测试数++;
        } else {
            失败测试数++;
        }
    }
    
    /**
     * 测试各模块数据库连接
     */
    private static void 测试各模块数据库连接() {
        // 测试登录模块
        测试登录模块连接();
        
        // 测试预订模块  
        测试预订模块连接();
        
        // 测试搜索模块
        测试搜索模块连接();
        
        // 测试认证模块
        测试认证模块连接();
    }
    
    /**
     * 测试登录模块连接
     */
    private static void 测试登录模块连接() {
        try {
            // 测试LoginTestFix
            System.out.println("   测试登录模块数据库连接...");
            
            // 模拟LoginPage的数据库查询
            Connection conn = DatabaseHelper.getConnection();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM client");
                stmt.setQueryTimeout(3);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("   ✅ 登录模块数据库连接正常");
                    通过测试数++;
                } else {
                    System.out.println("   ❌ 登录模块查询失败");
                    失败测试数++;
                }
                DatabaseHelper.closeResources(conn, stmt, rs);
            } else {
                System.out.println("   ❌ 登录模块无法获取数据库连接");
                失败测试数++;
            }
        } catch (Exception e) {
            System.out.println("   ❌ 登录模块数据库连接测试失败: " + e.getMessage());
            失败测试数++;
        }
    }
    
    /**
     * 测试预订模块连接
     */
    private static void 测试预订模块连接() {
        try {
            System.out.println("   测试预订模块数据库连接...");
            
            // 模拟RoomBooking的数据库查询
            Connection conn = DatabaseHelper.getConnection();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM rooms");
                stmt.setQueryTimeout(3);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("   ✅ 预订模块数据库连接正常");
                    通过测试数++;
                } else {
                    System.out.println("   ❌ 预订模块查询失败");
                    失败测试数++;
                }
                DatabaseHelper.closeResources(conn, stmt, rs);
            } else {
                System.out.println("   ❌ 预订模块无法获取数据库连接");
                失败测试数++;
            }
        } catch (Exception e) {
            System.out.println("   ❌ 预订模块数据库连接测试失败: " + e.getMessage());
            失败测试数++;
        }
    }
    
    /**
     * 测试搜索模块连接
     */
    private static void 测试搜索模块连接() {
        try {
            System.out.println("   测试搜索模块数据库连接...");
            
            // 模拟allRoom的数据库查询
            Connection conn = DatabaseHelper.getConnection();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM roomstate");
                stmt.setQueryTimeout(3);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("   ✅ 搜索模块数据库连接正常");
                    通过测试数++;
                } else {
                    System.out.println("   ❌ 搜索模块查询失败");
                    失败测试数++;
                }
                DatabaseHelper.closeResources(conn, stmt, rs);
            } else {
                System.out.println("   ❌ 搜索模块无法获取数据库连接");
                失败测试数++;
            }
        } catch (Exception e) {
            System.out.println("   ❌ 搜索模块数据库连接测试失败: " + e.getMessage());
            失败测试数++;
        }
    }
    
    /**
     * 测试认证模块连接
     */
    private static void 测试认证模块连接() {
        try {
            System.out.println("   测试认证模块数据库连接...");
            
            // 模拟AuthService的数据库查询
            Connection conn = DatabaseHelper.getConnection();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM leader");
                stmt.setQueryTimeout(3);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("   ✅ 认证模块数据库连接正常");
                    通过测试数++;
                } else {
                    System.out.println("   ❌ 认证模块查询失败");
                    失败测试数++;
                }
                DatabaseHelper.closeResources(conn, stmt, rs);
            } else {
                System.out.println("   ❌ 认证模块无法获取数据库连接");
                失败测试数++;
            }
        } catch (Exception e) {
            System.out.println("   ❌ 认证模块数据库连接测试失败: " + e.getMessage());
            失败测试数++;
        }
    }
    
    /**
     * 测试并发数据库连接
     */
    private static void 测试并发数据库连接() {
        int 并发数量 = 5;
        System.out.println("   启动" + 并发数量 + "个并发连接测试...");
        
        CompletableFuture<Boolean>[] 并发任务 = new CompletableFuture[并发数量];
        
        for (int i = 0; i < 并发数量; i++) {
            final int 任务编号 = i + 1;
            并发任务[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    Connection conn = DatabaseHelper.getConnection();
                    if (conn != null && DatabaseHelper.isConnectionValid(conn)) {
                        // 执行简单查询
                        PreparedStatement stmt = conn.prepareStatement("SELECT 1");
                        stmt.setQueryTimeout(2);
                        ResultSet rs = stmt.executeQuery();
                        boolean 成功 = rs.next();
                        DatabaseHelper.closeResources(conn, stmt, rs);
                        return 成功;
                    }
                    return false;
                } catch (Exception e) {
                    System.out.println("   并发任务" + 任务编号 + "失败: " + e.getMessage());
                    return false;
                }
            });
        }
        
        try {
            // 等待所有任务完成
            CompletableFuture.allOf(并发任务).get(10, TimeUnit.SECONDS);
            
            int 成功数量 = 0;
            for (CompletableFuture<Boolean> 任务 : 并发任务) {
                if (任务.get()) {
                    成功数量++;
                }
            }
            
            if (成功数量 == 并发数量) {
                System.out.println("   ✅ 并发连接测试通过 (" + 成功数量 + "/" + 并发数量 + ")");
                通过测试数++;
            } else {
                System.out.println("   ❌ 并发连接测试部分失败 (" + 成功数量 + "/" + 并发数量 + ")");
                失败测试数++;
            }
        } catch (Exception e) {
            System.out.println("   ❌ 并发连接测试超时或异常: " + e.getMessage());
            失败测试数++;
        }
    }
    
    /**
     * 测试连接稳定性
     */
    private static void 测试连接稳定性() {
        System.out.println("   进行30秒连接稳定性测试...");
        
        long 开始时间 = System.currentTimeMillis();
        long 结束时间 = 开始时间 + 30000; // 30秒
        int 成功次数 = 0;
        int 失败次数 = 0;
        
        while (System.currentTimeMillis() < 结束时间) {
            try {
                Connection conn = DatabaseHelper.getConnection();
                if (conn != null && DatabaseHelper.isConnectionValid(conn)) {
                    成功次数++;
                } else {
                    失败次数++;
                }
                DatabaseHelper.closeConnection(conn);
                
                // 每500ms测试一次
                Thread.sleep(500);
            } catch (Exception e) {
                失败次数++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
        
        double 成功率 = (double) 成功次数 / (成功次数 + 失败次数) * 100;
        
        if (成功率 >= 95) {
            System.out.printf("   ✅ 连接稳定性测试通过 (成功率: %.1f%%, %d成功/%d失败)\n", 
                            成功率, 成功次数, 失败次数);
            通过测试数++;
        } else {
            System.out.printf("   ❌ 连接稳定性测试失败 (成功率: %.1f%%, %d成功/%d失败)\n", 
                            成功率, 成功次数, 失败次数);
            失败测试数++;
        }
    }
    
    /**
     * 输出测试结果汇总
     */
    private static void 输出测试结果汇总() {
        System.out.println("\n===========================================");
        System.out.println("               测试结果汇总");
        System.out.println("===========================================");
        System.out.println("✅ 通过测试: " + 通过测试数 + " 项");
        System.out.println("❌ 失败测试: " + 失败测试数 + " 项");
        System.out.println("📊 总测试数: " + (通过测试数 + 失败测试数) + " 项");
        
        double 成功率 = (double) 通过测试数 / (通过测试数 + 失败测试数) * 100;
        System.out.printf("🎯 成功率: %.1f%%\n", 成功率);
        
        if (失败测试数 == 0) {
            System.out.println("\n🎉 所有数据库连接测试都通过了！系统数据库连接功能完全正常。");
        } else if (成功率 >= 80) {
            System.out.println("\n⚠️ 大部分测试通过，系统基本正常，但有少量问题需要关注。");
        } else {
            System.out.println("\n🚨 发现较多问题，建议检查数据库配置和网络连接。");
        }
        
        System.out.println("\n建议:");
        System.out.println("- 如有失败项，请检查MySQL服务是否正常运行");
        System.out.println("- 确认数据库名称为'hotel'或对应的数据库名");
        System.out.println("- 检查网络连接是否稳定");
        System.out.println("- 如果是首次运行，DatabaseHelper会自动创建所需表结构");
    }
} 