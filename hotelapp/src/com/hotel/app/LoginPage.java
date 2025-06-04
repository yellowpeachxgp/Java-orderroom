package com.hotel.app;

import com.database.helper.DatabaseHelper;
import com.hotel.auth.UserAuthManager;
import com.hotel.book.RoomBooking;
import com.hotel.ui.AnimationManager;
import com.hotel.ui.ModernTheme;
import com.hotel.auth.AuthService;
import com.hotel.auth.PasswordUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class LoginPage extends JFrame {
    // 静态常量
    private static final Logger LOGGER = Logger.getLogger(LoginPage.class.getName());
    private static final int LOGIN_TIMEOUT_SECONDS = 5;
    
    // 共享变量
    public static String enteredUsername;  // 用于存储用户输入的用户名
    
    // UI组件
    private JTextField usernameField; // 用户名输入框
    private JPasswordField passwordField; // 密码输入框
    private JButton loginButton; // 登录按钮
    private JPanel mainPanel; // 主面板
    private JRadioButton clientRadio; // 普通用户单选按钮
    private JRadioButton leaderRadio; // 管理员单选按钮
    private JCheckBox rememberAccountBox; // 记住账号复选框
    private JLabel statusLabel; // 状态显示标签
    
    // 线程管理
    private volatile boolean isLoginInProgress = false;
    private volatile boolean isDisposed = false;
    
    // 用户偏好存储
    private final Preferences prefs = Preferences.userNodeForPackage(LoginPage.class);

    public static void main(String[] args) {
        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.log(Level.SEVERE, "未捕获的异常", throwable);
            JOptionPane.showMessageDialog(
                null,
                "发生错误: " + throwable.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE
            );
        });
        
        // 使用SwingUtilities确保在EDT线程中创建UI
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置MacOS特定属性，避免IMKCFRunLoopWakeUpReliable错误
                System.setProperty("apple.awt.UIElement", "true");
                
                // 启动应用
                new LoginPage().LoginPage();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "启动应用时出错", e);
                JOptionPane.showMessageDialog(
                    null,
                    "启动应用时出错: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    public void LoginPage() {
        // 应用现代主题
        ModernTheme.applyTheme();
        
        // 快速测试数据库连接
        testDatabaseConnectionAsync();
        
        setTitle("酒店管理系统登录");
        setSize(400, 480);
        setUndecorated(true); // 无边框窗口
        setShape(new RoundRectangle2D.Double(0, 0, 400, 480, 15, 15)); // 设置圆角窗口形状
        
        // 创建主面板
        mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ModernTheme.BACKGROUND_COLOR);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.dispose();
            }
        };
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 窗口关闭按钮
        JButton closeButton = new JButton("×");
        closeButton.setForeground(ModernTheme.TEXT_SECONDARY);
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(ModernTheme.DANGER_COLOR);
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(ModernTheme.TEXT_SECONDARY);
            }
        });
        
        closeButton.addActionListener(e -> {
            closeApplication();
        });
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(closeButton, BorderLayout.EAST);
        
        // 创建标题面板
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("酒店管理系统");
        titleLabel.setFont(ModernTheme.TITLE_FONT);
        titleLabel.setForeground(ModernTheme.PRIMARY_COLOR);
        
        titlePanel.add(titleLabel);
        
        // 创建表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 10, 0);
        
        // 用户名标签和输入框
        JLabel usernameLabel = new JLabel("用户名");
        usernameLabel.setFont(ModernTheme.REGULAR_FONT);
        usernameLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        formPanel.add(usernameLabel, c);
        
        usernameField = new JTextField(20);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        formPanel.add(usernameField, c);
        
        // 密码标签和输入框
        JLabel passwordLabel = new JLabel("密码");
        passwordLabel.setFont(ModernTheme.REGULAR_FONT);
        passwordLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        c.gridx = 0;
        c.gridy = 2;
        formPanel.add(passwordLabel, c);
        
        passwordField = new JPasswordField(20);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        c.gridx = 0;
        c.gridy = 3;
        formPanel.add(passwordField, c);
        
        // 用户类型单选按钮组
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioPanel.setOpaque(false);
        
        clientRadio = new JRadioButton("普通用户");
        clientRadio.setFont(ModernTheme.SMALL_FONT);
        clientRadio.setForeground(ModernTheme.TEXT_PRIMARY);
        clientRadio.setOpaque(false);
        clientRadio.setSelected(true);
        
        leaderRadio = new JRadioButton("管理员");
        leaderRadio.setFont(ModernTheme.SMALL_FONT);
        leaderRadio.setForeground(ModernTheme.TEXT_PRIMARY);
        leaderRadio.setOpaque(false);
        
        ButtonGroup group = new ButtonGroup();
        group.add(clientRadio);
        group.add(leaderRadio);
        
        radioPanel.add(clientRadio);
        radioPanel.add(Box.createHorizontalStrut(20)); // 间距
        radioPanel.add(leaderRadio);
        
        c.gridx = 0;
        c.gridy = 4;
        formPanel.add(radioPanel, c);
        
        // 记住账号复选框
        rememberAccountBox = new JCheckBox("记住账号");
        rememberAccountBox.setFont(ModernTheme.SMALL_FONT);
        rememberAccountBox.setForeground(ModernTheme.TEXT_PRIMARY);
        rememberAccountBox.setOpaque(false);
        c.gridx = 0;
        c.gridy = 5;
        formPanel.add(rememberAccountBox, c);
        
        // 登录按钮
        loginButton = ModernTheme.createRoundedButton("登录");
        loginButton.setFont(ModernTheme.BUTTON_FONT);
        c.gridx = 0;
        c.gridy = 6;
        c.insets = new Insets(20, 0, 10, 0);
        formPanel.add(loginButton, c);
        
        // 状态标签
        statusLabel = new JLabel(" ");
        statusLabel.setFont(ModernTheme.SMALL_FONT);
        statusLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 0;
        c.gridy = 7;
        c.insets = new Insets(5, 0, 5, 0);
        formPanel.add(statusLabel, c);
        
        // 加载保存的用户名
        String savedUsername = prefs.get("savedUsername", "");
        boolean rememberAccount = prefs.getBoolean("rememberAccount", false);
        if (!savedUsername.isEmpty() && rememberAccount) {
            usernameField.setText(savedUsername);
            rememberAccountBox.setSelected(true);
        }
        
        // 给登录按钮添加点击事件
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 防止重复点击
                if (isLoginInProgress) {
                    return;
                }
                
                // 获取用户名
                enteredUsername = usernameField.getText().trim();
                
                // 验证用户名是否为空
                if (enteredUsername.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        LoginPage.this, 
                        "请输入用户名", 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                
                // 保存用户名（如果选择了记住账号）
                if (rememberAccountBox.isSelected()) {
                    prefs.put("savedUsername", enteredUsername);
                    prefs.putBoolean("rememberAccount", true);
                } else {
                    prefs.remove("savedUsername");
                    prefs.putBoolean("rememberAccount", false);
                }
                
                // 禁用登录按钮，防止多次点击
                loginButton.setEnabled(false);
                isLoginInProgress = true;
                
                // 显示登录状态
                updateStatus("正在登录...", false);
                
                // 使用简单线程执行登录操作
                Thread loginThread = new Thread(() -> {
                    try {
                        // 执行登录
                        final boolean loginSuccess = performLogin();
                        
                        // 在EDT线程中更新UI
                        if (!isDisposed) {
                            SwingUtilities.invokeLater(() -> {
                                if (loginSuccess) {
                                    // 登录成功的处理
                                    JOptionPane.showMessageDialog(
                                        LoginPage.this, 
                                        "登录成功", 
                                        "提示", 
                                        JOptionPane.INFORMATION_MESSAGE
                                    );
                                    
                                    // 启动相应界面
                                    if (UserAuthManager.isCurrentUserAdmin()) {
                                        startMainMenu();
                                    } else {
                                        startClientMenu();
                                    }
                                    dispose();
                                } else {
                                    // 恢复登录按钮
                                    loginButton.setEnabled(true);
                                    isLoginInProgress = false;
                                    updateStatus(" ", false);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "登录过程中出现异常", ex);
                        if (!isDisposed) {
                            SwingUtilities.invokeLater(() -> {
                                loginButton.setEnabled(true);
                                isLoginInProgress = false;
                                updateStatus(" ", false);
                                JOptionPane.showMessageDialog(
                                    LoginPage.this,
                                    "登录时出错: " + ex.getMessage(),
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE
                                );
                            });
                        }
                    }
                });
                
                // 设置为守护线程
                loginThread.setDaemon(true);
                loginThread.start();
                
                // 设置登录超时
                new Thread(() -> {
                    try {
                        Thread.sleep(LOGIN_TIMEOUT_SECONDS * 1000);
                        if (isLoginInProgress && !isDisposed) {
                            SwingUtilities.invokeLater(() -> {
                                loginButton.setEnabled(true);
                                isLoginInProgress = false;
                                updateStatus(" ", false);
                                JOptionPane.showMessageDialog(
                                    LoginPage.this,
                                    "登录超时，请检查网络连接后重试",
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE
                                );
                            });
                        }
                    } catch (InterruptedException ignored) {
                        // 忽略中断异常
                    }
                }).start();
            }
        });
        
        // 添加回车键快捷登录
        ActionListener loginAction = e -> loginButton.doClick();
        usernameField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);
        
        // 设置窗口可拖动
        final Point[] dragPoint = {null};
        mainPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                dragPoint[0] = evt.getPoint();
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                dragPoint[0] = null;
            }
        });
        
        mainPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                if (dragPoint[0] != null) {
                    Point currentLocation = getLocation();
                    setLocation(
                        currentLocation.x + evt.getX() - dragPoint[0].x,
                        currentLocation.y + evt.getY() - dragPoint[0].y
                    );
                }
            }
        });
        
        // 组装界面
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        setLocationRelativeTo(null); // 将窗口位置设置为屏幕中央
        setVisible(true); // 设置窗口可见
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置窗口关闭时的操作
        
        // 应用淡入动画
        mainPanel.setVisible(false);
        AnimationManager.fadeIn(mainPanel, 500);
        
        // 聚焦用户名输入框
        if (usernameField.getText().isEmpty()) {
            usernameField.requestFocusInWindow();
        } else {
            passwordField.requestFocusInWindow();
        }
        
        // 添加窗口关闭监听器
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeApplication();
            }
        });
    }
    
    /**
     * 异步测试数据库连接
     */
    private void testDatabaseConnectionAsync() {
        new Thread(() -> {
            Connection conn = null;
            try {
                updateStatus("正在连接数据库...", true);
                conn = DatabaseHelper.getConnection();
                
                if (conn != null && DatabaseHelper.isConnectionValid(conn)) {
                    updateStatus("数据库连接正常", true);
                    Thread.sleep(1000);
                    updateStatus(" ", true);
                } else {
                    updateStatus("数据库连接失败", true);
                    LOGGER.warning("数据库连接失败");
                }
            } catch (Exception e) {
                updateStatus("数据库连接出错", true);
                LOGGER.log(Level.WARNING, "测试数据库连接时出错", e);
            } finally {
                DatabaseHelper.closeConnection(conn);
            }
        }).start();
    }
    
    /**
     * 更新状态标签
     */
    private void updateStatus(String message, boolean showInLog) {
        if (showInLog) {
            LOGGER.info(message);
        }
        
        if (!isDisposed) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(message);
            });
        }
    }
    
    /**
     * 关闭应用程序
     */
    private void closeApplication() {
        isDisposed = true;
        dispose();
        System.exit(0);
    }
    
    /**
     * 执行登录操作
     */
    private boolean performLogin() {
        // 获取密码
        String password = new String(passwordField.getPassword());
        
        // 用户类型
        int userType = clientRadio.isSelected() ? UserAuthManager.USER_TYPE_CLIENT : UserAuthManager.USER_TYPE_LEADER;
        
        try {
            // 首先快速测试数据库连接
            updateStatus("正在连接数据库...", true);
            Connection testConn = null;
            try {
                testConn = DatabaseHelper.getConnection();
                if (testConn == null || !DatabaseHelper.isConnectionValid(testConn)) {
                    updateStatus("数据库连接失败", true);
                    showError("无法连接到数据库，请检查网络连接");
                    return false;
                }
                updateStatus("正在验证账户...", true);
            } finally {
                DatabaseHelper.closeConnection(testConn);
            }
            
            // 首先尝试使用传统登录方式，因为大多数用户还没有注册到新的auth系统
            boolean legacyLoginSuccess = legacyCheckLogin();
            if (legacyLoginSuccess) {
                return true;
            }
            
            // 如果传统登录失败，尝试使用AuthService的登录方法
            try {
                AuthService.LoginResult result = AuthService.login(enteredUsername, password);
                
                if (result.isSuccess()) {
                    // 设置当前用户信息
                    UserAuthManager.setCurrentUser(
                        result.getUserId(),
                        result.getUserName(),
                        result.getAccountType(),
                        result.isFirstLogin()
                    );
                    
                    // 如果是首次登录，提示修改默认密码
                    if (result.isFirstLogin()) {
                        showInfo("这是您首次登录，建议您立即修改密码", "安全提示");
                    }
                    
                    return true;
                } else {
                    // 显示错误消息
                    updateStatus("登录失败", true);
                    showError(result.getErrorMessage(), "登录失败");
                    return false;
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "新认证方式登录失败", e);
                // 已经尝试过传统登录，所以这里直接返回失败
                updateStatus("登录失败", true);
                showError("用户名或密码错误");
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "登录过程中出现异常", e);
            updateStatus("登录异常", true);
            showError("登录失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 在EDT线程中显示错误消息
     */
    private void showError(String message) {
        showError(message, "错误");
    }
    
    /**
     * 在EDT线程中显示错误消息，带标题
     */
    private void showError(String message, String title) {
        if (!isDisposed) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    /**
     * 在EDT线程中显示信息
     */
    private void showInfo(String message, String title) {
        if (!isDisposed) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
            });
        }
    }
    
    /**
     * 传统登录方法，当新登录方式失败时使用
     */
    private boolean legacyCheckLogin() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseHelper.getConnection(); // 获取数据库连接
            
            if (connection == null) {
                updateStatus("数据库连接失败", true);
                showError("无法连接到数据库，请检查网络连接", "连接错误");
                return false;
            }
            
            // 检查连接是否有效
            if (!DatabaseHelper.isConnectionValid(connection)) {
                updateStatus("数据库连接无效", true);
                showError("数据库连接无效，请重试", "连接错误");
                return false;
            }
            
            // 根据用户类型选择查询不同的表
            String tableName = clientRadio.isSelected() ? "client" : "leader";
            String idField = clientRadio.isSelected() ? "cNumber" : "lNumber";
            String nameField = clientRadio.isSelected() ? "cName" : "lName";
            int userType = clientRadio.isSelected() ? UserAuthManager.USER_TYPE_CLIENT : UserAuthManager.USER_TYPE_LEADER;
            
            updateStatus("正在查询用户信息...", true);
            String query = "SELECT * FROM " + tableName + " WHERE " + idField + " = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, enteredUsername);
            preparedStatement.setQueryTimeout(3); // 设置3秒查询超时
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                // 获取用户名称
                String userName = resultSet.getString(nameField);
                
                // 登录成功，设置用户信息
                UserAuthManager.setCurrentUser(enteredUsername, userName, userType, false);
                updateStatus("登录成功", true);
                
                return true;
            } else {
                updateStatus("用户不存在", true);
                showError("用户名不存在，请检查输入", "登录失败");
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "传统登录方式SQL异常", e);
            updateStatus("登录错误: " + e.getMessage(), true);
            showError("登录时发生错误: " + e.getMessage(), "数据库错误");
            return false;
        } finally {
            // 确保关闭所有资源
            DatabaseHelper.closeResources(connection, preparedStatement, resultSet);
        }
    }

    /**
     * 启动管理员主菜单
     */
    private void startMainMenu() {
        try {
            MainMenu.main(new String[]{});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "启动主菜单失败", e);
        }
    }
    
    /**
     * 启动普通用户菜单
     */
    private void startClientMenu() {
        try {
            runRoomBook();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "启动客户菜单失败", e);
        }
    }
    
    /**
     * 启动房间预订界面
     */
    private void runRoomBook() {
        SwingUtilities.invokeLater(() -> {
            try {
                // 先检查是否已经登录
                if (UserAuthManager.getCurrentUserId() == null || UserAuthManager.getCurrentUserId().isEmpty()) {
                    LOGGER.warning("尝试启动预订界面时未检测到有效的用户登录");
                    JOptionPane.showMessageDialog(
                        this,
                        "登录会话无效，请重新登录",
                        "会话错误",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                
                RoomBooking roomBooking = new RoomBooking(enteredUsername);
                roomBooking.setVisible(true);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "启动房间预订界面失败", e);
                JOptionPane.showMessageDialog(
                    this,
                    "启动房间预订界面失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
    
    @Override
    public void dispose() {
        isDisposed = true;
        super.dispose();
    }
}
