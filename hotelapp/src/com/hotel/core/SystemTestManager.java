package com.hotel.core;

import com.database.helper.DatabaseHelper;
import com.hotel.auth.UserAuthManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

/**
 * 统一系统测试管理器
 * 集成所有模块的测试功能，替代分散的测试文件
 */
public class SystemTestManager {
    
    private static final Logger LOGGER = Logger.getLogger(SystemTestManager.class.getName());
    private static SystemTestManager instance;
    
    private SystemTestManager() {}
    
    public static SystemTestManager getInstance() {
        if (instance == null) {
            synchronized (SystemTestManager.class) {
                if (instance == null) {
                    instance = new SystemTestManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 执行完整的系统测试
     * @return 测试结果
     */
    public TestResult runFullSystemTest() {
        TestResult result = new TestResult();
        
        System.out.println("=== 开始系统完整测试 ===");
        
        // 1. 测试数据库连接
        boolean dbTest = testDatabaseConnection();
        result.setDatabaseConnectionTest(dbTest);
        
        if (!dbTest) {
            result.setOverallSuccess(false);
            result.setErrorMessage("数据库连接测试失败，无法继续后续测试");
            return result;
        }
        
        // 2. 测试认证模块
        boolean authTest = testAuthenticationModule();
        result.setAuthenticationTest(authTest);
        
        // 3. 测试房间管理模块
        boolean roomTest = testRoomManagement();
        result.setRoomManagementTest(roomTest);
        
        // 4. 测试客户管理模块
        boolean customerTest = testCustomerManagement();
        result.setCustomerManagementTest(customerTest);
        
        // 5. 测试预订管理模块
        boolean bookingTest = testBookingManagement();
        result.setBookingManagementTest(bookingTest);
        
        // 6. 计算总体测试结果
        boolean overallSuccess = dbTest && authTest && roomTest && customerTest && bookingTest;
        result.setOverallSuccess(overallSuccess);
        
        System.out.println("=== 系统测试完成 ===");
        System.out.println("总体结果: " + (overallSuccess ? "通过" : "失败"));
        
        return result;
    }
    
    /**
     * 测试数据库连接
     */
    public boolean testDatabaseConnection() {
        System.out.println("\n--- 测试数据库连接 ---");
        
        try {
            boolean connected = DatabaseHelper.testConnection();
            if (connected) {
                System.out.println("✅ 数据库连接正常");
                
                // 进一步测试查询功能
                Connection conn = DatabaseHelper.getConnection();
                if (conn != null) {
                    PreparedStatement stmt = conn.prepareStatement("SELECT 1");
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        System.out.println("✅ 数据库查询功能正常");
                    }
                    DatabaseHelper.closeResources(conn, stmt, rs);
                    return true;
                } else {
                    System.out.println("❌ 获取数据库连接失败");
                    return false;
                }
            } else {
                System.out.println("❌ 数据库连接失败");
                return false;
            }
        } catch (Exception e) {
            System.out.println("❌ 数据库测试异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试认证模块
     */
    public boolean testAuthenticationModule() {
        System.out.println("\n--- 测试认证模块 ---");
        
        try {
            AuthenticationManager authManager = AuthenticationManager.getInstance();
            
            // 测试无效登录
            AuthenticationManager.LoginResult invalidResult = authManager.login("", "", UserAuthManager.USER_TYPE_CLIENT);
            if (!invalidResult.isSuccess()) {
                System.out.println("✅ 无效登录正确拒绝");
            } else {
                System.out.println("❌ 无效登录未被拒绝");
                return false;
            }
            
            // 测试不存在用户的登录
            AuthenticationManager.LoginResult nonExistentResult = authManager.login("NONEXISTENT", "password", UserAuthManager.USER_TYPE_CLIENT);
            if (!nonExistentResult.isSuccess()) {
                System.out.println("✅ 不存在用户登录正确拒绝");
            } else {
                System.out.println("❌ 不存在用户登录未被拒绝");
                return false;
            }
            
            System.out.println("✅ 认证模块测试通过");
            return true;
            
        } catch (Exception e) {
            System.out.println("❌ 认证模块测试异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试房间管理模块
     */
    public boolean testRoomManagement() {
        System.out.println("\n--- 测试房间管理模块 ---");
        
        try {
            RoomManager roomManager = RoomManager.getInstance();
            
            // 测试查询所有房间
            List<RoomManager.Room> rooms = roomManager.getAllRooms();
            System.out.println("✅ 查询到 " + rooms.size() + " 个房间");
            
            // 测试查询可预订房间
            List<RoomManager.Room> availableRooms = roomManager.getAvailableRooms();
            System.out.println("✅ 可预订房间: " + availableRooms.size() + " 个");
            
            // 测试房间统计
            RoomManager.RoomStats stats = roomManager.getRoomStatistics();
            System.out.println("✅ 房间统计 - 总数: " + stats.getTotalRooms() + 
                             ", 可用: " + stats.getAvailableRooms() + 
                             ", 已预订: " + stats.getBookedRooms() + 
                             ", 已入住: " + stats.getOccupiedRooms());
            
            // 测试添加无效房间
            RoomManager.Room invalidRoom = new RoomManager.Room();
            RoomManager.OperationResult invalidResult = roomManager.addRoom(invalidRoom);
            if (!invalidResult.isSuccess()) {
                System.out.println("✅ 无效房间添加正确拒绝");
            } else {
                System.out.println("❌ 无效房间添加未被拒绝");
                return false;
            }
            
            System.out.println("✅ 房间管理模块测试通过");
            return true;
            
        } catch (Exception e) {
            System.out.println("❌ 房间管理模块测试异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试客户管理模块
     */
    public boolean testCustomerManagement() {
        System.out.println("\n--- 测试客户管理模块 ---");
        
        try {
            CustomerManager customerManager = CustomerManager.getInstance();
            
            // 测试查询所有客户
            List<CustomerManager.Customer> customers = customerManager.getAllCustomers();
            System.out.println("✅ 查询到 " + customers.size() + " 个客户");
            
            // 测试客户统计
            CustomerManager.CustomerStats stats = customerManager.getCustomerStatistics();
            System.out.println("✅ 客户统计 - 总数: " + stats.getTotalCustomers() + 
                             ", 活跃: " + stats.getActiveCustomers() + 
                             ", 待入住: " + stats.getPendingCustomers() + 
                             ", 已入住: " + stats.getCheckedInCustomers());
            
            // 测试生成客户编号
            String nextId = customerManager.generateNextCustomerNumber();
            System.out.println("✅ 生成下一个客户编号: " + nextId);
            
            // 测试添加无效客户
            CustomerManager.Customer invalidCustomer = new CustomerManager.Customer();
            CustomerManager.OperationResult invalidResult = customerManager.addCustomer(invalidCustomer);
            if (!invalidResult.isSuccess()) {
                System.out.println("✅ 无效客户添加正确拒绝");
            } else {
                System.out.println("❌ 无效客户添加未被拒绝");
                return false;
            }
            
            System.out.println("✅ 客户管理模块测试通过");
            return true;
            
        } catch (Exception e) {
            System.out.println("❌ 客户管理模块测试异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试预订管理模块
     */
    public boolean testBookingManagement() {
        System.out.println("\n--- 测试预订管理模块 ---");
        
        try {
            BookingManager bookingManager = BookingManager.getInstance();
            
            // 测试查询所有预订
            List<BookingManager.Booking> bookings = bookingManager.getAllBookings();
            System.out.println("✅ 查询到 " + bookings.size() + " 个预订记录");
            
            // 测试无效预订创建
            BookingManager.Booking invalidBooking = new BookingManager.Booking();
            BookingManager.BookingResult invalidResult = bookingManager.createBooking(invalidBooking);
            if (!invalidResult.isSuccess()) {
                System.out.println("✅ 无效预订创建正确拒绝");
            } else {
                System.out.println("❌ 无效预订创建未被拒绝");
                return false;
            }
            
            // 测试无效预订号取消
            BookingManager.BookingResult cancelResult = bookingManager.cancelBooking("INVALID_BOOK_NUMBER");
            if (!cancelResult.isSuccess()) {
                System.out.println("✅ 无效预订取消正确拒绝");
            } else {
                System.out.println("❌ 无效预订取消未被拒绝");
                return false;
            }
            
            System.out.println("✅ 预订管理模块测试通过");
            return true;
            
        } catch (Exception e) {
            System.out.println("❌ 预订管理模块测试异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 运行快速健康检查
     * @return 是否通过
     */
    public boolean runQuickHealthCheck() {
        System.out.println("=== 快速健康检查 ===");
        
        // 只检查关键功能
        boolean dbOk = testDatabaseConnection();
        if (!dbOk) {
            System.out.println("❌ 快速检查失败：数据库连接异常");
            return false;
        }
        
        try {
            // 检查核心管理器能否正常实例化
            AuthenticationManager.getInstance();
            RoomManager.getInstance();
            CustomerManager.getInstance();
            BookingManager.getInstance();
            
            System.out.println("✅ 快速健康检查通过");
            return true;
            
        } catch (Exception e) {
            System.out.println("❌ 快速检查失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试结果类
     */
    public static class TestResult {
        private boolean overallSuccess = false;
        private boolean databaseConnectionTest = false;
        private boolean authenticationTest = false;
        private boolean roomManagementTest = false;
        private boolean customerManagementTest = false;
        private boolean bookingManagementTest = false;
        private String errorMessage;
        
        // Getters and Setters
        public boolean isOverallSuccess() { return overallSuccess; }
        public void setOverallSuccess(boolean overallSuccess) { this.overallSuccess = overallSuccess; }
        
        public boolean isDatabaseConnectionTest() { return databaseConnectionTest; }
        public void setDatabaseConnectionTest(boolean databaseConnectionTest) { this.databaseConnectionTest = databaseConnectionTest; }
        
        public boolean isAuthenticationTest() { return authenticationTest; }
        public void setAuthenticationTest(boolean authenticationTest) { this.authenticationTest = authenticationTest; }
        
        public boolean isRoomManagementTest() { return roomManagementTest; }
        public void setRoomManagementTest(boolean roomManagementTest) { this.roomManagementTest = roomManagementTest; }
        
        public boolean isCustomerManagementTest() { return customerManagementTest; }
        public void setCustomerManagementTest(boolean customerManagementTest) { this.customerManagementTest = customerManagementTest; }
        
        public boolean isBookingManagementTest() { return bookingManagementTest; }
        public void setBookingManagementTest(boolean bookingManagementTest) { this.bookingManagementTest = bookingManagementTest; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        @Override
        public String toString() {
            return "TestResult{" +
                   "overallSuccess=" + overallSuccess +
                   ", databaseConnectionTest=" + databaseConnectionTest +
                   ", authenticationTest=" + authenticationTest +
                   ", roomManagementTest=" + roomManagementTest +
                   ", customerManagementTest=" + customerManagementTest +
                   ", bookingManagementTest=" + bookingManagementTest +
                   ", errorMessage='" + errorMessage + '\'' +
                   '}';
        }
    }
    
    /**
     * 主测试方法
     */
    public static void main(String[] args) {
        SystemTestManager testManager = SystemTestManager.getInstance();
        
        if (args.length > 0 && "quick".equals(args[0])) {
            // 快速检查
            testManager.runQuickHealthCheck();
        } else {
            // 完整测试
            TestResult result = testManager.runFullSystemTest();
            System.out.println("\n最终测试结果:");
            System.out.println(result);
        }
    }
} 