package com.hotel.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 现代化UI主题工具类
 * 提供扁平化设计、圆角按钮、过渡动画和现代配色方案
 */
public class ModernTheme {
    // 主色调
    public static final Color PRIMARY_COLOR = new Color(41, 128, 185);  // 蓝色
    public static final Color PRIMARY_DARK_COLOR = new Color(44, 62, 80);  // 深蓝色
    public static final Color ACCENT_COLOR = new Color(230, 126, 34);  // 橙色
    
    // 背景色
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 245);  // 浅灰色
    public static final Color CARD_COLOR = new Color(255, 255, 255);  // 白色
    
    // 文本颜色
    public static final Color TEXT_PRIMARY = new Color(33, 33, 33);  // 主要文本颜色
    public static final Color TEXT_SECONDARY = new Color(117, 117, 117);  // 次要文本颜色
    
    // 状态颜色
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);  // 成功绿色
    public static final Color ERROR_COLOR = new Color(231, 76, 60);  // 错误红色
    public static final Color WARNING_COLOR = new Color(241, 196, 15);  // 警告黄色
    public static final Color DANGER_COLOR = new Color(231, 76, 60);  // 危险红色
    
    // 边框颜色
    public static final Color BORDER_COLOR = new Color(218, 218, 218);  // 边框灰色
    
    // 字体
    public static final Font TITLE_FONT = new Font("微软雅黑", Font.BOLD, 18);
    public static final Font HEADER_FONT = new Font("微软雅黑", Font.BOLD, 16);
    public static final Font REGULAR_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("微软雅黑", Font.PLAIN, 12);
    public static final Font BUTTON_FONT = new Font("微软雅黑", Font.BOLD, 14);
    
    // 圆角边框
    public static final int BORDER_RADIUS = 8;
    
    // 应用现代主题到整个应用程序
    public static void applyTheme() {
        try {
            // 设置全局UI属性
            UIManager.put("Panel.background", BACKGROUND_COLOR);
            UIManager.put("OptionPane.background", CARD_COLOR);
            UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
            
            // 按钮样式
            UIManager.put("Button.background", PRIMARY_COLOR);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.font", REGULAR_FONT);
            UIManager.put("Button.focusPainted", false);
            UIManager.put("Button.borderPainted", false);
            UIManager.put("Button.opaque", true);
            
            // 文本框样式
            UIManager.put("TextField.background", CARD_COLOR);
            UIManager.put("TextField.foreground", TEXT_PRIMARY);
            UIManager.put("TextField.caretForeground", PRIMARY_COLOR);
            UIManager.put("TextField.font", REGULAR_FONT);
            
            // 密码框样式
            UIManager.put("PasswordField.background", CARD_COLOR);
            UIManager.put("PasswordField.foreground", TEXT_PRIMARY);
            UIManager.put("PasswordField.caretForeground", PRIMARY_COLOR);
            UIManager.put("PasswordField.font", REGULAR_FONT);
            
            // 标签样式
            UIManager.put("Label.font", REGULAR_FONT);
            UIManager.put("Label.foreground", TEXT_PRIMARY);
            
            // 表格样式
            UIManager.put("Table.background", CARD_COLOR);
            UIManager.put("Table.foreground", TEXT_PRIMARY);
            UIManager.put("Table.selectionBackground", PRIMARY_COLOR);
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("Table.gridColor", new Color(230, 230, 230));
            UIManager.put("Table.font", REGULAR_FONT);
            UIManager.put("TableHeader.background", PRIMARY_DARK_COLOR);
            UIManager.put("TableHeader.foreground", Color.WHITE);
            UIManager.put("TableHeader.font", REGULAR_FONT);
            
            // 滚动条样式
            UIManager.put("ScrollBar.thumb", new ColorUIResource(180, 180, 180));
            UIManager.put("ScrollBar.track", BACKGROUND_COLOR);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 创建圆角按钮
    public static JButton createRoundedButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(PRIMARY_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(PRIMARY_COLOR.brighter());
                } else {
                    g2.setColor(PRIMARY_COLOR);
                }
                
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS));
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setForeground(Color.WHITE);
        button.setFont(REGULAR_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        
        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return button;
    }
    
    // 创建圆角面板
    public static JPanel createRoundedPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_COLOR);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return panel;
    }
    
    // 创建现代风格的文本框
    public static JTextField createModernTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(REGULAR_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        return textField;
    }
    
    // 创建现代风格的密码框
    public static JPasswordField createModernPasswordField(int columns) {
        JPasswordField passwordField = new JPasswordField(columns);
        passwordField.setFont(REGULAR_FONT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        return passwordField;
    }
    
    // 创建标题标签
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE_FONT);
        label.setForeground(PRIMARY_DARK_COLOR);
        return label;
    }
    
    // 创建普通标签
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(REGULAR_FONT);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }
    
    // 应用动画效果 - 淡入
    public static void fadeIn(final JComponent component, final int duration) {
        new Thread(() -> {
            float opacity = 0.0f;
            component.setVisible(true);
            
            try {
                while (opacity < 1.0f) {
                    opacity += 0.05f;
                    if (opacity > 1.0f) opacity = 1.0f;
                    
                    final float finalOpacity = opacity;
                    SwingUtilities.invokeLater(() -> 
                        component.setBackground(new Color(
                            component.getBackground().getRed(),
                            component.getBackground().getGreen(),
                            component.getBackground().getBlue(),
                            (int)(finalOpacity * 255)
                        ))
                    );
                    
                    Thread.sleep(duration / 20);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
} 