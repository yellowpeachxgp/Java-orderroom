package com.hotel.app;

import com.all.search.*;
import com.hotel.auth.UserAuthManager;
import com.hotel.book.RoomBooking;
import com.hotel.ui.AnimationManager;
import com.hotel.ui.ModernTheme;
import com.hotel.room.AddRoomModule;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

/**
 * 酒店管理系统主菜单
 * 提供系统各功能模块的入口
 */
public class MainMenu extends JFrame {
    
    private JPanel mainPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // 菜单面板常量
    private static final String HOME_PANEL = "HOME";
    private static final String USER_PANEL = "USER";
    private static final String ROOM_PANEL = "ROOM";
    private static final String BOOKING_PANEL = "BOOKING";
    private static final String STAFF_PANEL = "STAFF";
    
    public MainMenu() {
        // 应用现代主题
        ModernTheme.applyTheme();
        
        // 设置窗口属性
        setTitle("酒店管理系统");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 创建主面板
        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(ModernTheme.BACKGROUND_COLOR);
        
        // 创建顶部面板
        JPanel topPanel = createTopPanel();
        
        // 创建左侧导航面板
        JPanel navPanel = createNavigationPanel();
        
        // 创建内容面板（使用卡片布局）
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(ModernTheme.BACKGROUND_COLOR);
        
        // 添加各个功能面板
        contentPanel.add(createHomePanel(), HOME_PANEL);
        contentPanel.add(createUserPanel(), USER_PANEL);
        contentPanel.add(createRoomPanel(), ROOM_PANEL);
        contentPanel.add(createBookingPanel(), BOOKING_PANEL);
        contentPanel.add(createStaffPanel(), STAFF_PANEL);
        
        // 组装主界面
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(navPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 添加主面板到窗口
        add(mainPanel);
        
        // 显示窗口
        setVisible(true);
        
        // 应用淡入动画
        AnimationManager.fadeIn(mainPanel, 500);
    }
    
    /**
     * 创建顶部面板
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ModernTheme.PRIMARY_DARK_COLOR);
        panel.setPreferredSize(new Dimension(getWidth(), 60));
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        // 系统标题
        JLabel titleLabel = new JLabel("酒店管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        
        // 用户信息和退出按钮面板
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        // 显示当前用户信息
        if (UserAuthManager.getCurrentUserId() != null) {
            JLabel userLabel = new JLabel(UserAuthManager.getCurrentUserName() + 
                    " (" + (UserAuthManager.isCurrentUserAdmin() ? "管理员" : "普通用户") + ")");
            userLabel.setFont(ModernTheme.REGULAR_FONT);
            userLabel.setForeground(Color.WHITE);
            userLabel.setBorder(new EmptyBorder(0, 0, 0, 15));
            userPanel.add(userLabel);
        }
        
        // 退出按钮
        JButton logoutButton = new JButton("退出系统");
        logoutButton.setFont(ModernTheme.REGULAR_FONT);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(null);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加退出事件
        logoutButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                MainMenu.this,
                "确定要退出系统吗？",
                "确认",
                JOptionPane.YES_NO_OPTION
            );
            
            if (option == JOptionPane.YES_OPTION) {
                // 注销用户
                UserAuthManager.clearCurrentUser();
                // 关闭窗口并返回登录界面
                dispose();
                SwingUtilities.invokeLater(() -> {
                    LoginPage login = new LoginPage();
                    login.LoginPage();
                });
            }
        });
        
        userPanel.add(logoutButton);
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(userPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 创建左侧导航面板
     */
    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(ModernTheme.PRIMARY_COLOR);
        panel.setPreferredSize(new Dimension(200, getHeight()));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // 创建导航按钮
        panel.add(createNavButton("首页", "home.png", HOME_PANEL));
        panel.add(Box.createVerticalStrut(5));
        
        // 只有管理员可以看到用户管理
        if (UserAuthManager.isCurrentUserAdmin()) {
            panel.add(createNavButton("用户管理", "user.png", USER_PANEL));
            panel.add(Box.createVerticalStrut(5));
        }
        
        panel.add(createNavButton("房间管理", "room.png", ROOM_PANEL));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createNavButton("预订管理", "booking.png", BOOKING_PANEL));
        panel.add(Box.createVerticalStrut(5));
        
        // 只有管理员可以看到员工管理
        if (UserAuthManager.isCurrentUserAdmin()) {
            panel.add(createNavButton("员工管理", "staff.png", STAFF_PANEL));
        }
        
        // 添加填充空间
        panel.add(Box.createVerticalGlue());
        
        // 添加直接打开预订界面的按钮
        JButton bookingButton = new JButton("房间预订");
        bookingButton.setFont(ModernTheme.REGULAR_FONT);
        bookingButton.setForeground(Color.WHITE);
        bookingButton.setBackground(null);
        bookingButton.setBorderPainted(false);
        bookingButton.setFocusPainted(false);
        bookingButton.setContentAreaFilled(false);
        bookingButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookingButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bookingButton.setMaximumSize(new Dimension(180, 30));
        
