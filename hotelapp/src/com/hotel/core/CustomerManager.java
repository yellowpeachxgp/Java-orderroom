package com.hotel.core;

import com.database.helper.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 统一客户管理器
 * 整合所有客户相关的CRUD操作和查询功能
 */
public class CustomerManager {
    
    private static final Logger LOGGER = Logger.getLogger(CustomerManager.class.getName());
    private static CustomerManager instance;
    
    private CustomerManager() {}
    
    public static CustomerManager getInstance() {
        if (instance == null) {
            synchronized (CustomerManager.class) {
                if (instance == null) {
                    instance = new CustomerManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 添加客户
     * @param customer 客户信息
     * @return 操作结果
     */
    public OperationResult addCustomer(Customer customer) {
        OperationResult result = new OperationResult();
        
        if (customer == null) {
            result.setErrorMessage("客户信息不能为空");
            return result;
        }
        
        if (customer.getcNumber() == null || customer.getcNumber().trim().isEmpty()) {
            result.setErrorMessage("客户编号不能为空");
            return result;
        }
        
        if (customer.getcName() == null || customer.getcName().trim().isEmpty()) {
            result.setErrorMessage("客户姓名不能为空");
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
            
            // 检查客户是否已存在
            if (isCustomerExists(conn, customer.getcNumber())) {
                result.setErrorMessage("客户编号 " + customer.getcNumber() + " 已存在");
                return result;
            }
            
            // 插入客户信息
            String insertSql = "INSERT INTO client (cNumber, cName, cAge, cSex, cTel) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(insertSql);
            stmt.setString(1, customer.getcNumber());
            stmt.setString(2, customer.getcName());
            stmt.setString(3, customer.getcAge());
            stmt.setString(4, customer.getcSex());
            stmt.setString(5, customer.getcTel());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                result.setSuccess(true);
                result.setMessage("客户添加成功");
                LOGGER.info("客户添加成功: " + customer.getcNumber());
            } else {
                result.setErrorMessage("客户添加失败");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "添加客户时发生数据库错误", e);
            result.setErrorMessage("添加客户时发生错误: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
        
        return result;
    }
    
    /**
     * 更新客户信息
     * @param customer 客户信息
     * @return 操作结果
     */
    public OperationResult updateCustomer(Customer customer) {
        OperationResult result = new OperationResult();
        
        if (customer == null || customer.getcNumber() == null) {
            result.setErrorMessage("客户信息不完整");
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
            
            // 检查客户是否存在
            if (!isCustomerExists(conn, customer.getcNumber())) {
                result.setErrorMessage("客户编号 " + customer.getcNumber() + " 不存在");
                return result;
            }
            
            // 更新客户信息
            String updateSql = "UPDATE client SET cName = ?, cAge = ?, cSex = ?, cTel = ? WHERE cNumber = ?";
            stmt = conn.prepareStatement(updateSql);
            stmt.setString(1, customer.getcName());
            stmt.setString(2, customer.getcAge());
            stmt.setString(3, customer.getcSex());
            stmt.setString(4, customer.getcTel());
            stmt.setString(5, customer.getcNumber());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                result.setSuccess(true);
                result.setMessage("客户信息更新成功");
                LOGGER.info("客户信息更新成功: " + customer.getcNumber());
            } else {
                result.setErrorMessage("客户信息更新失败");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "更新客户时发生数据库错误", e);
            result.setErrorMessage("更新客户时发生错误: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
        
        return result;
    }
    
    /**
     * 删除客户
     * @param customerNumber 客户编号
     * @return 操作结果
     */
    public OperationResult deleteCustomer(String customerNumber) {
        OperationResult result = new OperationResult();
        
        if (customerNumber == null || customerNumber.trim().isEmpty()) {
            result.setErrorMessage("客户编号不能为空");
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
                // 检查客户是否有未完成的预订
                if (hasActiveBookings(conn, customerNumber)) {
                    result.setErrorMessage("客户有未完成的预订，无法删除");
                    conn.rollback();
                    return result;
                }
                
                // 删除客户认证记录（如果存在）
                String deleteAuthSql = "DELETE FROM user_auth WHERE user_id = ?";
                stmt = conn.prepareStatement(deleteAuthSql);
                stmt.setString(1, customerNumber);
                stmt.executeUpdate();
                stmt.close();
                
                // 删除客户记录
                String deleteCustomerSql = "DELETE FROM client WHERE cNumber = ?";
                stmt = conn.prepareStatement(deleteCustomerSql);
                stmt.setString(1, customerNumber);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    result.setSuccess(true);
                    result.setMessage("客户删除成功");
                    LOGGER.info("客户删除成功: " + customerNumber);
                } else {
                    conn.rollback();
                    result.setErrorMessage("客户不存在或删除失败");
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "删除客户时发生数据库错误", e);
            result.setErrorMessage("删除客户时发生错误: " + e.getMessage());
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
     * 查询所有客户
     * @return 客户列表
     */
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                LOGGER.warning("无法连接到数据库");
                return customers;
            }
            
            String query = "SELECT cNumber, cName, cAge, cSex, cTel FROM client ORDER BY cNumber";
            
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Customer customer = new Customer();
                customer.setcNumber(rs.getString("cNumber"));
                customer.setcName(rs.getString("cName"));
                customer.setcAge(rs.getString("cAge"));
                customer.setcSex(rs.getString("cSex"));
                customer.setcTel(rs.getString("cTel"));
                customers.add(customer);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询客户列表时发生数据库错误", e);
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return customers;
    }
    
    /**
     * 根据客户编号查询客户信息
     * @param customerNumber 客户编号
     * @return 客户信息
     */
    public Customer getCustomerByNumber(String customerNumber) {
        if (customerNumber == null || customerNumber.trim().isEmpty()) {
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
            
            String query = "SELECT cNumber, cName, cAge, cSex, cTel FROM client WHERE cNumber = ?";
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, customerNumber);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Customer customer = new Customer();
                customer.setcNumber(rs.getString("cNumber"));
                customer.setcName(rs.getString("cName"));
                customer.setcAge(rs.getString("cAge"));
                customer.setcSex(rs.getString("cSex"));
                customer.setcTel(rs.getString("cTel"));
                return customer;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询客户信息时发生数据库错误", e);
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return null;
    }
    
    /**
     * 根据姓名搜索客户
     * @param name 客户姓名（支持模糊查询）
     * @return 客户列表
     */
    public List<Customer> searchCustomersByName(String name) {
        List<Customer> customers = new ArrayList<>();
        
        if (name == null || name.trim().isEmpty()) {
            return customers;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return customers;
            }
            
            String query = "SELECT cNumber, cName, cAge, cSex, cTel FROM client WHERE cName LIKE ? ORDER BY cNumber";
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, "%" + name + "%");
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Customer customer = new Customer();
                customer.setcNumber(rs.getString("cNumber"));
                customer.setcName(rs.getString("cName"));
                customer.setcAge(rs.getString("cAge"));
                customer.setcSex(rs.getString("cSex"));
                customer.setcTel(rs.getString("cTel"));
                customers.add(customer);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "搜索客户时发生数据库错误", e);
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return customers;
    }
    
    /**
     * 获取客户统计信息
     * @return 客户统计
     */
    public CustomerStats getCustomerStatistics() {
        CustomerStats stats = new CustomerStats();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return stats;
            }
            
            // 查询客户总数
            String totalQuery = "SELECT COUNT(*) as total FROM client";
            stmt = conn.prepareStatement(totalQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setTotalCustomers(rs.getInt("total"));
            }
            rs.close();
            stmt.close();
            
            // 查询有预订记录的客户数
            String activeQuery = "SELECT COUNT(DISTINCT c.cNumber) as active " +
                                "FROM client c " +
                                "INNER JOIN bookroom b ON c.cNumber = b.cNumber";
            stmt = conn.prepareStatement(activeQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setActiveCustomers(rs.getInt("active"));
            }
            rs.close();
            stmt.close();
            
            // 查询当前有未入住预订的客户数
            String pendingQuery = "SELECT COUNT(DISTINCT b.cNumber) as pending " +
                                 "FROM bookroom b WHERE b.checkin = '未入住'";
            stmt = conn.prepareStatement(pendingQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setPendingCustomers(rs.getInt("pending"));
            }
            rs.close();
            stmt.close();
            
            // 查询当前入住的客户数
            String checkedInQuery = "SELECT COUNT(DISTINCT b.cNumber) as checkedIn " +
                                   "FROM bookroom b WHERE b.checkin = '已入住'";
            stmt = conn.prepareStatement(checkedInQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setCheckedInCustomers(rs.getInt("checkedIn"));
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询客户统计时发生数据库错误", e);
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return stats;
    }
    
    /**
     * 生成下一个客户编号
     * @return 客户编号
     */
    public String generateNextCustomerNumber() {
        return DatabaseHelper.getNextId("client", "cNumber", "C");
    }
    
    /**
     * 检查客户是否存在
     */
    private boolean isCustomerExists(Connection conn, String customerNumber) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String query = "SELECT COUNT(*) FROM client WHERE cNumber = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, customerNumber);
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
     * 检查客户是否有活跃的预订
     */
    private boolean hasActiveBookings(Connection conn, String customerNumber) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String query = "SELECT COUNT(*) FROM bookroom WHERE cNumber = ? AND checkin IN ('未入住', '已入住')";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, customerNumber);
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
     * 客户信息类
     */
    public static class Customer {
        private String cNumber;
        private String cName;
        private String cAge;
        private String cSex;
        private String cTel;
        
        // Constructors
        public Customer() {}
        
        public Customer(String cNumber, String cName, String cAge, String cSex, String cTel) {
            this.cNumber = cNumber;
            this.cName = cName;
            this.cAge = cAge;
            this.cSex = cSex;
            this.cTel = cTel;
        }
        
        // Getters and Setters
        public String getcNumber() { return cNumber; }
        public void setcNumber(String cNumber) { this.cNumber = cNumber; }
        
        public String getcName() { return cName; }
        public void setcName(String cName) { this.cName = cName; }
        
        public String getcAge() { return cAge; }
        public void setcAge(String cAge) { this.cAge = cAge; }
        
        public String getcSex() { return cSex; }
        public void setcSex(String cSex) { this.cSex = cSex; }
        
        public String getcTel() { return cTel; }
        public void setcTel(String cTel) { this.cTel = cTel; }
        
        @Override
        public String toString() {
            return "Customer{" +
                   "cNumber='" + cNumber + '\'' +
                   ", cName='" + cName + '\'' +
                   ", cAge='" + cAge + '\'' +
                   ", cSex='" + cSex + '\'' +
                   ", cTel='" + cTel + '\'' +
                   '}';
        }
    }
    
    /**
     * 客户统计信息类
     */
    public static class CustomerStats {
        private int totalCustomers = 0;
        private int activeCustomers = 0;
        private int pendingCustomers = 0;
        private int checkedInCustomers = 0;
        
        // Getters and Setters
        public int getTotalCustomers() { return totalCustomers; }
        public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }
        
        public int getActiveCustomers() { return activeCustomers; }
        public void setActiveCustomers(int activeCustomers) { this.activeCustomers = activeCustomers; }
        
        public int getPendingCustomers() { return pendingCustomers; }
        public void setPendingCustomers(int pendingCustomers) { this.pendingCustomers = pendingCustomers; }
        
        public int getCheckedInCustomers() { return checkedInCustomers; }
        public void setCheckedInCustomers(int checkedInCustomers) { this.checkedInCustomers = checkedInCustomers; }
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