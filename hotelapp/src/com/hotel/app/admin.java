package com.hotel.app;

import com.all.search.*;
import com.hotel.ui.AnimationManager;
import com.hotel.ui.ModernTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class admin extends JFrame {
    private JPanel contentPanel;
    
    public admin() {
        // 应用现代主题
        ModernTheme.applyTheme();
        
        setTitle("酒店管理系统 - 传统面板");
        setSize(850, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 关闭窗口时不退出整个应用
        
        // 创建主内容面板
        contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(ModernTheme.BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 创建顶部标题面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ModernTheme.PRIMARY_DARK_COLOR);
        topPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("传统管理面板");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JButton backButton = new JButton("返回主菜单");
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(null);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFont(ModernTheme.REGULAR_FONT);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            dispose(); // 关闭当前窗口
        });
        
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(backButton, BorderLayout.EAST);
        
        // 创建功能按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(5, 3, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // 用户管理按钮
        buttonPanel.add(createFunctionButton("添加用户", "user_add", ModernTheme.PRIMARY_COLOR, e -> add_client.main(new String[]{})));
        buttonPanel.add(createFunctionButton("删除用户", "user_delete", ModernTheme.ERROR_COLOR, e -> delete_client.main(new String[]{})));
        buttonPanel.add(createFunctionButton("用户信息管理", "user_edit", ModernTheme.ACCENT_COLOR, e -> alter_client.main(new String[]{})));
        
        // 房间管理按钮
        buttonPanel.add(createFunctionButton("添加房间", "room_add", ModernTheme.PRIMARY_COLOR, e -> add_room.main(new String[]{})));
        buttonPanel.add(createFunctionButton("删除房间", "room_delete", ModernTheme.ERROR_COLOR, e -> delete_room.main(new String[]{})));
        buttonPanel.add(createFunctionButton("房间信息管理", "room_edit", ModernTheme.ACCENT_COLOR, e -> alter_room.main(new String[]{})));
        
        // 负责人管理按钮
        buttonPanel.add(createFunctionButton("添加负责人", "leader_add", ModernTheme.PRIMARY_COLOR, e -> add_leader.main(new String[]{})));
        buttonPanel.add(createFunctionButton("删除负责人", "leader_delete", ModernTheme.ERROR_COLOR, e -> delete_leader.main(new String[]{})));
        buttonPanel.add(createFunctionButton("负责人信息管理", "leader_edit", ModernTheme.ACCENT_COLOR, e -> alter_leader.main(new String[]{})));
        
        // 入住退房管理按钮
        buttonPanel.add(createFunctionButton("入住管理", "check_in", new Color(46, 204, 113), e -> checkIn.main(new String[]{})));
        buttonPanel.add(createFunctionButton("退房管理", "check_out", new Color(231, 76, 60), e -> checkOut.main(new String[]{})));
        buttonPanel.add(createFunctionButton("换房管理", "change_room", new Color(230, 126, 34), e -> changeRoom.main(new String[]{})));
        
        // 查看所有房间信息
        buttonPanel.add(createFunctionButton("所有房间信息", "room_all", new Color(52, 152, 219), e -> allRoom.main(new String[]{})));
        
        // 添加到内容面板
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // 添加到窗口
        add(contentPanel);
        
        // 显示窗口
        setVisible(true);
        
        // 应用淡入动画
        AnimationManager.fadeIn(contentPanel, 300);
    }
    
    /**
     * 创建功能按钮
     */
    private JPanel createFunctionButton(String text, String iconName, Color color, ActionListener action) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制背景
                g2d.setColor(ModernTheme.CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                
                // 绘制顶部颜色条
                g2d.setColor(color);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), 5, 0, 0));
                
                g2d.dispose();
            }
        };
        
        panel.setLayout(new BorderLayout());
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建按钮文本
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(ModernTheme.HEADER_FONT);
        label.setForeground(ModernTheme.TEXT_PRIMARY);
        
        panel.add(label, BorderLayout.CENTER);
        
        // 添加鼠标效果
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(10, 10, 10, 10),
                    BorderFactory.createLineBorder(color, 1, true)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            }
            
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new admin(); // 创建并显示管理员界面
            }
        });
    }
}
