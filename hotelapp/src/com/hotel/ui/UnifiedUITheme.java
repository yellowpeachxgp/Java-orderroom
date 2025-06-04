package com.hotel.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 统一的现代扁平化UI主题管理器
 * 确保整个系统的UI风格统一和现代化
 */
public class UnifiedUITheme {
    
    // === 现代扁平化配色方案 ===
    
    // 主要颜色
    public static final Color PRIMARY = new Color(52, 152, 219);       // 现代蓝色
    public static final Color PRIMARY_DARK = new Color(41, 128, 185);  // 深蓝色
    public static final Color PRIMARY_LIGHT = new Color(174, 214, 241); // 浅蓝色
    
    // 辅助颜色
    public static final Color SECONDARY = new Color(149, 165, 166);     // 灰色
    public static final Color ACCENT = new Color(230, 126, 34);         // 橙色强调色
    
    // 背景色
    public static final Color BACKGROUND = new Color(248, 249, 250);    // 浅灰背景
    public static final Color SURFACE = new Color(255, 255, 255);       // 表面白色
    public static final Color CARD = new Color(255, 255, 255);          // 卡片白色
    
    // 文本颜色
    public static final Color TEXT_PRIMARY = new Color(33, 37, 41);     // 主要文本
    public static final Color TEXT_SECONDARY = new Color(108, 117, 125); // 次要文本
    public static final Color TEXT_HINT = new Color(173, 181, 189);     // 提示文本
    
    // 状态颜色
    public static final Color SUCCESS = new Color(40, 167, 69);         // 成功绿色
    public static final Color WARNING = new Color(255, 193, 7);         // 警告黄色
    public static final Color ERROR = new Color(220, 53, 69);           // 错误红色
    public static final Color INFO = new Color(23, 162, 184);           // 信息蓝色
    
    // 边框和分割线
    public static final Color BORDER = new Color(222, 226, 230);        // 边框灰色
    public static final Color DIVIDER = new Color(233, 236, 239);       // 分割线颜色
    
    // === 现代字体系统 ===
    
    public static final Font FONT_EXTRA_LARGE = new Font("微软雅黑", Font.BOLD, 24);
    public static final Font FONT_LARGE = new Font("微软雅黑", Font.BOLD, 20);
    public static final Font FONT_TITLE = new Font("微软雅黑", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("微软雅黑", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("微软雅黑", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("微软雅黑", Font.PLAIN, 12);
    public static final Font FONT_CAPTION = new Font("微软雅黑", Font.PLAIN, 11);
    
    // === 设计规格 ===
    
    public static final int RADIUS_SMALL = 6;
    public static final int RADIUS_MEDIUM = 8;
    public static final int RADIUS_LARGE = 12;
    
    public static final int SPACING_XS = 4;
    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 16;
    public static final int SPACING_LG = 24;
    public static final int SPACING_XL = 32;
    
    public static final int ELEVATION_1 = 2;
    public static final int ELEVATION_2 = 4;
    public static final int ELEVATION_3 = 8;
    
    /**
     * 应用统一的现代主题到整个应用
     */
    public static void applyGlobalTheme() {
        try {
            // 设置系统Look and Feel属性
            UIManager.put("Panel.background", BACKGROUND);
            UIManager.put("OptionPane.background", SURFACE);
            UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
            
            // 按钮全局样式
            UIManager.put("Button.font", FONT_BODY);
            UIManager.put("Button.background", PRIMARY);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.focusPainted", false);
            UIManager.put("Button.borderPainted", false);
            
            // 文本框全局样式
            UIManager.put("TextField.font", FONT_BODY);
            UIManager.put("TextField.background", SURFACE);
            UIManager.put("TextField.foreground", TEXT_PRIMARY);
            UIManager.put("TextField.caretForeground", PRIMARY);
            
            // 密码框全局样式
            UIManager.put("PasswordField.font", FONT_BODY);
            UIManager.put("PasswordField.background", SURFACE);
            UIManager.put("PasswordField.foreground", TEXT_PRIMARY);
            UIManager.put("PasswordField.caretForeground", PRIMARY);
            
            // 标签全局样式
            UIManager.put("Label.font", FONT_BODY);
            UIManager.put("Label.foreground", TEXT_PRIMARY);
            
            // 表格全局样式
            UIManager.put("Table.font", FONT_BODY);
            UIManager.put("Table.background", SURFACE);
            UIManager.put("Table.foreground", TEXT_PRIMARY);
            UIManager.put("Table.selectionBackground", PRIMARY_LIGHT);
            UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
            UIManager.put("Table.gridColor", BORDER);
            UIManager.put("TableHeader.font", FONT_SUBTITLE);
            UIManager.put("TableHeader.background", BACKGROUND);
            UIManager.put("TableHeader.foreground", TEXT_PRIMARY);
            
            // 滚动条样式
            UIManager.put("ScrollBar.thumb", new ColorUIResource(SECONDARY));
            UIManager.put("ScrollBar.track", BACKGROUND);
            UIManager.put("ScrollBar.thumbShadow", BORDER);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 创建现代扁平化按钮
     */
    public static JButton createButton(String text, ButtonStyle style) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = style.getBackgroundColor();
                if (getModel().isPressed()) {
                    bgColor = style.getPressedColor();
                } else if (getModel().isRollover()) {
                    bgColor = style.getHoverColor();
                }
                
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 
                    RADIUS_MEDIUM, RADIUS_MEDIUM));
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setFont(FONT_BODY);
        button.setForeground(style.getTextColor());
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 40));
        
        // 添加现代交互效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });
        
        return button;
    }
    
