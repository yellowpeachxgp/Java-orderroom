package com.hotel.book;

import com.database.helper.DatabaseHelper;
import com.hotel.app.LoginPage;
import com.hotel.app.MainMenu;
import com.hotel.ui.AnimationManager;
import com.hotel.ui.ModernTableRenderer;
import com.hotel.ui.ModernTheme;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CancelBook extends JFrame {
    // 静态常量
    private static final Logger LOGGER = Logger.getLogger(CancelBook.class.getName());
    private static final int DB_TIMEOUT_SECONDS = 5;
    
    // UI组件
    private JTable table; // 表格
    private JButton confirmButton; // 确认按钮
    private JButton backButton; // 返回按钮
    private JButton menuButton; // 主菜单按钮
    private JButton refreshButton; // 刷新按钮
    private JPanel mainPanel; // 主面板
    
    // 数据字段
    private String selectedRNumber; // 保存用户选择的房间编号
    private String selectedBookNumber; // 保存用户选择的预订编号
    private String enteredUsername; // 保存用户登录的用户名
    
    // 线程安全控制
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    // 构造函数
    public CancelBook(String username) {
        this.enteredUsername = username;  // 保存用户登录的用户名
        
        // 应用现代主题
        ModernTheme.applyTheme();
        
        setTitle("预订管理"); // 设置窗口标题
        setSize(800, 600); // 设置窗口大小
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 设置窗口关闭操作为销毁窗口
        setLocationRelativeTo(null); // 将窗口位置设置为屏幕中央
        
        // 创建主面板
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(ModernTheme.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 创建顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        // 创建标题
        JLabel titleLabel = ModernTheme.createTitleLabel("我的预订管理");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 创建客户信息
        JLabel clientLabel = new JLabel("客户ID: " + username);
        clientLabel.setFont(ModernTheme.REGULAR_FONT);
        clientLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(clientLabel, BorderLayout.EAST);
        
        // 创建内容面板
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
        
        JLabel tableTitle = new JLabel("我的预订记录");
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
        model.addColumn("预约编号");
        model.addColumn("用户编号");
        model.addColumn("房间编号");
        model.addColumn("预约日期");
        model.addColumn("入住情况");

        // 异步加载数据
        loadBookingData(model);

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
                        selectedBookNumber = (String) table.getValueAt(selectedRow, 0); // 保存预订编号
                        selectedRNumber = (String) table.getValueAt(selectedRow, 2); // 保存房间编号
                        
                        // 启用取消按钮
                        confirmButton.setEnabled(true);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ModernTheme.CARD_COLOR);
        
        // 添加表格标题和表格到内容面板
        contentPanel.add(tableHeaderPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 创建底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        // 创建刷新按钮
        refreshButton = ModernTheme.createRoundedButton("刷新预订列表");
        refreshButton.setBackground(ModernTheme.SUCCESS_COLOR);
        AnimationManager.addButtonClickAnimation(refreshButton);
        
        // 创建取消预约按钮
        confirmButton = ModernTheme.createRoundedButton("取消选中预约");
        confirmButton.setEnabled(false); // 初始禁用按钮，直到选择了记录
        AnimationManager.addButtonClickAnimation(confirmButton);
        
        // 创建返回按钮
        backButton = ModernTheme.createRoundedButton("返回预订页面");
        backButton.setBackground(ModernTheme.ACCENT_COLOR);
        AnimationManager.addButtonClickAnimation(backButton);
        
        // 创建主菜单按钮
        menuButton = ModernTheme.createRoundedButton("返回主菜单");
        menuButton.setBackground(new Color(155, 89, 182)); // 紫色
        AnimationManager.addButtonClickAnimation(menuButton);
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(menuButton);
        buttonPanel.add(backButton);
        buttonPanel.add(confirmButton);
        
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
                confirmButton.setEnabled(false);
                selectedRNumber = null;
                selectedBookNumber = null;
                
                // 清空当前表格数据
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.setRowCount(0);
                
                // 重新加载数据
                loadBookingData(model);
                
                // 恢复按钮状态
                refreshButton.setEnabled(true);
            }
        });
        
        // 添加按钮事件
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedRNumber != null && selectedBookNumber != null) {
                    // 防止重复点击 - 使用原子操作确保线程安全
                    if (!isProcessing.compareAndSet(false, true)) {
                        return;
                    }
                    
                    // 禁用按钮防止重复点击
                    confirmButton.setEnabled(false);
                    
                    // 显示确认对话框
                    int option = JOptionPane.showConfirmDialog(
                        CancelBook.this,
                        "确定要取消房间 " + selectedRNumber + " 的预约吗？",
                        "确认取消",
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    if (option == JOptionPane.YES_OPTION) {
                        // 创建进度指示器
                        JDialog loadingDialog = createLoadingDialog("正在处理取消请求...");
                        loadingDialog.setVisible(true);
                        
                        // 在新线程中执行数据库操作
                        Thread cancelThread = new Thread(() -> {
                            try {
                                // 执行取消预约操作
                                cancelBooking(selectedBookNumber, selectedRNumber);
                                
                                // 关闭进度对话框并刷新数据
                                SwingUtilities.invokeLater(() -> {
                                    loadingDialog.dispose();
                                    
                                    // 显示成功消息
                                    JOptionPane.showMessageDialog(
                                        CancelBook.this,
                                        "预约已成功取消",
                                        "操作成功",
                                        JOptionPane.INFORMATION_MESSAGE
                                    );
                                    
                                    // 刷新表格数据
                                    refreshPage();
                                    
                                    // 重置状态
                                    selectedRNumber = null;
                                    selectedBookNumber = null;
                                    confirmButton.setEnabled(false);
                                    isProcessing.set(false);
                                });
                            } catch (Exception ex) {
                                LOGGER.log(Level.WARNING, "取消预约时出错", ex);
                                SwingUtilities.invokeLater(() -> {
                                    loadingDialog.dispose();
                                    
                                    JOptionPane.showMessageDialog(
                                        CancelBook.this,
                                        "取消预约失败: " + ex.getMessage(),
                                        "错误",
                                        JOptionPane.ERROR_MESSAGE
                                    );
                                    
                                    confirmButton.setEnabled(true);
                                    isProcessing.set(false);
                                });
                            }
                        });
                        
                        // 设置为守护线程，确保程序关闭时线程也会终止
                        cancelThread.setDaemon(true);
                        cancelThread.start();
                        
                        // 设置超时机制
                        new Thread(() -> {
                            try {
                                Thread.sleep(10000); // 10秒超时
                                if (isProcessing.get()) {
                                    SwingUtilities.invokeLater(() -> {
                                        loadingDialog.dispose();
                                        JOptionPane.showMessageDialog(
                                            CancelBook.this, 
                                            "取消操作超时，请稍后重试", 
                                            "超时", 
                                            JOptionPane.WARNING_MESSAGE
                                        );
                                        confirmButton.setEnabled(true);
                                        isProcessing.set(false);
                                    });
                                }
                            } catch (InterruptedException ex) {
                                // 忽略中断异常
                            }
                        }).start();
                    } else {
                        // 用户取消了操作，恢复按钮状态
                        confirmButton.setEnabled(true);
                        isProcessing.set(false);
                    }
                } else {
                    JOptionPane.showMessageDialog(CancelBook.this, "请选择要取消的预约");
                }
            }
        });
        
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭当前窗口
                try {
                    RoomBooking.main(new String[]{}); // 返回预订页面
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "返回预订页面失败", ex);
                    JOptionPane.showMessageDialog(
                        CancelBook.this,
                        "返回预订页面失败: " + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        
        menuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭当前窗口
                try {
                    MainMenu.main(new String[]{}); // 返回主菜单
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "返回主菜单失败", ex);
                    JOptionPane.showMessageDialog(
                        CancelBook.this,
                        "返回主菜单失败: " + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                    );
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
    
    /**
     * 异步加载预订数据
     */
    private void loadBookingData(DefaultTableModel model) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Connection conn = null;
                PreparedStatement stmt = null;
                ResultSet rs = null;
                
                try {
                    conn = DatabaseHelper.getConnection();
                    if (conn == null) {
                        throw new SQLException("无法连接到数据库");
                    }
                    
                    // 确保bookroom表存在
                    try {
                        DatabaseMetaData meta = conn.getMetaData();
                        rs = meta.getTables(null, null, "bookroom", new String[] {"TABLE"});
                        boolean tableExists = rs.next();
                        rs.close();
                        
                        if (!tableExists) {
                            LOGGER.info("bookroom表不存在，无预订记录");
                            return null;
                        }
                        
                        // 查询当前用户未入住的预订
                        String query = "SELECT * FROM bookroom WHERE cNumber = ? AND checkin = '未入住'";
                        stmt = conn.prepareStatement(query);
                        stmt.setString(1, enteredUsername);
                        stmt.setQueryTimeout(DB_TIMEOUT_SECONDS);
                        rs = stmt.executeQuery();
                        
                        boolean hasBookings = false;
                        
                        while (rs.next()) {
                            hasBookings = true;
                            final String bookNumber = rs.getString("bookNumber");
                            final String cNumber = rs.getString("cNumber");
                            final String rNumber = rs.getString("rNumber");
                            final String bookdate = rs.getString("bookdate");
                            final String checkin = rs.getString("checkin");
                            
                            // 在EDT线程中更新UI
                            SwingUtilities.invokeLater(() -> {
                                model.addRow(new Object[]{
                                    bookNumber,
                                    cNumber,
                                    rNumber,
                                    bookdate,
                                    checkin
                                });
                            });
                        }
                        
                        // 如果没有预订记录，显示提示
                        if (!hasBookings) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                    CancelBook.this,
                                    "您当前没有未入住的预订记录",
                                    "提示",
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                            });
                        }
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "执行查询时出错", e);
                        throw e;
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "加载预订数据失败", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                            CancelBook.this,
                            "加载预订数据失败: " + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                } finally {
                    DatabaseHelper.closeResources(conn, stmt, rs);
                }
                return null;
            }
            
            @Override
            protected void done() {
                // 检查表格是否有数据
                if (table.getRowCount() == 0) {
                    LOGGER.info("预订数据加载完成，但当前没有预订记录");
                } else {
                    LOGGER.info("预订数据加载完成，共加载 " + table.getRowCount() + " 条记录");
                }
            }
        };
        
        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    String username = LoginPage.enteredUsername;
                    new CancelBook(username);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "启动预订管理界面失败", e);
                    JOptionPane.showMessageDialog(
                        null,
                        "启动预订管理界面失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }

    /**
     * 刷新页面数据
     */
    public void refreshPage() {
        // 清空表格数据
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        
        // 重新加载数据
        loadBookingData(model);
        
        // 重置选择
        selectedRNumber = null;
        selectedBookNumber = null;
        confirmButton.setEnabled(false);
    }
    
    /**
     * 取消预订的操作
     */
    private void cancelBooking(String bookNumber, String rNumber) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                throw new SQLException("无法连接到数据库");
            }
            
            // 启动事务
            conn.setAutoCommit(false);
            
            try {
                // 0. 首先验证预订记录是否存在并且是当前用户的
                String verifySQL = "SELECT cNumber FROM bookroom WHERE bookNumber = ? FOR UPDATE";
                stmt = conn.prepareStatement(verifySQL);
                stmt.setString(1, bookNumber);
                stmt.setQueryTimeout(DB_TIMEOUT_SECONDS);
                rs = stmt.executeQuery();
                
                if (!rs.next()) {
                    throw new SQLException("预订记录不存在");
                }
                
                String cNumber = rs.getString("cNumber");
                if (!enteredUsername.equals(cNumber)) {
                    throw new SQLException("无权取消他人的预订");
                }
                
                rs.close();
                stmt.close();
                
                // 1. 从bookroom表中删除预订记录
                String deleteBookSQL = "DELETE FROM bookroom WHERE bookNumber = ?";
                stmt = conn.prepareStatement(deleteBookSQL);
                stmt.setString(1, bookNumber);
                stmt.setQueryTimeout(DB_TIMEOUT_SECONDS);
                int bookRowsAffected = stmt.executeUpdate();
                
                if (bookRowsAffected == 0) {
                    throw new SQLException("未找到预订记录");
                }
                
                // 关闭当前PreparedStatement
                stmt.close();
                
                // 2. 检查roomstate表是否存在
                DatabaseMetaData meta = conn.getMetaData();
                rs = meta.getTables(null, null, "roomstate", new String[] {"TABLE"});
                boolean tableExists = rs.next();
                rs.close();
                
                if (tableExists) {
                    // 3. 更新房间状态为可预订
                    String updateRoomSQL = "UPDATE roomstate SET state = '可预订' WHERE rNumber = ?";
                    stmt = conn.prepareStatement(updateRoomSQL);
                    stmt.setString(1, rNumber);
                    stmt.setQueryTimeout(DB_TIMEOUT_SECONDS);
                    stmt.executeUpdate();
                } else {
                    LOGGER.warning("roomstate表不存在，无法更新房间状态");
                }
                
                // 提交事务
                conn.commit();
                LOGGER.info("成功取消预订: " + bookNumber + ", 房间: " + rNumber);
            } catch (SQLException e) {
                // 回滚事务
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        LOGGER.log(Level.WARNING, "事务回滚失败", ex);
                    }
                }
                throw e;
            } finally {
                // 恢复自动提交
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                    } catch (SQLException ex) {
                        LOGGER.log(Level.WARNING, "恢复自动提交失败", ex);
                    }
                }
            }
        } finally {
            // 关闭所有资源
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "关闭ResultSet失败", e);
                }
            }
            
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "关闭Statement失败", e);
                }
            }
            
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "关闭连接失败", e);
                }
            }
        }
    }
}