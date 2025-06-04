package com.hotel.auth;

import com.hotel.ui.ModernTheme;
import com.hotel.user.ChangePasswordDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * 登录对话框
 * 用于用户身份验证
 */
public class LoginDialog extends JDialog {
    
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private boolean loginSuccess = false;
    
    /**
     * 构造函数
     * @param parent 父窗口
     */
    public LoginDialog(JFrame parent) {
        super(parent, "用户登录", true);
        
        // 应用现代主题
        ModernTheme.applyTheme();
        
        // 设置窗口属性
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 400, 350, 15, 15));
        
        // 创建主面板
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ModernTheme.BACKGROUND_COLOR);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setOpaque(false);
        
        // 创建顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        // 创建标题
        JLabel titleLabel = ModernTheme.createTitleLabel("酒店管理系统");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // 创建关闭按钮
        JButton closeButton = new JButton("×");
        closeButton.setForeground(ModernTheme.TEXT_PRIMARY);
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(null);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> System.exit(0));
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(ModernTheme.ERROR_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(ModernTheme.TEXT_PRIMARY);
            }
        });
        
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(closeButton, BorderLayout.EAST);
        
        // 创建表单面板
        JPanel formPanel = ModernTheme.createRoundedPanel();
        formPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 10, 10);
        
        // 创建用户ID输入框和标签
        JLabel userIdLabel = ModernTheme.createLabel("用户ID:");
        userIdField = ModernTheme.createModernTextField(20);
        
        c.gridx = 0;
        c.gridy = 0;
        formPanel.add(userIdLabel, c);
        
        c.gridx = 0;
        c.gridy = 1;
        formPanel.add(userIdField, c);
        
        // 创建密码输入框和标签
        JLabel passwordLabel = ModernTheme.createLabel("密码:");
        passwordField = ModernTheme.createModernPasswordField(20);
        
        // 添加回车键监听器
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        });
        
        c.gridx = 0;
        c.gridy = 2;
        formPanel.add(passwordLabel, c);
        
        c.gridx = 0;
        c.gridy = 3;
        formPanel.add(passwordField, c);
        
        // 创建状态标签
        statusLabel = new JLabel("");
        statusLabel.setForeground(ModernTheme.ERROR_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(ModernTheme.SMALL_FONT);
        
        c.gridx = 0;
        c.gridy = 4;
        c.insets = new Insets(5, 10, 5, 10);
        formPanel.add(statusLabel, c);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        
        // 创建登录按钮
        loginButton = ModernTheme.createRoundedButton("登录");
        loginButton.addActionListener(e -> login());
        
        // 创建取消按钮
        cancelButton = ModernTheme.createRoundedButton("退出");
        cancelButton.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);
        
        c.gridx = 0;
        c.gridy = 5;
        c.insets = new Insets(20, 10, 10, 10);
        formPanel.add(buttonPanel, c);
        
        // 添加面板到主面板
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // 添加主面板到窗口
        add(mainPanel);
        
        // 设置默认按钮
        getRootPane().setDefaultButton(loginButton);
        
        // 显示窗口
        setVisible(true);
    }
    
    /**
     * 登录操作
     */
    private void login() {
        // 获取输入的用户ID和密码
        String userId = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // 验证输入
        if (userId.isEmpty()) {
            showError("请输入用户ID");
            return;
        }
        
        if (password.isEmpty()) {
            showError("请输入密码");
            return;
        }
        
        // 禁用按钮防止重复点击
        loginButton.setEnabled(false);
        cancelButton.setEnabled(false);
        
        // 显示加载状态
        statusLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        statusLabel.setText("正在验证...");
        
        // 在新线程中执行登录操作
        new Thread(() -> {
            try {
                // 调用服务进行登录验证
                AuthService.LoginResult result = AuthService.login(userId, password);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    if (result.isSuccess()) {
                        // 登录成功
                        loginSuccess = true;
                        
                        // 保存当前用户信息
                        UserAuthManager.setCurrentUser(
                            result.getUserId(),
                            result.getUserName(),
                            result.getAccountType(),
                            result.isFirstLogin()
                        );
                        
                        // 如果是首次登录，强制修改密码
                        if (result.isFirstLogin()) {
                            dispose();
                            new ChangePasswordDialog((JFrame) getOwner(), true);
                        } else {
                            dispose();
                        }
                        
                    } else {
                        // 登录失败
                        showError(result.getErrorMessage());
                        loginButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                    }
                });
                
            } catch (Exception ex) {
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    showError("登录时发生错误: " + ex.getMessage());
                    loginButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                });
            }
        }).start();
    }
    
    /**
     * 显示错误消息
     * @param message 错误消息
     */
    private void showError(String message) {
        statusLabel.setForeground(ModernTheme.ERROR_COLOR);
        statusLabel.setText(message);
    }
    
    /**
     * 检查登录是否成功
     * @return 是否登录成功
     */
    public boolean isLoginSuccess() {
        return loginSuccess;
    }
    
    /**
     * 主方法，用于测试
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ModernTheme.applyTheme();
                LoginDialog dialog = new LoginDialog(new JFrame());
                if (dialog.isLoginSuccess()) {
                    System.out.println("登录成功！");
                } else {
                    System.out.println("登录取消！");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
} 