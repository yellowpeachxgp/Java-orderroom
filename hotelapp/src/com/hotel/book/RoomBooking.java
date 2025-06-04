package com.hotel.book;

import com.database.helper.DatabaseHelper;
import com.hotel.app.LoginPage;
import com.hotel.app.MainMenu;
import com.hotel.ui.AnimationManager;
import com.hotel.ui.ModernTableRenderer;
import com.hotel.ui.ModernTheme;
import com.hotel.auth.UserAuthManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomBooking extends JFrame {
    // 静态常量
    private static final Logger LOGGER = Logger.getLogger(RoomBooking.class.getName());
    private static final int DB_TIMEOUT_SECONDS = 5;
    private static final int BOOKING_TIMEOUT_SECONDS = 15; // 增加预订超时时间
    
    // UI组件
    private JTable table;
    private JButton confirmButton;
    private JButton queryButton;
    private JButton backButton;
    private JPanel mainPanel;
    
    // 数据字段
    private String selectedRNumber; // 保存用户选择的 rNumber
    private String enteredUsername; // 保存用户登录的用户名

    // 线程安全控制
    private final AtomicBoolean isBookingInProgress = new AtomicBoolean(false);
    private volatile boolean isWindowClosing = false; // 添加窗口关闭标志

    public RoomBooking(String username) {
        this.enteredUsername = username;  // 保存用户登录的用户名
        
        // 验证用户名
        if (username == null || username.trim().isEmpty()) {
            LOGGER.warning("用户名为空，使用当前登录用户");
            this.enteredUsername = UserAuthManager.getCurrentUserId();
            if (this.enteredUsername == null) {
                this.enteredUsername = "GUEST";
            }
        }
        
        LOGGER.info("初始化房间预订界面，用户: " + this.enteredUsername);
        
        // 应用现代主题
        ModernTheme.applyTheme();
        
        setTitle("酒店客房预订系统");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 关闭当前窗口而不是整个应用
        
        // 添加窗口关闭监听器
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                isWindowClosing = true;
                LOGGER.info("房间预订窗口正在关闭");
            }
        });
        
        // 创建主面板
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(ModernTheme.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 创建顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        // 创建标题
        JLabel titleLabel = ModernTheme.createTitleLabel("客房预订系统");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 创建欢迎信息
        JLabel welcomeLabel = new JLabel("欢迎, 客户ID: " + username);
        welcomeLabel.setFont(ModernTheme.REGULAR_FONT);
        welcomeLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(welcomeLabel, BorderLayout.EAST);
        
        // 创建中间内容面板（用于放置表格）
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10)) {
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
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // 创建表格说明面板
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);
        
        JLabel tableTitle = new JLabel("可预订客房列表");
        tableTitle.setFont(ModernTheme.HEADER_FONT);
        tableTitle.setForeground(ModernTheme.PRIMARY_DARK_COLOR);
        tableTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);

        // 创建表格模型
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 设置表格不可编辑
            }
        };
        model.addColumn("房间号");
        model.addColumn("房间类型");
        model.addColumn("价格");
        model.addColumn("状态");  // 新增状态列

        // 在后台线程加载数据
        loadRoomData(model);

        // 创建表格并添加到界面
        table = new JTable(model);
        // 应用现代表格样式
        ModernTableRenderer.applyModernStyle(table);
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1) {
                        selectedRNumber = (String) table.getValueAt(selectedRow, 0); // 保存用户选择的 rNumber
                        
                        // 检查状态列的值
                        String status = "";
                        if (table.getColumnCount() > 3) {
                            status = (String) table.getValueAt(selectedRow, 3);
                        }
                        
                        // 只有当状态不是"已预订"时才启用按钮
                        if (!"已预订".equals(status)) {
                            confirmButton.setEnabled(true);
                        } else {
                            confirmButton.setEnabled(false);
                            JOptionPane.showMessageDialog(
                                RoomBooking.this, 
                                "该房间已被预订，请选择其他房间", 
                                "提示", 
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ModernTheme.CARD_COLOR);
        
        // 添加表格说明和表格到内容面板
        contentPanel.add(tableHeaderPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 创建底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        // 创建刷新按钮
        JButton refreshButton = ModernTheme.createRoundedButton("刷新房间列表");
        refreshButton.setBackground(ModernTheme.SUCCESS_COLOR);
        AnimationManager.addButtonClickAnimation(refreshButton);

        // 创建确定按钮
        confirmButton = ModernTheme.createRoundedButton("预订选中房间");
        confirmButton.setEnabled(false); // 初始禁用按钮，直到选择了房间
        AnimationManager.addButtonClickAnimation(confirmButton);
        
        // 创建查询预约按钮
        queryButton = ModernTheme.createRoundedButton("查询我的预约");
        AnimationManager.addButtonClickAnimation(queryButton);
        
        // 创建返回主菜单按钮
        backButton = ModernTheme.createRoundedButton("返回主菜单");
        backButton.setBackground(ModernTheme.ACCENT_COLOR);
        AnimationManager.addButtonClickAnimation(backButton);
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(queryButton);
        buttonPanel.add(confirmButton);
        buttonPanel.add(backButton);
        
        // 添加所有面板到主面板
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 添加主面板到窗口
        add(mainPanel);
        
        // 刷新按钮事件
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshButton.setEnabled(false);
                
                // 清空当前表格数据
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.setRowCount(0);
                
                // 重新加载数据
                loadRoomData(model);
                
                // 重置选择
                selectedRNumber = null;
                confirmButton.setEnabled(false);
                
                // 恢复按钮状态
                refreshButton.setEnabled(true);
            }
        });
        
        // 按钮事件
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedRNumber != null) {
                    // 防止重复点击 - 使用原子操作确保线程安全
                    if (!isBookingInProgress.compareAndSet(false, true)) {
                        LOGGER.info("预订操作正在进行中，忽略重复点击");
                        return;
                    }
                    
                    LOGGER.info("开始预订流程，房间: " + selectedRNumber + ", 用户: " + enteredUsername);
                    
                    // 禁用按钮防止重复点击
                    confirmButton.setEnabled(false);
                    
                    // 创建进度指示器
                    JDialog loadingDialog = createLoadingDialog("正在处理预订请求...");
                    
                    // 使用CompletableFuture替代原始Thread，提供更好的异常处理和超时控制
                    CompletableFuture<Boolean> bookingFuture = CompletableFuture.supplyAsync(() -> {
                        try {
                            // 验证窗口状态
                            if (isWindowClosing) {
                                LOGGER.info("窗口正在关闭，取消预订操作");
                                return false;
                            }
                            
                            // 模拟处理延迟
                            Thread.sleep(200);
                            
                            LOGGER.info("开始预订房间: " + selectedRNumber + " 用户: " + enteredUsername);
                            
                            // 检查房间状态
                            if (checkRoomAvailability(selectedRNumber)) {
                                // 更新房间状态和添加预订信息
                                updateRoomState(selectedRNumber);
                                bookUpdate(selectedRNumber, enteredUsername);
                                
                                LOGGER.info("预订完成: " + selectedRNumber);
                                return true;
                            } else {
                                LOGGER.warning("房间预订失败 - 房间已被占用: " + selectedRNumber);
                                return false;
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "预订过程中出错", ex);
                            throw new RuntimeException(ex);
                        }
                    });
                    
                    // 显示对话框
                    SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
                    
                    // 处理预订结果
                    bookingFuture.orTimeout(BOOKING_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .whenComplete((success, throwable) -> {
                            SwingUtilities.invokeLater(() -> {
                                try {
                                    // 检查窗口是否已关闭
                                    if (isWindowClosing) {
                                        LOGGER.info("窗口已关闭，跳过UI更新");
                                        return;
                                    }
                                    
                                    // 关闭进度对话框
                                    if (loadingDialog.isDisplayable()) {
                                        loadingDialog.dispose();
                                    }
                                    
                                    if (throwable != null) {
                                        // 处理异常
                                        String errorMessage;
                                        if (throwable instanceof java.util.concurrent.TimeoutException) {
                                            errorMessage = "预订操作超时，请稍后重试";
                                            LOGGER.warning("预订操作超时: " + selectedRNumber);
                                        } else {
                                            errorMessage = "预订失败: " + throwable.getMessage();
                                            LOGGER.log(Level.SEVERE, "预订操作失败", throwable);
                                        }
                                        
                                        JOptionPane.showMessageDialog(
                                            RoomBooking.this, 
                                            errorMessage, 
                                            "错误", 
                                            JOptionPane.ERROR_MESSAGE
                                        );
                                    } else if (success != null && success) {
                                        // 预订成功
                                        JOptionPane.showMessageDialog(
                                            RoomBooking.this, 
                                            "房间 " + selectedRNumber + " 预订成功！", 
                                            "预订成功", 
                                            JOptionPane.INFORMATION_MESSAGE
                                        );
                                        
                                        // 重新加载表格数据
                                        refreshTableData();
                                        
                                        // 清除选择
                                        table.clearSelection();
                                        selectedRNumber = null;
                                    } else {
                                        // 预订失败（房间已被预订）
                                        JOptionPane.showMessageDialog(
                                            RoomBooking.this, 
                                            "该房间已被其他用户预订，请选择其他房间", 
                                            "提示", 
                                            JOptionPane.INFORMATION_MESSAGE
                                        );
                                        
                                        // 重新加载表格数据
                                        refreshTableData();
                                        
                                        // 清除选择
                                        table.clearSelection();
                                        selectedRNumber = null;
                                    }
                                } catch (Exception ex) {
                                    LOGGER.log(Level.SEVERE, "更新UI时发生错误", ex);
                                } finally {
                                    // 重置状态并重新启用按钮
                                    confirmButton.setEnabled(true);
                                    isBookingInProgress.set(false);
                                }
                            });
                        });
                        
                } else {
                    JOptionPane.showMessageDialog(RoomBooking.this, "请选择一个房间");
                }
            }
        });
        
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 禁用按钮防止重复点击
                queryButton.setEnabled(false);
                try {
                CancelBook.main(new String[]{});
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "启动预订查询界面失败", ex);
                    JOptionPane.showMessageDialog(
                        RoomBooking.this,
                        "启动预订查询界面失败: " + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    // 重新启用按钮
                    queryButton.setEnabled(true);
                }
            }
        });
        
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭当前窗口
                try {
                    MainMenu.main(new String[]{}); // 返回主菜单
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "返回主菜单失败", ex);
                }
            }
        });
        
        // 显示窗口
        setVisible(true);
        
        // 应用淡入动画
        mainPanel.setVisible(false);
        AnimationManager.fadeIn(mainPanel, 500);
    }
    
    /**
     * 刷新表格数据
     */
    private void refreshTableData() {
        if (isWindowClosing) {
            LOGGER.info("窗口正在关闭，跳过表格刷新");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        loadRoomData(model);
    }

    /**
     * 异步加载房间数据
     */
    private void loadRoomData(DefaultTableModel model) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (isWindowClosing) {
                    return null;
                }
                
                Connection conn = null;
                PreparedStatement stmt = null;
                ResultSet rs = null;
                
                try {
                    LOGGER.info("开始加载房间数据");
                    conn = DatabaseHelper.getConnection();
                    if (conn == null) {
                        throw new SQLException("无法连接到数据库");
                    }
                    
                    // 设置查询超时的正确方式 - 使用PreparedStatement的超时设置
                    try {
                        // 检查rooms表是否存在
                        DatabaseMetaData meta = conn.getMetaData();
                        rs = meta.getTables(null, null, "rooms", new String[] {"TABLE"});
                        boolean roomsTableExists = rs.next();
                        rs.close();
                        
                        if (!roomsTableExists) {
                            SwingUtilities.invokeLater(() -> {
                                if (!isWindowClosing) {
                                    JOptionPane.showMessageDialog(
                                        RoomBooking.this,
                                        "系统中没有房间数据，请先添加房间",
                                        "提示",
                                        JOptionPane.INFORMATION_MESSAGE
                                    );
                                }
                            });
                            return null;
                        }
                        
                        // 查询房间信息和状态
                        String query = "SELECT r.rNumber, r.rType, r.rPrice, " +
                                      "COALESCE(rs.state, '可预订') as state " +
                                      "FROM rooms r " +
                                      "LEFT JOIN roomstate rs ON r.rNumber = rs.rNumber " +
                                      "WHERE COALESCE(rs.state, '可预订') != '已预订' OR rs.state IS NULL " +
                                      "ORDER BY r.rNumber";
                        
                        stmt = conn.prepareStatement(query);
                        stmt.setQueryTimeout(DB_TIMEOUT_SECONDS);
                        rs = stmt.executeQuery();
                        
                        // 检查是否有房间数据
                        boolean hasRooms = false;
                        
                        while (rs.next() && !isWindowClosing) {
                            hasRooms = true;
                            final String rNumber = rs.getString("rNumber");
                            final String rType = rs.getString("rType");
                            final String rPrice = rs.getString("rPrice");
                            final String state = rs.getString("state");
                            
                            // 在EDT线程中更新UI
                            SwingUtilities.invokeLater(() -> {
                                if (!isWindowClosing) {
                                    model.addRow(new Object[]{
                                        rNumber,
                                        rType,
                                        rPrice,
                                        state != null ? state : "可预订"
                                    });
                                }
                            });
                        }
                        
                        // 如果没有房间数据，显示提示
                        if (!hasRooms && !isWindowClosing) {
                            SwingUtilities.invokeLater(() -> {
                                if (!isWindowClosing) {
                                    JOptionPane.showMessageDialog(
                                        RoomBooking.this,
                                        "系统中没有可预订的房间",
                                        "提示",
                                        JOptionPane.INFORMATION_MESSAGE
                                    );
                                }
                            });
                        }
                        
                        LOGGER.info("房间数据加载完成，共 " + model.getRowCount() + " 条记录");
                        
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "查询房间数据失败", e);
                        SwingUtilities.invokeLater(() -> {
                            if (!isWindowClosing) {
                                JOptionPane.showMessageDialog(
                                    RoomBooking.this,
                                    "加载房间数据失败: " + e.getMessage(),
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE
                                );
                            }
                        });
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "加载房间数据时发生异常", e);
                    SwingUtilities.invokeLater(() -> {
                        if (!isWindowClosing) {
                            JOptionPane.showMessageDialog(
                                RoomBooking.this,
                                "系统错误: " + e.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    });
                } finally {
                    // 确保关闭所有资源
                    DatabaseHelper.closeResources(conn, stmt, rs);
                }
                
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get(); // 检查是否有异常
                    LOGGER.info("房间数据加载任务完成");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "房间数据加载任务异常", e);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * 检查房间是否可用
     */
    private boolean checkRoomAvailability(String roomNumber) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            LOGGER.info("正在检查房间可用性: " + roomNumber);
            
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                throw new SQLException("无法连接到数据库");
            }
            
            // 验证数据库连接有效性
            if (!DatabaseHelper.isConnectionValid(conn)) {
                throw new SQLException("数据库连接无效");
            }
            
            // 检查roomstate表是否存在
            DatabaseMetaData meta = conn.getMetaData();
            rs = meta.getTables(null, null, "roomstate", new String[] {"TABLE"});
            boolean tableExists = rs.next();
            rs.close();
            
            if (!tableExists) {
                // 如果表不存在，说明没有任何房间被预订
                LOGGER.info("roomstate表不存在，房间可用: " + roomNumber);
                return true;
            }
            
            // 检查房间状态
            String query = "SELECT state FROM roomstate WHERE rNumber = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, roomNumber);
            stmt.setQueryTimeout(DB_TIMEOUT_SECONDS);
            rs = stmt.executeQuery();
            
            // 如果没有记录或状态不是"已预订"，则房间可用
            if (!rs.next()) {
                LOGGER.info("房间未找到状态记录，房间可用: " + roomNumber);
                return true;
            }
            
            String state = rs.getString("state");
            boolean isAvailable = !"已预订".equals(state);
            LOGGER.info("房间状态检查完成: " + roomNumber + ", 状态: " + state + ", 可用: " + isAvailable);
            
            return isAvailable;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "检查房间可用性时出错: " + roomNumber, e);
            throw e;
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * 创建加载对话框
     */
    private JDialog createLoadingDialog(String message) {
        JDialog dialog = new JDialog(this, "处理中", true);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        
        // 创建面板
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
        
        // 创建消息标签
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(ModernTheme.REGULAR_FONT);
        messageLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(messageLabel, BorderLayout.CENTER);
        dialog.add(panel);
        
        return dialog;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                String username = LoginPage.enteredUsername;
                RoomBooking roomBooking = new RoomBooking(username);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "启动房间预订界面失败", e);
                    JOptionPane.showMessageDialog(
                        null,
                        "启动房间预订界面失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }

    private void updateRoomState(String selectedRNumber) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            LOGGER.info("开始更新房间状态: " + selectedRNumber);
            
            connection = DatabaseHelper.getConnection();
            if (connection == null) {
                throw new SQLException("无法连接数据库");
            }
            
            // 启动事务管理，确保操作的一致性
            connection.setAutoCommit(false);
            
            try {
                // 首先检查roomstate表是否存在
                boolean tableExists = false;
                try {
                    DatabaseMetaData meta = connection.getMetaData();
                    resultSet = meta.getTables(null, null, "roomstate", new String[] {"TABLE"});
                    tableExists = resultSet.next();
                    resultSet.close();
                    resultSet = null;
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "检查表是否存在时出错", e);
                    // 继续执行，尝试创建表
                }
                
                // 如果表不存在，创建表
                if (!tableExists) {
                    LOGGER.info("roomstate表不存在，正在创建...");
                    String createTableSQL = "CREATE TABLE roomstate (" +
                                          "rNumber VARCHAR(50) NOT NULL PRIMARY KEY, " +
                                          "state VARCHAR(50) NOT NULL)";
                    preparedStatement = connection.prepareStatement(createTableSQL);
                    preparedStatement.setQueryTimeout(DB_TIMEOUT_SECONDS);
                    preparedStatement.executeUpdate();
                    LOGGER.info("roomstate表创建成功");
                    
                    // 关闭当前PreparedStatement以便后续使用
                    preparedStatement.close();
                    preparedStatement = null;
                }
                
                // 再次检查房间是否已被预订（双重检查，避免并发问题）
                preparedStatement = connection.prepareStatement(
                    "SELECT state FROM roomstate WHERE rNumber = ? FOR UPDATE"
                );
                preparedStatement.setString(1, selectedRNumber);
                preparedStatement.setQueryTimeout(DB_TIMEOUT_SECONDS);
                resultSet = preparedStatement.executeQuery();
                
                if (resultSet.next() && "已预订".equals(resultSet.getString("state"))) {
                    // 房间已被预订，回滚事务
                    connection.rollback();
                    LOGGER.warning("房间已被预订，无法更新状态: " + selectedRNumber);
                    throw new SQLException("该房间已被其他用户预订");
                }
                
                // 关闭之前的结果集和语句
                resultSet.close();
                preparedStatement.close();
                
                // 检查是否已有该房间的记录
                String checkSQL = "SELECT * FROM roomstate WHERE rNumber = ?";
                preparedStatement = connection.prepareStatement(checkSQL);
                preparedStatement.setString(1, selectedRNumber);
                preparedStatement.setQueryTimeout(DB_TIMEOUT_SECONDS);
                resultSet = preparedStatement.executeQuery();
                
                if (resultSet.next()) {
                    // 如果已有记录，更新状态
                    preparedStatement.close();
                    String updateSQL = "UPDATE roomstate SET state = '已预订' WHERE rNumber = ?";
                    preparedStatement = connection.prepareStatement(updateSQL);
                    preparedStatement.setString(1, selectedRNumber);
                    preparedStatement.setQueryTimeout(DB_TIMEOUT_SECONDS);
                    int updatedRows = preparedStatement.executeUpdate();
                    LOGGER.info("更新房间状态记录: " + selectedRNumber + ", 影响行数: " + updatedRows);
                } else {
                    // 如果没有记录，插入新记录
                    preparedStatement.close();
                    String insertSQL = "INSERT INTO roomstate (rNumber, state) VALUES (?, '已预订')";
                    preparedStatement = connection.prepareStatement(insertSQL);
                    preparedStatement.setString(1, selectedRNumber);
                    preparedStatement.setQueryTimeout(DB_TIMEOUT_SECONDS);
                    int insertedRows = preparedStatement.executeUpdate();
                    LOGGER.info("插入房间状态记录: " + selectedRNumber + ", 影响行数: " + insertedRows);
                }
                
                // 提交事务
                connection.commit();
                LOGGER.info("房间状态更新成功: " + selectedRNumber);
            } catch (SQLException e) {
                // 发生错误时回滚事务
                if (connection != null) {
                    try {
                        connection.rollback();
                        LOGGER.warning("房间状态更新失败，已回滚事务: " + selectedRNumber);
                    } catch (SQLException ex) {
                        LOGGER.log(Level.SEVERE, "回滚事务失败", ex);
                    }
                }
                throw e;
            } finally {
                // 恢复自动提交
                if (connection != null) {
                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "恢复自动提交失败", e);
                    }
                }
            }
        } finally {
            // 关闭所有资源
            DatabaseHelper.closeResources(connection, preparedStatement, resultSet);
        }
    }

    private void bookUpdate(String selectedRNumber, String enteredUsername) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            LOGGER.info("开始添加预订记录: 房间=" + selectedRNumber + ", 用户=" + enteredUsername);
            
            connection = DatabaseHelper.getConnection();
            if (connection == null) {
                throw new SQLException("无法连接数据库");
            }
            
            // 启动事务管理
            connection.setAutoCommit(false);
            
            try {
                // 首先检查bookroom表是否存在
                boolean tableExists = false;
                try {
                    DatabaseMetaData meta = connection.getMetaData();
                    resultSet = meta.getTables(null, null, "bookroom", new String[] {"TABLE"});
                    tableExists = resultSet.next();
                    resultSet.close();
                    resultSet = null;
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "检查表是否存在时出错", e);
                    // 继续执行，尝试创建表
                }
                
                // 如果表不存在，创建表
                if (!tableExists) {
                    LOGGER.info("bookroom表不存在，正在创建...");
                    String createTableSQL = "CREATE TABLE bookroom (" +
                                          "bookNumber VARCHAR(50) NOT NULL PRIMARY KEY, " +
                                          "cNumber VARCHAR(50) NOT NULL, " +
                                          "rNumber VARCHAR(50) NOT NULL, " +
                                          "bookdate VARCHAR(50) NOT NULL, " +
                                          "checkin VARCHAR(50) NOT NULL)";
                    preparedStatement = connection.prepareStatement(createTableSQL);
                    preparedStatement.setQueryTimeout(DB_TIMEOUT_SECONDS);
                    preparedStatement.executeUpdate();
                    LOGGER.info("bookroom表创建成功");
                    
                    // 关闭当前PreparedStatement以便后续使用
                    preparedStatement.close();
                    preparedStatement = null;
                }
                
                // 检查该房间是否已经被预订
                String checkSQL = "SELECT COUNT(*) FROM bookroom WHERE rNumber = ? AND checkin = '未入住'";
                preparedStatement = connection.prepareStatement(checkSQL);
                preparedStatement.setString(1, selectedRNumber);
                preparedStatement.setQueryTimeout(DB_TIMEOUT_SECONDS);
                resultSet = preparedStatement.executeQuery();
                
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    connection.rollback();
                    LOGGER.warning("房间已存在未入住预订记录: " + selectedRNumber);
                    throw new SQLException("该房间已被预订");
                }
                
                resultSet.close();
                preparedStatement.close();
                
                // 获取当前日期
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String currentDateStr = dateFormat.format(currentDate);

                // 生成预订号
                String bookNumber = generateBookNumber();
                LOGGER.info("生成预订号: " + bookNumber);

                // 插入预订记录
                String insertSQL = "INSERT INTO bookroom (bookNumber, cNumber, rNumber, bookdate, checkin) VALUES (?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(insertSQL);
                preparedStatement.setString(1, bookNumber);
                preparedStatement.setString(2, enteredUsername);
                preparedStatement.setString(3, selectedRNumber);
                preparedStatement.setString(4, currentDateStr);
                preparedStatement.setString(5, "未入住");
                preparedStatement.setQueryTimeout(DB_TIMEOUT_SECONDS);
                int insertedRows = preparedStatement.executeUpdate();
                
                // 提交事务
                connection.commit();
                LOGGER.info("预订信息已添加成功: 房间=" + selectedRNumber + ", 用户=" + enteredUsername + 
                           ", 预订号=" + bookNumber + ", 影响行数=" + insertedRows);
            } catch (SQLException e) {
                // 发生错误时回滚事务
                if (connection != null) {
                    try {
                        connection.rollback();
                        LOGGER.warning("预订记录添加失败，已回滚事务: " + selectedRNumber);
                    } catch (SQLException ex) {
                        LOGGER.log(Level.SEVERE, "回滚事务失败", ex);
                    }
                }
                throw e;
            } finally {
                // 恢复自动提交
                if (connection != null) {
                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "恢复自动提交失败", e);
                    }
                }
            }
        } finally {
            // 确保关闭所有资源
            DatabaseHelper.closeResources(connection, preparedStatement, resultSet);
        }
    }

    private String generateBookNumber() {
        // 创建一个随机生成预定序号的方法
        UUID uuid = UUID.randomUUID();
        String randomBookingNumber = uuid.toString().replace("-", "").substring(5, 11);
        return "BOOK" + randomBookingNumber;
    }
}