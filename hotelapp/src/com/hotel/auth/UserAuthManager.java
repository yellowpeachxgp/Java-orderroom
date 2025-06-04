package com.hotel.auth;

/**
 * 用户认证管理器
 * 维护用户登录会话和权限常量
 */
public class UserAuthManager {
    
    // 用户类型常量
    public static final int USER_TYPE_CLIENT = 1;  // 普通用户
    public static final int USER_TYPE_LEADER = 2;  // 管理员
    
    // 当前登录用户信息
    private static String currentUserId;
    private static String currentUserName;
    private static int currentUserType;
    private static boolean isFirstLogin;
    
    /**
     * 设置当前登录用户
     * @param userId 用户ID
     * @param userName 用户名
     * @param userType 用户类型
     * @param firstLogin 是否首次登录
     */
    public static void setCurrentUser(String userId, String userName, int userType, boolean firstLogin) {
        currentUserId = userId;
        currentUserName = userName;
        currentUserType = userType;
        isFirstLogin = firstLogin;
    }
    
    /**
     * 清除当前用户信息（注销）
     */
    public static void clearCurrentUser() {
        currentUserId = null;
        currentUserName = null;
        currentUserType = 0;
        isFirstLogin = false;
    }
    
    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    public static String getCurrentUserId() {
        return currentUserId;
    }
    
    /**
     * 获取当前用户名
     * @return 用户名
     */
    public static String getCurrentUserName() {
        return currentUserName;
    }
    
    /**
     * 获取当前用户类型
     * @return 用户类型
     */
    public static int getCurrentUserType() {
        return currentUserType;
    }
    
    /**
     * 检查当前用户是否为管理员
     * @return 是否为管理员
     */
    public static boolean isCurrentUserAdmin() {
        return currentUserType == USER_TYPE_LEADER;
    }
    
    /**
     * 检查当前用户是否为首次登录
     * @return 是否为首次登录
     */
    public static boolean isFirstLogin() {
        return isFirstLogin;
    }
    
    /**
     * 设置首次登录状态
     * @param firstLogin 是否为首次登录
     */
    public static void setFirstLogin(boolean firstLogin) {
        isFirstLogin = firstLogin;
    }
} 