    /**
     * 创建现代文本框
     */
    public static JTextField createTextField(String placeholder) {
        JTextField textField = new JTextField();
        textField.setFont(FONT_BODY);
        textField.setForeground(TEXT_PRIMARY);
        textField.setBackground(SURFACE);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));
        
        // 添加占位符效果
        if (placeholder != null && !placeholder.isEmpty()) {
            addPlaceholderEffect(textField, placeholder);
        }
        
        // 添加焦点效果
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY, 2, true),
                    BorderFactory.createEmptyBorder(SPACING_SM - 1, SPACING_MD - 1, 
                        SPACING_SM - 1, SPACING_MD - 1)
                ));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1, true),
                    BorderFactory.createEmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
                ));
            }
        });
        
        return textField;
    }
    
    /**
     * 创建现代密码框
     */
    public static JPasswordField createPasswordField(String placeholder) {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(FONT_BODY);
        passwordField.setForeground(TEXT_PRIMARY);
        passwordField.setBackground(SURFACE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));
        
        // 添加焦点效果
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY, 2, true),
                    BorderFactory.createEmptyBorder(SPACING_SM - 1, SPACING_MD - 1, 
                        SPACING_SM - 1, SPACING_MD - 1)
                ));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1, true),
                    BorderFactory.createEmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
                ));
            }
        });
        
        return passwordField;
    }
    
    /**
     * 创建现代卡片面板
     */
    public static JPanel createCard() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制阴影
                g2.setColor(new Color(0, 0, 0, 10));
                for (int i = 0; i < ELEVATION_1; i++) {
                    g2.fill(new RoundRectangle2D.Double(i, i, 
                        getWidth() - 2 * i, getHeight() - 2 * i, 
                        RADIUS_MEDIUM, RADIUS_MEDIUM));
                }
                
                // 绘制卡片背景
                g2.setColor(CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 
                    RADIUS_MEDIUM, RADIUS_MEDIUM));
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD));
        return panel;
    }
    
    /**
     * 创建现代标签
     */
    public static JLabel createLabel(String text, LabelStyle style) {
        JLabel label = new JLabel(text);
        
        switch (style) {
            case TITLE:
                label.setFont(FONT_TITLE);
                label.setForeground(TEXT_PRIMARY);
                break;
            case SUBTITLE:
                label.setFont(FONT_SUBTITLE);
                label.setForeground(TEXT_PRIMARY);
                break;
            case BODY:
                label.setFont(FONT_BODY);
                label.setForeground(TEXT_PRIMARY);
                break;
            case CAPTION:
                label.setFont(FONT_CAPTION);
                label.setForeground(TEXT_SECONDARY);
                break;
            case HINT:
                label.setFont(FONT_SMALL);
                label.setForeground(TEXT_HINT);
                break;
        }
        
        return label;
    }
    
    /**
     * 创建现代表格
     */
    public static JScrollPane createModernTable(JTable table) {
        // 应用现代表格样式
        table.setFont(FONT_BODY);
        table.setBackground(SURFACE);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER);
        table.setRowHeight(40);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        
        // 表头样式
        table.getTableHeader().setFont(FONT_SUBTITLE);
        table.getTableHeader().setBackground(BACKGROUND);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        scrollPane.getViewport().setBackground(SURFACE);
        
        return scrollPane;
    }
    
    /**
     * 创建现代窗口
     */
    public static void modernizeFrame(JFrame frame, String title, int width, int height) {
        applyGlobalTheme();
        
        frame.setTitle(title);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // 创建现代主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND);
        frame.add(mainPanel);
    }
    
    /**
     * 添加占位符效果
     */
    private static void addPlaceholderEffect(JTextField textField, String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(TEXT_HINT);
        
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(TEXT_PRIMARY);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(TEXT_HINT);
                }
            }
        });
    }
    
    /**
     * 按钮样式枚举
     */
    public enum ButtonStyle {
        PRIMARY(UnifiedUITheme.PRIMARY, UnifiedUITheme.PRIMARY_DARK, UnifiedUITheme.PRIMARY_LIGHT, Color.WHITE),
        SECONDARY(UnifiedUITheme.SECONDARY, UnifiedUITheme.SECONDARY.darker(), UnifiedUITheme.SECONDARY.brighter(), Color.WHITE),
        SUCCESS(UnifiedUITheme.SUCCESS, UnifiedUITheme.SUCCESS.darker(), UnifiedUITheme.SUCCESS.brighter(), Color.WHITE),
        WARNING(UnifiedUITheme.WARNING, UnifiedUITheme.WARNING.darker(), UnifiedUITheme.WARNING.brighter(), Color.WHITE),
        ERROR(UnifiedUITheme.ERROR, UnifiedUITheme.ERROR.darker(), UnifiedUITheme.ERROR.brighter(), Color.WHITE),
        OUTLINE(UnifiedUITheme.SURFACE, UnifiedUITheme.BORDER, UnifiedUITheme.BACKGROUND, UnifiedUITheme.PRIMARY);
        
        private final Color backgroundColor;
        private final Color pressedColor;
        private final Color hoverColor;
        private final Color textColor;
        
        ButtonStyle(Color backgroundColor, Color pressedColor, Color hoverColor, Color textColor) {
            this.backgroundColor = backgroundColor;
            this.pressedColor = pressedColor;
            this.hoverColor = hoverColor;
            this.textColor = textColor;
        }
        
        public Color getBackgroundColor() { return backgroundColor; }
        public Color getPressedColor() { return pressedColor; }
        public Color getHoverColor() { return hoverColor; }
        public Color getTextColor() { return textColor; }
    }
    
    /**
     * 标签样式枚举
     */
    public enum LabelStyle {
        TITLE, SUBTITLE, BODY, CAPTION, HINT
    }
} 