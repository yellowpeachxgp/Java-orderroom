package com.hotel.book;

import com.database.helper.DatabaseHelper;
import com.hotel.ui.ModernTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 简化版房间预订系统
 * 修复原版本的语法错误和逻辑问题
 */
public class SimpleRoomBooking extends JFrame {
    
    private static final Logger LOGGER = Logger.getLogger(SimpleRoomBooking.class.getName());
    private static final int DB_TIMEOUT_SECONDS = 10;
    
    // UI组件
    private JTable roomTable;
    private JButton bookButton;
    private JButton refreshButton;
    private JButton backButton;
    private JLabel statusLabel;
    
    // 数据字段
    private String selectedRoomNumber;
    private String currentUser;
    
    public SimpleRoomBooking(String username) {
        this.currentUser = username;
        
        // 应用现代主题
        ModernTheme.applyTheme();
        
        // 初始化窗口
        initializeWindow();
        
        // 创建UI组件
        createComponents();
        
        // 布局组件
        layoutComponents();
        
        // 添加事件监听器
        addEventListeners();
        
        // 加载房间数据
        refreshRoomData();
        
        // 显示窗口
        setVisible(true);
    }
    
    private void initializeWindow() {
        setTitle("酒店房间预订系统 - 客户: " + currentUser);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void createComponents() {
        // 创建表格
        String[] columnNames = {"房间号", "房间类型", "价格(元)", "状态"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 不允许编辑
            }
        };
        
        roomTable = new JTable(tableModel);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.setRowHeight(35);
        roomTable.getTableHeader().setFont(ModernTheme.REGULAR_FONT);
        roomTable.setFont(ModernTheme.REGULAR_FONT);
        
        // 创建按钮
        refreshButton = new JButton("刷新房间列表");
        refreshButton.setFont(ModernTheme.REGULAR_FONT);
        refreshButton.setBackground(ModernTheme.SUCCESS_COLOR);
        refreshButton.setForeground(Color.WHITE);
        
        bookButton = new JButton("预订选中房间");
        bookButton.setFont(ModernTheme.REGULAR_FONT);
        bookButton.setBackground(ModernTheme.PRIMARY_COLOR);
        bookButton.setForeground(Color.WHITE);
        bookButton.setEnabled(false);
        
        backButton = new JButton("返回主菜单");
        backButton.setFont(ModernTheme.REGULAR_FONT);
        backButton.setBackground(ModernTheme.ACCENT_COLOR);
        backButton.setForeground(Color.WHITE);
        
        // 创建状态标签
        statusLabel = new JLabel("请选择要预订的房间");
        statusLabel.setFont(ModernTheme.REGULAR_FONT);
        statusLabel.setForeground(ModernTheme.TEXT_SECONDARY);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // 顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        topPanel.setBackground(ModernTheme.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("房间预订系统", SwingConstants.CENTER);
        titleLabel.setFont(ModernTheme.TITLE_FONT);
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        
        JLabel userLabel = new JLabel("当前用户: " + currentUser, SwingConstants.RIGHT);
        userLabel.setFont(ModernTheme.REGULAR_FONT);
        userLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(userLabel, BorderLayout.EAST);
        
        // 中间面板 - 表格
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        centerPanel.setBackground(ModernTheme.BACKGROUND_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("可预订房间列表"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 底部面板
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        bottomPanel.setBackground(ModernTheme.BACKGROUND_COLOR);
        
        // 状态面板
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(ModernTheme.BACKGROUND_COLOR);
        statusPanel.add(statusLabel);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ModernTheme.BACKGROUND_COLOR);
        buttonPanel.add(refreshButton);
        buttonPanel.add(bookButton);
        buttonPanel.add(backButton);
        
        bottomPanel.add(statusPanel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        // 添加到主窗口
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // 设置背景色
        getContentPane().setBackground(ModernTheme.BACKGROUND_COLOR);
    }
    
    private void addEventListeners() {
        // 表格选择监听器
        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = roomTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedRoomNumber = (String) roomTable.getValueAt(selectedRow, 0);
                    String roomStatus = (String) roomTable.getValueAt(selectedRow, 3);
                    
                    if ("可预订".equals(roomStatus)) {
                        bookButton.setEnabled(true);
                        statusLabel.setText("已选择房间: " + selectedRoomNumber + " (可预订)");
                        statusLabel.setForeground(ModernTheme.SUCCESS_COLOR);
                    } else {
                        bookButton.setEnabled(false);
                        statusLabel.setText("房间 " + selectedRoomNumber + " 不可预订");
                        statusLabel.setForeground(ModernTheme.ERROR_COLOR);
                    }
                } else {
                    selectedRoomNumber = null;
                    bookButton.setEnabled(false);
                    statusLabel.setText("请选择要预订的房间");
                    statusLabel.setForeground(ModernTheme.TEXT_SECONDARY);
                }
            }
        });
        
        // 刷新按钮
        refreshButton.addActionListener(e -> {
            refreshButton.setEnabled(false);
            statusLabel.setText("正在刷新房间列表...");
            statusLabel.setForeground(ModernTheme.TEXT_SECONDARY);
            
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    refreshRoomData();
                    return null;
                }
                
                @Override
                protected void done() {
                    refreshButton.setEnabled(true);
                    statusLabel.setText("房间列表已刷新");
                    statusLabel.setForeground(ModernTheme.SUCCESS_COLOR);
                }
            };
            worker.execute();
        });
        
        // 预订按钮
        bookButton.addActionListener(e -> {
            if (selectedRoomNumber != null) {
                performBooking();
            }
        });
        
        // 返回按钮
        backButton.addActionListener(e -> {
            dispose();
        });
    }
    
    private void refreshRoomData() {
        SwingUtilities.invokeLater(() -> {
            DefaultTableModel model = (DefaultTableModel) roomTable.getModel();
            model.setRowCount(0);
            
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = DatabaseHelper.getConnection();
                if (conn == null) {
                    throw new SQLException("无法连接到数据库");
                }
                
                // 查询房间信息和状态
                String query = "SELECT r.rNumber, r.rType, r.rPrice, " +
                              "COALESCE(rs.state, '可预订') as state " +
                              "FROM rooms r " +
                              "LEFT JOIN roomstate rs ON r.rNumber = rs.rNumber " +
                              "ORDER BY r.rNumber";
                
                stmt = conn.prepareStatement(query);
                stmt.setQueryTimeout(DB_TIMEOUT_SECONDS);
                rs = stmt.executeQuery();
                
                while (rs.next()) {
                    String rNumber = rs.getString("rNumber");
                    String rType = rs.getString("rType");
                    String rPrice = rs.getString("rPrice");
                    String state = rs.getString("state");
                    
                    // 检查是否真的可预订（没有未入住的预订记录）
                    if ("可预订".equals(state) || state == null) {
                        state = isRoomReallyAvailable(rNumber) ? "可预订" : "已预订";
                    }
                    
                    model.addRow(new Object[]{rNumber, rType, rPrice + "元", state});
                }
                
                if (model.getRowCount() == 0) {
                    statusLabel.setText("没有找到房间数据");
                    statusLabel.setForeground(ModernTheme.ERROR_COLOR);
                }
                
            } catch (Exception e) {
                LOGGER.severe("加载房间数据失败: " + e.getMessage());
                statusLabel.setText("加载房间数据失败: " + e.getMessage());
                statusLabel.setForeground(ModernTheme.ERROR_COLOR);
                JOptionPane.showMessageDialog(this, 
                    "加载房间数据失败: " + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            } finally {
                DatabaseHelper.closeResources(conn, stmt, rs);
            }
        });
    }
    
    private boolean isRoomReallyAvailable(String roomNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) return false;
            
            // 检查是否有未入住的预订记录
            String query = "SELECT COUNT(*) FROM bookroom WHERE rNumber = ? AND checkin = '未入住'";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, roomNumber);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) == 0; // 没有未入住记录说明可预订
            }
            
        } catch (Exception e) {
            LOGGER.warning("检查房间可用性失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return false;
    }
    
    private void performBooking() {
        // 禁用按钮防止重复点击
        bookButton.setEnabled(false);
        statusLabel.setText("正在处理预订请求...");
        statusLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        
        SwingWorker<Boolean, Void> bookingWorker = new SwingWorker<Boolean, Void>() {
            private String errorMessage = "";
            
            @Override
            protected Boolean doInBackground() throws Exception {
                return executeBooking();
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    if (success) {
                        statusLabel.setText("房间 " + selectedRoomNumber + " 预订成功！");
                        statusLabel.setForeground(ModernTheme.SUCCESS_COLOR);
                        
                        JOptionPane.showMessageDialog(SimpleRoomBooking.this,
                            "房间 " + selectedRoomNumber + " 预订成功！\n请记住您的预订信息。",
                            "预订成功",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // 刷新房间列表
                        refreshRoomData();
                        
                        // 清除选择
                        roomTable.clearSelection();
                        selectedRoomNumber = null;
                        
                    } else {
                        statusLabel.setText("预订失败: " + errorMessage);
                        statusLabel.setForeground(ModernTheme.ERROR_COLOR);
                        
                        JOptionPane.showMessageDialog(SimpleRoomBooking.this,
                            "预订失败: " + errorMessage,
                            "预订失败",
                            JOptionPane.ERROR_MESSAGE);
                        
                        bookButton.setEnabled(true);
                    }
                    
                } catch (Exception e) {
                    statusLabel.setText("预订失败: " + e.getMessage());
                    statusLabel.setForeground(ModernTheme.ERROR_COLOR);
                    bookButton.setEnabled(true);
                }
            }
            
            private boolean executeBooking() {
                Connection conn = null;
                PreparedStatement stmt = null;
                ResultSet rs = null;
                
                try {
                    conn = DatabaseHelper.getConnection();
                    if (conn == null) {
                        errorMessage = "无法连接到数据库";
                        return false;
                    }
                    
                    conn.setAutoCommit(false);
                    
                    try {
                        // 1. 再次检查房间可用性（防并发）
                        String checkQuery = "SELECT COUNT(*) FROM bookroom WHERE rNumber = ? AND checkin = '未入住'";
                        stmt = conn.prepareStatement(checkQuery);
                        stmt.setString(1, selectedRoomNumber);
                        rs = stmt.executeQuery();
                        
                        if (rs.next() && rs.getInt(1) > 0) {
                            errorMessage = "该房间已被其他用户预订";
                            conn.rollback();
                            return false;
                        }
                        
                        rs.close();
                        stmt.close();
                        
                        // 2. 更新或插入房间状态
                        String updateStateQuery = "INSERT INTO roomstate (rNumber, state) VALUES (?, '已预订') " +
                                                "ON DUPLICATE KEY UPDATE state = '已预订'";
                        stmt = conn.prepareStatement(updateStateQuery);
                        stmt.setString(1, selectedRoomNumber);
                        stmt.executeUpdate();
                        stmt.close();
                        
                        // 3. 生成预订号
                        String bookNumber = generateBookNumber();
                        
                        // 4. 插入预订记录
                        String insertBookQuery = "INSERT INTO bookroom (bookNumber, cNumber, rNumber, bookdate, checkin) " +
                                               "VALUES (?, ?, ?, ?, '未入住')";
                        stmt = conn.prepareStatement(insertBookQuery);
                        stmt.setString(1, bookNumber);
                        stmt.setString(2, currentUser);
                        stmt.setString(3, selectedRoomNumber);
                        
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        stmt.setString(4, sdf.format(new Date()));
                        
                        int result = stmt.executeUpdate();
                        
                        if (result > 0) {
                            conn.commit();
                            LOGGER.info("预订成功: 用户=" + currentUser + ", 房间=" + selectedRoomNumber + ", 预订号=" + bookNumber);
                            return true;
                        } else {
                            errorMessage = "预订记录插入失败";
                            conn.rollback();
                            return false;
                        }
                        
                    } catch (SQLException e) {
                        conn.rollback();
                        errorMessage = "数据库操作失败: " + e.getMessage();
                        LOGGER.severe("预订失败: " + e.getMessage());
                        return false;
                    }
                    
                } catch (Exception e) {
                    errorMessage = "预订过程中发生错误: " + e.getMessage();
                    LOGGER.severe("预订失败: " + e.getMessage());
                    return false;
                } finally {
                    try {
                        if (conn != null) {
                            conn.setAutoCommit(true);
                        }
                    } catch (SQLException e) {
                        LOGGER.warning("恢复自动提交模式失败: " + e.getMessage());
                    }
                    DatabaseHelper.closeResources(conn, stmt, rs);
                }
            }
        };
        
        bookingWorker.execute();
    }
    
    private String generateBookNumber() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "BOOK" + uuid.substring(0, 6).toUpperCase();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                String username = args.length > 0 ? args[0] : "GUEST";
                new SimpleRoomBooking(username);
            } catch (Exception e) {
                LOGGER.severe("启动预订系统失败: " + e.getMessage());
                JOptionPane.showMessageDialog(null, 
                    "启动预订系统失败: " + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
} 