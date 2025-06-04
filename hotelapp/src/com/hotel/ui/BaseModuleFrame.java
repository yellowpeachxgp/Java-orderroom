package com.hotel.ui;

import com.hotel.auth.UserAuthManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * 基础模块窗口类
 * 所有模块窗口的父类，提供统一的窗口行为和UI元素
 */
public abstract class BaseModuleFrame extends JFrame {
    
    protected static final Logger LOGGER = Logger.getLogger(BaseModuleFrame.class.getName());
    
    // 线程池，用于执行后台任务
    protected final ExecutorService executorService;
    
    // UI组件
    protected JPanel mainPanel;
    protected JLabel statusLabel;
    protected JPanel contentPanel;
    
    // 权限类型常量
    public static final int PERMISSION_NONE = 0;      // 不需要权限
    public static final int PERMISSION_LOGIN = 1;     // 需要登录
    public static final int PERMISSION_ADMIN = 2;     // 需要管理员权限
    
    /**
     * 构造函数
     * @param title 窗口标题
     * @param width 窗口宽度
     * @param height 窗口高度
     */
    public BaseModuleFrame(String title, int width, int height) {
        this(title, width, height, PERMISSION_NONE);
    }
    
    /**
     * 带权限检查的构造函数
     * @param title 窗口标题
     * @param width 窗口宽度
     * @param height 窗口高度
     * @param permissionLevel 需要的权限级别
     */
    public BaseModuleFrame(String title, int width, int height, int permissionLevel) {
        try {
            // 检查权限
            if (!checkPermission(permissionLevel)) {
                // 如果权限检查失败，不继续初始化
                executorService = null;
                return;
            }
            
            // 设置窗口属性
            setTitle(title);
            setSize(width, height);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            
            // 创建线程池
            executorService = UIHelper.createExecutorService();
            
            // 应用现代主题
            ModernTheme.applyTheme();
            
            // 创建基础UI组件
            initializeUI();
            
            // 添加窗口关闭事件
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    cleanupResources();
                }
            });
            
        } catch (Exception e) {
            UIHelper.handleException(this, "初始化窗口失败", e);
            throw new RuntimeException("窗口初始化失败", e);
        }
    }
    
    /**
     * 检查当前用户是否有权限
     * @param permissionLevel 需要的权限级别
     * @return 是否有权限
     */
    protected boolean checkPermission(int permissionLevel) {
        // 不需要权限
        if (permissionLevel == PERMISSION_NONE) {
            return true;
        }
        
        // 需要登录
        if (permissionLevel >= PERMISSION_LOGIN) {
            if (UserAuthManager.getCurrentUserId() == null) {
                JOptionPane.showMessageDialog(this, 
                    "请先登录系统", 
                    "未登录", 
                    JOptionPane.WARNING_MESSAGE);
                dispose();
                return false;
            }
        }
        
        // 需要管理员权限
        if (permissionLevel >= PERMISSION_ADMIN) {
            if (!UserAuthManager.isCurrentUserAdmin()) {
                JOptionPane.showMessageDialog(this, 
                    "您没有权限访问此功能", 
                    "权限不足", 
                    JOptionPane.WARNING_MESSAGE);
                dispose();
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 初始化UI组件
     */
    protected void initializeUI() {
        // 创建主面板
        mainPanel = CommonUITemplate.createMainPanel();
        
        // 创建顶部标题面板
        JPanel headerPanel = CommonUITemplate.createHeaderPanel(getTitle(), e -> {
            cleanupResources();
            dispose();
        });
        
        // 创建内容面板
        contentPanel = CommonUITemplate.createContentPanel();
        
        // 创建状态栏
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusLabel = new JLabel("就绪");
        statusLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        statusLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        // 如果是管理员，添加权限信息
        if (UserAuthManager.getCurrentUserId() != null) {
            JLabel userInfoLabel = new JLabel(UserAuthManager.getCurrentUserName() + 
                " (" + (UserAuthManager.isCurrentUserAdmin() ? "管理员" : "普通用户") + ")");
            userInfoLabel.setForeground(ModernTheme.TEXT_SECONDARY);
            userInfoLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
            statusPanel.add(userInfoLabel, BorderLayout.EAST);
        }
        
        // 添加到主面板
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // 添加主面板到窗口
        add(mainPanel);
    }
    
    /**
     * 显示窗口并应用动画
     */
    public void showWithAnimation() {
        setVisible(true);
        CommonUITemplate.applyFadeInAnimation(mainPanel);
    }
    
    /**
     * 更新状态栏
     * @param message 状态消息
     */
    protected void updateStatus(String message) {
        UIHelper.updateStatus(statusLabel, message);
    }
    
    /**
     * 处理异常
     * @param message 错误消息
     * @param e 异常
     */
    protected void handleException(String message, Exception e) {
        UIHelper.handleException(this, message, e);
    }
    
    /**
     * 清理资源
     * 子类应该覆盖此方法并调用super.cleanupResources()
     */
    protected void cleanupResources() {
        try {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                LOGGER.info("线程池已关闭");
            }
        } catch (Exception e) {
            LOGGER.warning("关闭线程池时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 窗口关闭时调用
     */
    @Override
    public void dispose() {
        cleanupResources();
        super.dispose();
    }
} 