        bookingButton.addActionListener(e -> {
            RoomBooking roomBooking = new RoomBooking(UserAuthManager.getCurrentUserId());
            roomBooking.setVisible(true);
        });
        
        panel.add(bookingButton);
        panel.add(Box.createVerticalStrut(10));
        
        // 只有管理员可以访问传统管理面板
        if (UserAuthManager.isCurrentUserAdmin()) {
            // 添加直接打开管理员面板的按钮
            JButton adminButton = new JButton("传统管理面板");
            adminButton.setFont(ModernTheme.SMALL_FONT);
            adminButton.setForeground(Color.WHITE);
            adminButton.setBackground(null);
            adminButton.setBorderPainted(false);
            adminButton.setFocusPainted(false);
            adminButton.setContentAreaFilled(false);
            adminButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            adminButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            adminButton.setMaximumSize(new Dimension(180, 30));
            
            adminButton.addActionListener(e -> {
                new com.hotel.app.admin();
            });
            
            panel.add(adminButton);
        }
        
        panel.add(Box.createVerticalStrut(20));
        
        return panel;
    }
    
    /**
     * 创建导航按钮
     */
    private JButton createNavButton(String text, String iconName, String targetPanel) {
        JButton button = new JButton(text);
        button.setFont(ModernTheme.HEADER_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(null);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(180, 50));
        button.setPreferredSize(new Dimension(180, 50));
        
        // 添加按钮悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ModernTheme.PRIMARY_DARK_COLOR);
                button.setOpaque(true);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(null);
                button.setOpaque(false);
            }
        });
        
        // 添加按钮点击事件
        button.addActionListener(e -> {
            cardLayout.show(contentPanel, targetPanel);
        });
        
        return button;
    }
    
    /**
     * 创建首页面板
     */
    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ModernTheme.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 创建欢迎面板
        JPanel welcomePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制背景渐变
                GradientPaint gradient = new GradientPaint(
                    0, 0, ModernTheme.PRIMARY_COLOR, 
                    getWidth(), getHeight(), ModernTheme.ACCENT_COLOR
                );
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                
                g2d.dispose();
            }
        };
        welcomePanel.setLayout(new BorderLayout());
        welcomePanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // 欢迎标题
        JLabel welcomeLabel = new JLabel("欢迎使用酒店管理系统");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);
        
        // 系统描述
        JLabel descriptionLabel = new JLabel("<html>本系统提供全面的酒店管理功能，包括用户管理、房间管理、预订管理和员工管理等。<br>请通过左侧导航菜单选择您需要的功能模块。</html>");
        descriptionLabel.setFont(ModernTheme.REGULAR_FONT);
        descriptionLabel.setForeground(Color.WHITE);
        
        welcomePanel.add(welcomeLabel, BorderLayout.NORTH);
        welcomePanel.add(descriptionLabel, BorderLayout.CENTER);
        
        // 创建快捷功能面板
        JPanel shortcutsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        shortcutsPanel.setOpaque(false);
        shortcutsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // 添加快捷功能卡片
        shortcutsPanel.add(createShortcutCard("用户管理", "快速添加和管理用户信息", ModernTheme.PRIMARY_COLOR, e -> cardLayout.show(contentPanel, USER_PANEL)));
        shortcutsPanel.add(createShortcutCard("房间管理", "管理房间信息和状态", ModernTheme.ACCENT_COLOR, e -> cardLayout.show(contentPanel, ROOM_PANEL)));
        shortcutsPanel.add(createShortcutCard("预订管理", "处理房间预订和入住", new Color(46, 204, 113), e -> cardLayout.show(contentPanel, BOOKING_PANEL)));
        shortcutsPanel.add(createShortcutCard("员工管理", "管理员工和权限", new Color(155, 89, 182), e -> cardLayout.show(contentPanel, STAFF_PANEL)));
        
        panel.add(welcomePanel, BorderLayout.NORTH);
        panel.add(shortcutsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建快捷功能卡片
     */
    private JPanel createShortcutCard(String title, String description, Color color, ActionListener action) {
        JPanel card = new JPanel() {
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
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), 10, 0, 0));
                
                g2d.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setOpaque(false);
        
        // 标题
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ModernTheme.HEADER_FONT);
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        
        // 描述
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(ModernTheme.SMALL_FONT);
        descLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        
        // 打开按钮
        JButton openButton = new JButton("打开");
        openButton.setFont(ModernTheme.SMALL_FONT);
        openButton.setForeground(color);
        openButton.setBackground(null);
        openButton.setBorderPainted(false);
        openButton.setContentAreaFilled(false);
        openButton.setFocusPainted(false);
        
        // 添加点击事件
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        openButton.addActionListener(action);
        
        // 组装面板
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(descLabel);
        
        card.add(textPanel, BorderLayout.CENTER);
        card.add(openButton, BorderLayout.SOUTH);
        
        return card;
    }
    
    /**
     * 创建用户管理面板
     */
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ModernTheme.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 添加标题
        JLabel titleLabel = new JLabel("用户管理");
        titleLabel.setFont(ModernTheme.HEADER_FONT);
        titleLabel.setForeground(ModernTheme.PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        buttonPanel.setOpaque(false);
        
        // 只有管理员可以看到这些功能
        if (UserAuthManager.isCurrentUserAdmin()) {
            buttonPanel.add(createFunctionButton("添加用户", ModernTheme.PRIMARY_COLOR, e -> add_client.main(new String[]{})));
            buttonPanel.add(createFunctionButton("删除用户", ModernTheme.ERROR_COLOR, e -> delete_client.main(new String[]{})));
            buttonPanel.add(createFunctionButton("用户信息管理", ModernTheme.ACCENT_COLOR, e -> alter_client.main(new String[]{})));
        } else {
            JLabel infoLabel = new JLabel("您没有权限访问用户管理功能");
            infoLabel.setFont(ModernTheme.REGULAR_FONT);
            infoLabel.setForeground(ModernTheme.TEXT_SECONDARY);
            infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            buttonPanel.add(infoLabel);
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建房间管理面板
     */
    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ModernTheme.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("房间管理");
        titleLabel.setFont(ModernTheme.TITLE_FONT);
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonPanel.setOpaque(false);
        
        // 使用现代化界面替代旧版
        buttonPanel.add(createFunctionButton("添加房间", ModernTheme.PRIMARY_COLOR, e -> {
            new AddRoomModule();
        }));
        
        buttonPanel.add(createFunctionButton("修改房间", ModernTheme.ACCENT_COLOR, e -> {
            new alter_room();
        }));
        
        buttonPanel.add(createFunctionButton("删除房间", ModernTheme.ERROR_COLOR, e -> {
            new delete_room();
        }));
        
        buttonPanel.add(createFunctionButton("所有房间", ModernTheme.SUCCESS_COLOR, e -> {
            new allRoom();
        }));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建预订管理面板
     */
    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ModernTheme.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 标题面板
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = ModernTheme.createTitleLabel("预订管理");
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // 功能按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonPanel.setOpaque(false);
        
        buttonPanel.add(createFunctionButton("入住管理", ModernTheme.PRIMARY_COLOR, e -> checkIn.main(new String[]{})));
        buttonPanel.add(createFunctionButton("退房管理", ModernTheme.ERROR_COLOR, e -> checkOut.main(new String[]{})));
        buttonPanel.add(createFunctionButton("换房管理", ModernTheme.ACCENT_COLOR, e -> changeRoom.main(new String[]{})));
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建员工管理面板
     */
    private JPanel createStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ModernTheme.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 添加标题
        JLabel titleLabel = new JLabel("员工管理");
        titleLabel.setFont(ModernTheme.HEADER_FONT);
        titleLabel.setForeground(ModernTheme.PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        buttonPanel.setOpaque(false);
        
        // 只有管理员可以看到这些功能
        if (UserAuthManager.isCurrentUserAdmin()) {
            buttonPanel.add(createFunctionButton("添加负责人", ModernTheme.PRIMARY_COLOR, e -> add_leader.main(new String[]{})));
            buttonPanel.add(createFunctionButton("删除负责人", ModernTheme.ERROR_COLOR, e -> delete_leader.main(new String[]{})));
            buttonPanel.add(createFunctionButton("负责人信息管理", ModernTheme.ACCENT_COLOR, e -> alter_leader.main(new String[]{})));
        } else {
            JLabel infoLabel = new JLabel("您没有权限访问员工管理功能");
            infoLabel.setFont(ModernTheme.REGULAR_FONT);
            infoLabel.setForeground(ModernTheme.TEXT_SECONDARY);
            infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            buttonPanel.add(infoLabel);
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建功能按钮
     */
    private JPanel createFunctionButton(String text, Color color, ActionListener action) {
        JPanel buttonPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制背景
                g2d.setColor(ModernTheme.CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                
                // 绘制左侧颜色条
                g2d.setColor(color);
                g2d.fill(new Rectangle(0, 0, 10, getHeight()));
                
                g2d.dispose();
            }
        };
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.setOpaque(false);
        
        // 添加按钮文本
        JLabel buttonLabel = new JLabel(text);
        buttonLabel.setFont(ModernTheme.HEADER_FONT);
        buttonLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        buttonLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        buttonPanel.add(buttonLabel, BorderLayout.CENTER);
        
        // 添加点击事件
        buttonPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                    new EmptyBorder(20, 20, 20, 20),
                    BorderFactory.createLineBorder(color, 1, true)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            }
        });
        
        return buttonPanel;
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainMenu();
        });
    }
} 