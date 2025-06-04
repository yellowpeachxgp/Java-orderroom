package com.hotel.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

/**
 * 通用UI模板
 * 提供统一的UI组件创建方法，确保系统UI风格一致
 */
public class CommonUITemplate {
    
    /**
     * 创建基本窗口
     * @param title 窗口标题
     * @param width 窗口宽度
     * @param height 窗口高度
     * @return 配置好的JFrame窗口
     */
    public static JFrame createBasicFrame(String title, int width, int height) {
        // 应用现代主题
        ModernTheme.applyTheme();
        
        JFrame frame = new JFrame(title);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        return frame;
    }
    
    /**
     * 创建主面板
     * @return 主面板
     */
    public static JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(ModernTheme.BACKGROUND_COLOR);
        panel.setBorder(null);
        return panel;
    }
    
    /**
     * 创建标题面板
     * @param title 标题文本
     * @param closeAction 关闭按钮动作
     * @return 标题面板
     */
    public static JPanel createHeaderPanel(String title, ActionListener closeAction) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ModernTheme.PRIMARY_COLOR);
        panel.setPreferredSize(new Dimension(0, 50));
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // 标题标签
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        // 关闭按钮
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.setForeground(Color.WHITE);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(null);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(new Color(255, 200, 200));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(Color.WHITE);
            }
        });
        
        // 添加关闭动作
        if (closeAction != null) {
            closeButton.addActionListener(closeAction);
        }
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(closeButton, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 创建内容面板
     * @return 内容面板
     */
    public static JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(ModernTheme.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        return panel;
    }
    
    /**
     * 创建表单面板
     * @return 配置好的表单面板
     */
    public static JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }
    
    /**
     * 创建表格面板
     * @param model 表格数据模型
     * @return 配置好的带滚动条的表格
     */
    public static JScrollPane createTablePanel(DefaultTableModel model) {
        JTable table = new JTable(model);
        ModernTableRenderer.applyModernStyle(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ModernTheme.CARD_COLOR);
        
        return scrollPane;
    }
    
    /**
     * 创建表格标题面板
     * @param title 标题文本
     * @return 配置好的表格标题面板
     */
    public static JPanel createTableHeader(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ModernTheme.HEADER_FONT);
        titleLabel.setForeground(ModernTheme.PRIMARY_DARK_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        panel.add(titleLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    /**
     * 创建按钮面板
     * @return 按钮面板
     */
    public static JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(ModernTheme.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(0, 15, 15, 15));
        return panel;
    }
    
    /**
     * 创建卡片式面板
     * @return 卡片式面板
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ModernTheme.CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        return panel;
    }
    
    /**
     * 创建操作按钮
     * @param text 按钮文本
     * @param color 按钮颜色
     * @param action 按钮动作
     * @return 按钮
     */
    public static JButton createActionButton(String text, Color color, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(ModernTheme.REGULAR_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        // 添加动作
        if (action != null) {
            button.addActionListener(action);
        }
        
        return button;
    }
    
    /**
     * 创建标准文本输入字段
     * @param label 标签文本
     * @param columns 输入框列数
     * @return 包含标签和输入框的面板
     */
    public static JPanel createInputField(String label, int columns) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        
        JLabel fieldLabel = ModernTheme.createLabel(label);
        JTextField textField = ModernTheme.createModernTextField(columns);
        
        panel.add(fieldLabel, BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建表单布局约束
     * @param gridx x位置
     * @param gridy y位置
     * @param gridwidth 跨列数
     * @param weightx x权重
     * @return 配置好的约束
     */
    public static GridBagConstraints createFormConstraints(int gridx, int gridy, int gridwidth, double weightx) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 8, 8, 8);
        c.gridx = gridx;
        c.gridy = gridy;
        c.gridwidth = gridwidth;
        c.weightx = weightx;
        return c;
    }
    
    /**
     * 创建加载对话框
     * @param parent 父窗口
     * @param message 显示消息
     * @return 配置好的对话框
     */
    public static JDialog createLoadingDialog(JFrame parent, String message) {
        JDialog dialog = new JDialog(parent, "处理中", true);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(parent);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ModernTheme.CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.setColor(new Color(230, 230, 230));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(ModernTheme.REGULAR_FONT);
        messageLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(messageLabel, BorderLayout.CENTER);
        dialog.add(panel);
        
        return dialog;
    }
    
    /**
     * 应用淡入动画
     * @param component 要应用动画的组件
     */
    public static void applyFadeInAnimation(JComponent component) {
        AnimationManager.fadeIn(component, 500);
    }
} 