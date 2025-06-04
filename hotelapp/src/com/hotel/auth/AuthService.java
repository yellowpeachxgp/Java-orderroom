package com.hotel.auth;

import com.database.helper.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 用户认证服务类
 * 处理用户登录、注册、密码管理和权限组分配
 */
public class AuthService {
    
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    
    /**
     * 用户登录
     * @param userId 用户ID
     * @param password 密码
     * @return 登录结果对象
     */
    public static LoginResult login(String userId, String password) {
        LoginResult result = new LoginResult();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                result.setErrorMessage("无法连接到数据库");
                return result;
            }
            
            // 首先检查user_auth表是否存在
            try {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet tables = meta.getTables(null, null, "user_auth", null);
                boolean tableExists = tables.next();
                tables.close();
                
                if (!tableExists) {
                    result.setErrorMessage("user_auth表不存在，需要先创建认证表");
                    return result;
                }
            } catch (SQLException e) {
                result.setErrorMessage("检查user_auth表失败: " + e.getMessage());
                return result;
            }
            
            // 查询用户认证信息
            String query = "SELECT c.cName, ua.password, ua.salt, ua.account_type, ua.status " +
                          "FROM user_auth ua " +
                          "JOIN client c ON ua.user_id = c.cNumber " +
                          "WHERE ua.user_id = ?";
            
            stmt = conn.prepareStatement(query);
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
                    // 密码正确，更新登录时间
                    updateLastLoginTime(conn, userId);
                    
                    // 获取用户权限组
                    List<Integer> groupIds = getUserGroups(conn, userId);
                    
                    // 设置登录成功
                    result.setSuccess(true);
                    result.setUserId(userId);
                    result.setUserName(userName);
                    result.setAccountType(accountType);
                    result.setGroupIds(groupIds);
                    
