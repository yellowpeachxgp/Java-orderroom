package com.hotel.user;

import com.hotel.auth.AuthService;
import com.hotel.auth.UserAuthManager;
import com.hotel.ui.ModernTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

/**
 * 修改密码对话框
 * 允许用户修改自己的登录密码
 */
public class ChangePasswordDialog extends JDialog {
    
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton confirmButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    /**
     * 构造函数
     * @param parent 父窗口
     * @param isFirstLogin 是否首次登录
     */
    public ChangePasswordDialog(JFrame parent, boolean isFirstLogin) {
        super(parent, "修改密码", true);
        
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
        JLabel titleLabel = ModernTheme.createTitleLabel(isFirstLogin ? "首次登录，请修改密码" : "修改密码");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // 创建关闭按钮（仅在非首次登录时显示）
        if (!isFirstLogin) {
            JButton closeButton = new JButton("×");
            closeButton.setForeground(ModernTheme.TEXT_PRIMARY);
            closeButton.setFont(new Font("Arial", Font.BOLD, 18));
            closeButton.setContentAreaFilled(false);
            closeButton.setBorder(null);
            closeButton.setFocusPainted(false);
            closeButton.addActionListener(e -> dispose());
            closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    closeButton.setForeground(ModernTheme.ERROR_COLOR);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    closeButton.setForeground(ModernTheme.TEXT_PRIMARY);
                }
            });
            topPanel.add(closeButton, BorderLayout.EAST);
        }
        
        topPanel.add(titleLabel, BorderLayout.CENTER);
        
        // 创建表单面板
        JPanel formPanel = ModernTheme.createRoundedPanel();
        formPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 10, 10);
        
        // 创建当前密码输入框和标签（非首次登录时显示）
        if (!isFirstLogin) {
            JLabel currentPassLabel = ModernTheme.createLabel("当前密码:");
            currentPasswordField = ModernTheme.createModernPasswordField(20);
            
            c.gridx = 0;
            c.gridy = 0;
            formPanel.add(currentPassLabel, c);
            
            c.gridx = 0;
            c.gridy = 1;
            formPanel.add(currentPasswordField, c);
        }
        
        // 创建新密码输入框和标签
        JLabel newPassLabel = ModernTheme.createLabel("新密码:");
        newPasswordField = ModernTheme.createModernPasswordField(20);
        
        c.gridx = 0;
        c.gridy = isFirstLogin ? 0 : 2;
        formPanel.add(newPassLabel, c);
        
        c.gridx = 0;
        c.gridy = isFirstLogin ? 1 : 3;
        formPanel.add(newPasswordField, c);
        
        // 创建确认密码输入框和标签
        JLabel confirmPassLabel = ModernTheme.createLabel("确认新密码:");
        confirmPasswordField = ModernTheme.createModernPasswordField(20);
        
        c.gridx = 0;
        c.gridy = isFirstLogin ? 2 : 4;
        formPanel.add(confirmPassLabel, c);
        
        c.gridx = 0;
        c.gridy = isFirstLogin ? 3 : 5;
        formPanel.add(confirmPasswordField, c);
        
        // 创建状态标签
        statusLabel = new JLabel("");
        statusLabel.setForeground(ModernTheme.ERROR_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(ModernTheme.SMALL_FONT);
        
        c.gridx = 0;
        c.gridy = isFirstLogin ? 4 : 6;
        c.insets = new Insets(5, 10, 5, 10);
        formPanel.add(statusLabel, c);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        
        // 创建确认按钮
        confirmButton = ModernTheme.createRoundedButton("确认");
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changePassword(isFirstLogin);
            }
        });
        
        // 创建取消按钮（仅在非首次登录时显示）
        if (!isFirstLogin) {
            cancelButton = ModernTheme.createRoundedButton("取消");
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(cancelButton);
        }
        
        buttonPanel.add(confirmButton);
        
        c.gridx = 0;
        c.gridy = isFirstLogin ? 5 : 7;
        c.insets = new Insets(20, 10, 10, 10);
        formPanel.add(buttonPanel, c);
        
        // 添加面板到主面板
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // 添加主面板到窗口
        add(mainPanel);
        
        // 设置默认按钮
        getRootPane().setDefaultButton(confirmButton);
        
        // 如果是首次登录，设置对话框不可关闭
        if (isFirstLogin) {
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        }
        
        // 显示窗口
        setVisible(true);
    }
    
    /**
     * 修改密码
     * @param isFirstLogin 是否首次登录
     */
    private void changePassword(boolean isFirstLogin) {
        // 获取输入的密码
        String currentPassword = isFirstLogin ? "password" : new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // 验证输入
        if (!isFirstLogin && currentPassword.isEmpty()) {
            showError("请输入当前密码");
            return;
        }
        
        if (newPassword.isEmpty()) {
            showError("请输入新密码");
            return;
        }
        
        if (confirmPassword.isEmpty()) {
            showError("请确认新密码");
            return;
        }
        
        if (newPassword.length() < 6) {
            showError("新密码长度不能少于6位");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showError("两次输入的新密码不一致");
            return;
        }
        
        // 禁用按钮防止重复点击
        confirmButton.setEnabled(false);
        if (cancelButton != null) {
            cancelButton.setEnabled(false);
        }
        
        // 显示加载状态
        statusLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        statusLabel.setText("正在更新密码...");
        
        // 在新线程中执行更新操作
        new Thread(() -> {
            try {
                // 调用服务更新密码
                boolean success = AuthService.updatePassword(
                    UserAuthManager.getCurrentUserId(),
                    currentPassword,
                    newPassword
                );
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        JOptionPane.showMessageDialog(
                            this,
                            "密码修改成功",
                            "成功",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        dispose();
                    } else {
                        showError("密码修改失败，请检查当前密码是否正确");
                        confirmButton.setEnabled(true);
                        if (cancelButton != null) {
                            cancelButton.setEnabled(true);
                        }
                    }
                });
            } catch (Exception ex) {
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    showError("发生错误: " + ex.getMessage());
                    confirmButton.setEnabled(true);
                    if (cancelButton != null) {
                        cancelButton.setEnabled(true);
                    }
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
     * 主方法，用于测试
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setSize(500, 500);
            frame.setVisible(true);
            
            new ChangePasswordDialog(frame, false);
        });
    }
} 