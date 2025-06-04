package com.hotel.core;

import com.database.helper.DatabaseHelper;
import com.hotel.auth.PasswordUtils;
import com.hotel.auth.UserAuthManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 统一认证管理器
 * 整合所有登录、用户认证相关功能
 */
public class AuthenticationManager {
    
    private static final Logger LOGGER = Logger.getLogger(AuthenticationManager.class.getName());
    private static AuthenticationManager instance;
    private static volatile String currentUserId;
    private static volatile String currentUserName;
    private static volatile int currentUserType;
    
    private AuthenticationManager() {}
    
    public static AuthenticationManager getInstance() {
        if (instance == null) {
            synchronized (AuthenticationManager.class) {
                if (instance == null) {
                    instance = new AuthenticationManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 用户登录
     * @param userId 用户ID
     * @param password 密码
     * @param userType 用户类型 (1=普通用户, 2=管理员)
     * @return 登录结果
     */
    public LoginResult login(String userId, String password, int userType) {
        LoginResult result = new LoginResult();
        
        if (userId == null || userId.trim().isEmpty()) {
            result.setErrorMessage("用户ID不能为空");
            return result;
        }
        
        if (password == null || password.trim().isEmpty()) {
            result.setErrorMessage("密码不能为空");
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
            
            // 根据用户类型选择不同的认证逻辑
            if (userType == UserAuthManager.USER_TYPE_LEADER) {
                result = authenticateLeader(conn, userId, password);
            } else {
                result = authenticateClient(conn, userId, password);
            }
            
            // 登录成功后设置当前用户信息
            if (result.isSuccess()) {
                setCurrentUser(result.getUserId(), result.getUserName(), result.getAccountType());
                updateLastLoginTime(conn, result.getUserId());
                LOGGER.info("用户登录成功: " + result.getUserId());
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "登录时发生数据库错误", e);
            result.setErrorMessage("登录时发生错误: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return result;
    }
    
    /**
     * 认证普通客户
     */
    private LoginResult authenticateClient(Connection conn, String userId, String password) throws SQLException {
        LoginResult result = new LoginResult();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // 首先查询user_auth表
            String authQuery = "SELECT c.cName, ua.password, ua.salt, ua.account_type, ua.status " +
                              "FROM user_auth ua " +
                              "JOIN client c ON ua.user_id = c.cNumber " +
                              "WHERE ua.user_id = ?";
            
            stmt = conn.prepareStatement(authQuery);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String salt = rs.getString("salt");
                String userName = rs.getString("cName");
                int accountType = rs.getInt("account_type");
                int status = rs.getInt("status");
                
                // 检查账户状态
                if (status != 1) {
                    result.setErrorMessage("账户已被禁用");
                    return result;
                }
                
                // 验证密码
                if (PasswordUtils.verifyPassword(password, storedPassword, salt)) {
                    result.setSuccess(true);
                    result.setUserId(userId);
                    result.setUserName(userName);
                    result.setAccountType(accountType);
                    return result;
                } else {
                    result.setErrorMessage("密码错误");
                    return result;
                }
            } else {
                // 尝试从客户表中查找用户（可能是第一次登录）
                rs.close();
                stmt.close();
                
                String clientQuery = "SELECT cName FROM client WHERE cNumber = ?";
                stmt = conn.prepareStatement(clientQuery);
                stmt.setString(1, userId);
                rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String userName = rs.getString("cName");
                    
                    // 用户存在但还没有设置密码，使用默认密码"password"登录
                    if ("password".equals(password)) {
                        // 创建用户认证记录
                        if (createUserAuth(conn, userId, password, UserAuthManager.USER_TYPE_CLIENT)) {
                            result.setSuccess(true);
                            result.setUserId(userId);
                            result.setUserName(userName);
                            result.setAccountType(UserAuthManager.USER_TYPE_CLIENT);
                            result.setFirstLogin(true);
                            return result;
                        } else {
                            result.setErrorMessage("创建用户认证记录失败");
                            return result;
                        }
                    } else {
                        result.setErrorMessage("初次登录请使用默认密码：password");
                        return result;
                    }
                } else {
                    result.setErrorMessage("用户不存在");
                    return result;
                }
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }
    
    /**
     * 认证管理员
     */
    private LoginResult authenticateLeader(Connection conn, String userId, String password) throws SQLException {
        LoginResult result = new LoginResult();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // 首先查询user_auth表中的管理员
            String authQuery = "SELECT l.lName, ua.password, ua.salt, ua.account_type, ua.status " +
                              "FROM user_auth ua " +
                              "JOIN leader l ON ua.user_id = l.lNumber " +
                              "WHERE ua.user_id = ? AND ua.account_type = ?";
            
            stmt = conn.prepareStatement(authQuery);
            stmt.setString(1, userId);
            stmt.setInt(2, UserAuthManager.USER_TYPE_LEADER);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String salt = rs.getString("salt");
                String userName = rs.getString("lName");
                int status = rs.getInt("status");
                
                // 检查账户状态
                if (status != 1) {
                    result.setErrorMessage("管理员账户已被禁用");
                    return result;
                }
                
                // 验证密码
                if (PasswordUtils.verifyPassword(password, storedPassword, salt)) {
                    result.setSuccess(true);
                    result.setUserId(userId);
                    result.setUserName(userName);
                    result.setAccountType(UserAuthManager.USER_TYPE_LEADER);
                    return result;
                } else {
                    result.setErrorMessage("管理员密码错误");
                    return result;
                }
            } else {
                // 尝试从管理员表中查找用户（可能是第一次登录）
                rs.close();
                stmt.close();
                
                String leaderQuery = "SELECT lName FROM leader WHERE lNumber = ?";
                stmt = conn.prepareStatement(leaderQuery);
                stmt.setString(1, userId);
                rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String userName = rs.getString("lName");
                    
                    // 管理员第一次登录，使用默认密码"admin"
                    if ("admin".equals(password)) {
                        // 创建管理员认证记录
                        if (createUserAuth(conn, userId, password, UserAuthManager.USER_TYPE_LEADER)) {
                            result.setSuccess(true);
                            result.setUserId(userId);
                            result.setUserName(userName);
                            result.setAccountType(UserAuthManager.USER_TYPE_LEADER);
                            result.setFirstLogin(true);
                            return result;
                        } else {
                            result.setErrorMessage("创建管理员认证记录失败");
                            return result;
                        }
                    } else {
                        result.setErrorMessage("管理员初次登录请使用默认密码：admin");
                        return result;
                    }
                } else {
                    result.setErrorMessage("管理员账户不存在");
                    return result;
                }
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }
    
    /**
     * 创建用户认证记录
     */
    private boolean createUserAuth(Connection conn, String userId, String password, int accountType) {
        PreparedStatement stmt = null;
        
        try {
            // 生成盐值
            String salt = PasswordUtils.generateSalt();
            
            // 加密密码
            String hashedPassword = PasswordUtils.hashPassword(password, salt);
            
            // 插入认证记录
            String query = "INSERT INTO user_auth (user_id, password, salt, account_type, last_login) " +
                          "VALUES (?, ?, ?, ?, NOW())";
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userId);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, salt);
            stmt.setInt(4, accountType);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "创建用户认证记录失败", e);
            return false;
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
     * 更新最后登录时间
     */
    private void updateLastLoginTime(Connection conn, String userId) {
        PreparedStatement stmt = null;
        try {
            String query = "UPDATE user_auth SET last_login = NOW() WHERE user_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "更新最后登录时间失败", e);
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
     * 设置当前用户信息
     */
    public static void setCurrentUser(String userId, String userName, int userType) {
        currentUserId = userId;
        currentUserName = userName;
        currentUserType = userType;
    }
    
    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        return currentUserId;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUserName() {
        return currentUserName;
    }
    
    /**
     * 获取当前用户类型
     */
    public static int getCurrentUserType() {
        return currentUserType;
    }
    
    /**
     * 用户注销
     */
    public void logout() {
        currentUserId = null;
        currentUserName = null;
        currentUserType = 0;
        LOGGER.info("用户已注销");
    }
    
    /**
     * 检查用户是否已登录
     */
    public static boolean isUserLoggedIn() {
        return currentUserId != null && !currentUserId.trim().isEmpty();
    }
    
    /**
     * 检查当前用户是否为管理员
     */
    public static boolean isCurrentUserAdmin() {
        return currentUserType == UserAuthManager.USER_TYPE_LEADER;
    }
    
    /**
     * 登录结果类
     */
    public static class LoginResult {
        private boolean success = false;
        private String userId;
        private String userName;
        private int accountType;
        private String errorMessage;
        private boolean firstLogin = false;
        private List<Integer> groupIds = new ArrayList<>();
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public int getAccountType() { return accountType; }
        public void setAccountType(int accountType) { this.accountType = accountType; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public boolean isFirstLogin() { return firstLogin; }
        public void setFirstLogin(boolean firstLogin) { this.firstLogin = firstLogin; }
        
        public List<Integer> getGroupIds() { return groupIds; }
        public void setGroupIds(List<Integer> groupIds) { this.groupIds = groupIds; }
    }
} 