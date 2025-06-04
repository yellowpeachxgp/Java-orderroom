package com.hotel.auth;

import javax.swing.JOptionPane;

/**
 * 权限验证助手类
 * 用于对需要特定权限的操作进行验证
 */
public class AuthVerifier {
    
    /**
     * 验证用户是否已登录
     * @return 是否已登录
     */
    public static boolean verifyLogin() {
        if (UserAuthManager.getCurrentUserId() == null) {
            JOptionPane.showMessageDialog(null, 
                "请先登录系统", 
                "未登录", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
     * 验证管理员权限
     * @return 是否具有管理员权限
     */
    public static boolean verifyAdminAccess() {
        if (!verifyLogin()) {
            return false;
        }
        
        if (!UserAuthManager.isCurrentUserAdmin()) {
            JOptionPane.showMessageDialog(null, 
                "您没有权限执行此操作", 
                "权限不足", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证用户本人操作权限
     * 确保当前操作的是用户本人的数据
     * @param userId 要操作的用户ID
     * @return 是否有权限
     */
    public static boolean verifyUserSelfAccess(String userId) {
        if (!verifyLogin()) {
            return false;
        }
        
        // 管理员可以操作任何用户
        if (UserAuthManager.isCurrentUserAdmin()) {
            return true;
        }
        
        // 普通用户只能操作自己的数据
        if (!UserAuthManager.getCurrentUserId().equals(userId)) {
            JOptionPane.showMessageDialog(null, 
                "您只能操作自己的数据", 
                "权限不足", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
} 