                    return result;
                } else {
                    result.setErrorMessage("密码错误");
                    return result;
                }
            } else {
                // 尝试从客户表中查找用户（可能是第一次登录，使用默认密码）
                query = "SELECT cName FROM client WHERE cNumber = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, userId);
                rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String userName = rs.getString("cName");
                    
                    // 用户存在但还没有设置密码，使用默认密码"password"登录
                    if ("password".equals(password)) {
                        // 创建用户认证记录
                        createUserAuth(conn, userId, password, UserAuthManager.USER_TYPE_CLIENT);
                        
                        // 设置登录成功
                        result.setSuccess(true);
                        result.setUserId(userId);
                        result.setUserName(userName);
                        result.setAccountType(UserAuthManager.USER_TYPE_CLIENT);
                        result.setFirstLogin(true);
                        
                        return result;
                    } else {
                        result.setErrorMessage("初次登录请使用默认密码：password");
                        return result;
                    }
                } else {
                    result.setErrorMessage("用户不存在");
                    return result;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "登录时发生数据库错误", e);
            result.setErrorMessage("登录时发生错误: " + e.getMessage());
            return result;
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * 创建用户认证记录
     * @param conn 数据库连接
     * @param userId 用户ID
     * @param password 明文密码
     * @param accountType 账户类型
     * @return 是否成功
     */
    private static boolean createUserAuth(Connection conn, String userId, String password, int accountType) {
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
            
            int result = stmt.executeUpdate();
            
            // 如果是管理员，添加到管理员权限组
            if (accountType == UserAuthManager.USER_TYPE_LEADER && result > 0) {
                addUserToGroup(conn, userId, 2); // 2是管理员权限组
            } else if (result > 0) {
                addUserToGroup(conn, userId, 1); // 1是普通用户权限组
            }
            
            return result > 0;
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
     * 更新用户密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 更新结果
     */
    public static boolean updatePassword(String userId, String oldPassword, String newPassword) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return false;
            }
            
            // 查询当前密码
            String query = "SELECT password, salt FROM user_auth WHERE user_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String salt = rs.getString("salt");
                
                // 验证旧密码
                if (PasswordUtils.verifyPassword(oldPassword, storedPassword, salt)) {
                    // 生成新的盐值和密码
                    String newSalt = PasswordUtils.generateSalt();
                    String newHashedPassword = PasswordUtils.hashPassword(newPassword, newSalt);
                    
                    // 更新密码
                    query = "UPDATE user_auth SET password = ?, salt = ? WHERE user_id = ?";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, newHashedPassword);
                    stmt.setString(2, newSalt);
                    stmt.setString(3, userId);
                    
                    return stmt.executeUpdate() > 0;
                }
            }
            
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "更新密码失败", e);
            return false;
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * 重置用户密码
     * @param userId 用户ID
     * @return 重置后的密码
     */
    public static String resetPassword(String userId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return null;
            }
            
            // 生成新的随机密码
            String newPassword = PasswordUtils.generateRandomPassword(10);
            String salt = PasswordUtils.generateSalt();
            String hashedPassword = PasswordUtils.hashPassword(newPassword, salt);
            
            // 更新密码
            String query = "UPDATE user_auth SET password = ?, salt = ? WHERE user_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, hashedPassword);
            stmt.setString(2, salt);
            stmt.setString(3, userId);
            
            if (stmt.executeUpdate() > 0) {
                return newPassword;
            }
            
            return null;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "重置密码失败", e);
            return null;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * 更新最后登录时间
     * @param conn 数据库连接
     * @param userId 用户ID
     */
    private static void updateLastLoginTime(Connection conn, String userId) {
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
     * 获取用户所属权限组
     * @param conn 数据库连接
     * @param userId 用户ID
     * @return 权限组ID列表
     */
    private static List<Integer> getUserGroups(Connection conn, String userId) {
        List<Integer> groupIds = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String query = "SELECT group_id FROM user_group WHERE user_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                groupIds.add(rs.getInt("group_id"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "获取用户权限组失败", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "关闭ResultSet失败", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "关闭PreparedStatement失败", e);
                }
            }
        }
        
        return groupIds;
    }
    
    /**
     * 添加用户到权限组
     * @param conn 数据库连接
     * @param userId 用户ID
     * @param groupId 权限组ID
     * @return 是否成功
     */
    private static boolean addUserToGroup(Connection conn, String userId, int groupId) {
        PreparedStatement stmt = null;
        
        try {
            String query = "INSERT INTO user_group (user_id, group_id) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userId);
            stmt.setInt(2, groupId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "添加用户到权限组失败", e);
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
     * 公开方法：添加用户到权限组
     * @param userId 用户ID
     * @param groupId 权限组ID
     * @return 是否成功
     */
    public static boolean assignUserToGroup(String userId, int groupId) {
        Connection conn = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return false;
            }
            
            return addUserToGroup(conn, userId, groupId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "分配用户到权限组失败", e);
            return false;
        } finally {
            closeResources(conn, null, null);
        }
    }
    
    /**
     * 从权限组中移除用户
     * @param userId 用户ID
     * @param groupId 权限组ID
     * @return 是否成功
     */
    public static boolean removeUserFromGroup(String userId, int groupId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return false;
            }
            
            String query = "DELETE FROM user_group WHERE user_id = ? AND group_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userId);
            stmt.setInt(2, groupId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "从权限组移除用户失败", e);
            return false;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * 设置用户账户类型
     * @param userId 用户ID
     * @param accountType 账户类型
     * @return 是否成功
     */
    public static boolean setUserAccountType(String userId, int accountType) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return false;
            }
            
            String query = "UPDATE user_auth SET account_type = ? WHERE user_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, accountType);
            stmt.setString(2, userId);
            
            boolean result = stmt.executeUpdate() > 0;
            
            // 如果设置为管理员，确保添加到管理员权限组
            if (result && accountType == UserAuthManager.USER_TYPE_LEADER) {
                addUserToGroup(conn, userId, 2); // 管理员权限组ID为2
            }
            
            return result;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "设置用户账户类型失败", e);
            return false;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * 获取所有权限组
     * @return 权限组列表
     */
    public static List<PermissionGroup> getAllGroups() {
        List<PermissionGroup> groups = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return groups;
            }
            
            String query = "SELECT group_id, group_name, description FROM permission_group ORDER BY group_id";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                PermissionGroup group = new PermissionGroup();
                group.setGroupId(rs.getInt("group_id"));
                group.setGroupName(rs.getString("group_name"));
                group.setDescription(rs.getString("description"));
                groups.add(group);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "获取所有权限组失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return groups;
    }
    
    /**
     * 获取用户所属的权限组
     * @param userId 用户ID
     * @return 权限组列表
     */
    public static List<PermissionGroup> getUserGroups(String userId) {
        List<PermissionGroup> groups = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return groups;
            }
            
            String query = "SELECT pg.group_id, pg.group_name, pg.description " +
                          "FROM permission_group pg " +
                          "JOIN user_group ug ON pg.group_id = ug.group_id " +
                          "WHERE ug.user_id = ?";
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                PermissionGroup group = new PermissionGroup();
                group.setGroupId(rs.getInt("group_id"));
                group.setGroupName(rs.getString("group_name"));
                group.setDescription(rs.getString("description"));
                groups.add(group);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "获取用户权限组失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return groups;
    }
    
    /**
     * 检查用户是否具有管理员权限
     * @param userId 用户ID
     * @return 是否具有管理员权限
     */
    public static boolean isUserAdmin(String userId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return false;
            }
            
            String query = "SELECT account_type FROM user_auth WHERE user_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("account_type") == UserAuthManager.USER_TYPE_LEADER;
            }
            
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "检查用户权限失败", e);
            return false;
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * 关闭数据库资源
     * @param conn 数据库连接
     * @param stmt PreparedStatement
     * @param rs ResultSet
     */
    private static void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "关闭ResultSet失败", e);
            }
        }
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "关闭PreparedStatement失败", e);
            }
        }
        
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "关闭Connection失败", e);
            }
        }
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
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
        
        public String getUserName() {
            return userName;
        }
        
        public void setUserName(String userName) {
            this.userName = userName;
        }
        
        public int getAccountType() {
            return accountType;
        }
        
        public void setAccountType(int accountType) {
            this.accountType = accountType;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public boolean isFirstLogin() {
            return firstLogin;
        }
        
        public void setFirstLogin(boolean firstLogin) {
            this.firstLogin = firstLogin;
        }
        
        public List<Integer> getGroupIds() {
            return groupIds;
        }
        
        public void setGroupIds(List<Integer> groupIds) {
            this.groupIds = groupIds;
        }
    }
    
    /**
     * 权限组类
     */
    public static class PermissionGroup {
        private int groupId;
        private String groupName;
        private String description;
        
        public int getGroupId() {
            return groupId;
        }
        
        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }
        
        public String getGroupName() {
            return groupName;
        }
        
        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
            return groupName;
        }
    }
} 