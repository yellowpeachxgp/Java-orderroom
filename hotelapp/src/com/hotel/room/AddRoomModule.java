package com.hotel.room;

import com.database.helper.DatabaseHelper;
import com.hotel.ui.BaseModuleFrame;
import com.hotel.ui.ModernTableRenderer;
import com.hotel.ui.ModernTheme;
import com.hotel.ui.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 添加房间模块
 * 提供房间信息添加和列表查看功能
 */
public class AddRoomModule extends BaseModuleFrame {
    
    private static final Logger LOGGER = Logger.getLogger(AddRoomModule.class.getName());
    
    // UI组件
    private JTextField roomNumberField;
    private JTextField roomTypeField;
    private JTextField roomPriceField;
    private JTextField managerNumberField;
    private JTextField roomPhoneField;
    private JTable roomTable;
    private JButton addButton;
    private JButton clearButton;
    
    /**
     * 构造函数
     */
    public AddRoomModule() {
        super("房间管理", 900, 600);
        
        try {
            // 初始化组件
            initializeComponents();
            
            // 加载数据
            loadData();
            
            // 显示窗口并应用动画
            showWithAnimation();
            
        } catch (Exception e) {
            handleException("初始化房间管理界面失败", e);
        }
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 设置内容面板布局
        contentPanel.setLayout(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // 创建表单面板
        JPanel formPanel = createFormPanel();
        
        // 创建表格面板
        JPanel tablePanel = createTablePanel();
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        // 添加到内容面板
        contentPanel.add(formPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建表单面板
     */
    private JPanel createFormPanel() {
        // 创建卡片式面板
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ModernTheme.CARD_COLOR);
                g2d.fill(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // 创建表单标题
        JLabel titleLabel = new JLabel("添加新房间");
        titleLabel.setFont(ModernTheme.HEADER_FONT);
        titleLabel.setForeground(ModernTheme.PRIMARY_COLOR);
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 5;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 15, 0);
        panel.add(titleLabel, c);
        
        // 创建输入字段
        roomNumberField = ModernTheme.createModernTextField(15);
        roomTypeField = ModernTheme.createModernTextField(15);
        roomPriceField = ModernTheme.createModernTextField(15);
        managerNumberField = ModernTheme.createModernTextField(15);
        roomPhoneField = ModernTheme.createModernTextField(15);
        
        // 添加输入字段和标签
        addFormField(panel, "房间编号:", roomNumberField, 0, 1);
        addFormField(panel, "房间类型:", roomTypeField, 1, 1);
        addFormField(panel, "房间价格:", roomPriceField, 2, 1);
        addFormField(panel, "负责人编号:", managerNumberField, 3, 1);
        addFormField(panel, "房间电话:", roomPhoneField, 4, 1);
        
        return panel;
    }
    
    /**
     * 添加表单字段
     */
    private void addFormField(JPanel panel, String labelText, JTextField field, int gridx, int gridy) {
        GridBagConstraints c = new GridBagConstraints();
        
        // 添加标签
        JLabel label = ModernTheme.createLabel(labelText);
        c.gridx = gridx;
        c.gridy = gridy;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 5, 5, 10);
        panel.add(label, c);
        
        // 添加字段
        c.gridx = gridx;
        c.gridy = gridy + 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 5, 15, 10);
        panel.add(field, c);
    }
    
    /**
     * 创建表格面板
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        
        // 创建表格标题
        JLabel titleLabel = new JLabel("房间列表");
        titleLabel.setFont(ModernTheme.HEADER_FONT);
        titleLabel.setForeground(ModernTheme.PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        // 创建表格模型
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        model.addColumn("房间编号");
        model.addColumn("房间类型");
        model.addColumn("房间价格(元)");
        model.addColumn("负责人编号");
        model.addColumn("房间电话");
        
        // 创建表格
        roomTable = new JTable(model);
        ModernTableRenderer.applyModernStyle(roomTable);
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ModernTheme.CARD_COLOR);
        
        // 添加到面板
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // 创建清空按钮
        clearButton = new JButton("清空表单");
        clearButton.setFont(ModernTheme.REGULAR_FONT);
        clearButton.setForeground(Color.WHITE);
        clearButton.setBackground(ModernTheme.ACCENT_COLOR);
        clearButton.setBorderPainted(false);
        clearButton.setFocusPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.setPreferredSize(new Dimension(120, 35));
        
        // 添加按钮悬停效果
        clearButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                clearButton.setBackground(ModernTheme.ACCENT_COLOR.darker());
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                clearButton.setBackground(ModernTheme.ACCENT_COLOR);
            }
        });
        
        // 添加清空按钮事件
        clearButton.addActionListener(e -> clearForm());
        
        // 创建添加按钮
        addButton = ModernTheme.createRoundedButton("添加房间");
        addButton.setPreferredSize(new Dimension(120, 35));
        
        // 添加添加按钮事件
        addButton.addActionListener(e -> addRoom());
        
        panel.add(clearButton);
        panel.add(addButton);
        
        return panel;
    }
    
    /**
     * 清空表单
     */
    private void clearForm() {
        roomNumberField.setText("");
        roomTypeField.setText("");
        roomPriceField.setText("");
        managerNumberField.setText("");
        roomPhoneField.setText("");
        roomNumberField.requestFocus();
    }
    
    /**
     * 添加房间
     */
    private void addRoom() {
        // 获取表单数据
        String roomNumber = roomNumberField.getText().trim();
        String roomType = roomTypeField.getText().trim();
        String roomPrice = roomPriceField.getText().trim();
        String managerNumber = managerNumberField.getText().trim();
        String roomPhone = roomPhoneField.getText().trim();
        
        // 验证输入
        if (roomNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入房间编号", "提示", JOptionPane.WARNING_MESSAGE);
            roomNumberField.requestFocus();
            return;
        }
        
        if (roomType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入房间类型", "提示", JOptionPane.WARNING_MESSAGE);
            roomTypeField.requestFocus();
            return;
        }
        
        if (roomPrice.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入房间价格", "提示", JOptionPane.WARNING_MESSAGE);
            roomPriceField.requestFocus();
            return;
        }
        
        // 验证价格是否为数字
        try {
            Double.parseDouble(roomPrice);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "房间价格必须是数字", "提示", JOptionPane.WARNING_MESSAGE);
            roomPriceField.requestFocus();
            return;
        }
        
        // 禁用按钮防止重复提交
        addButton.setEnabled(false);
        clearButton.setEnabled(false);
        
        // 更新状态
        updateStatus("正在添加房间...");
        
        // 创建加载对话框
        final JDialog loadingDialog = UIHelper.createLoadingDialog(this, "正在添加房间...");
        
        // 在EDT中显示对话框
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
        
        // 在线程池中执行添加操作
        executorService.submit(() -> {
            try {
                // 执行添加操作
                boolean success = saveRoomToDatabase(roomNumber, roomType, roomPrice, managerNumber, roomPhone);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    // 关闭加载对话框
                    loadingDialog.dispose();
                    
                    // 恢复按钮状态
                    addButton.setEnabled(true);
                    clearButton.setEnabled(true);
                    
                    if (success) {
                        // 显示成功消息
                        JOptionPane.showMessageDialog(this, "房间添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                        
                        // 清空表单
                        clearForm();
                        
                        // 刷新数据
                        loadData();
                        
                        // 更新状态
                        updateStatus("房间添加成功");
                    } else {
                        // 显示错误消息
                        JOptionPane.showMessageDialog(this, "房间添加失败，可能是房间编号已存在", "错误", JOptionPane.ERROR_MESSAGE);
                        
                        // 更新状态
                        updateStatus("房间添加失败");
                    }
                });
                
            } catch (Exception e) {
                // 处理异常
                handleException("添加房间失败", e);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    // 关闭加载对话框
                    loadingDialog.dispose();
                    
                    // 恢复按钮状态
                    addButton.setEnabled(true);
                    clearButton.setEnabled(true);
                    
                    // 更新状态
                    updateStatus("添加失败: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * 保存房间信息到数据库
     */
    private boolean saveRoomToDatabase(String roomNumber, String roomType, String roomPrice, 
                                     String managerNumber, String roomPhone) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return false;
            }
            
            String query = "INSERT INTO rooms (rNumber, rType, rPrice, lNumber, lTel) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, roomNumber);
            stmt.setString(2, roomType);
            stmt.setString(3, roomPrice);
            stmt.setString(4, managerNumber);
            stmt.setString(5, roomPhone);
            
            int result = stmt.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "保存房间信息失败", e);
            return false;
            
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "关闭数据库资源失败", e);
            }
        }
    }
    
    /**
     * 加载房间数据
     */
    private void loadData() {
        updateStatus("正在加载房间数据...");
        
        // 创建加载对话框
        final JDialog loadingDialog = UIHelper.createLoadingDialog(this, "正在加载房间数据...");
        
        // 在EDT中显示对话框
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
        
        // 在线程池中执行数据库操作
        executorService.submit(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            
            try {
                DefaultTableModel model = (DefaultTableModel) roomTable.getModel();
                model.setRowCount(0); // 清空表格内容
                
                conn = DatabaseHelper.getConnection();
                if (conn != null) {
                    String query = "SELECT * FROM rooms ORDER BY rNumber";
                    stmt = conn.prepareStatement(query);
                    rs = stmt.executeQuery();
                    
                    int rowCount = 0;
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getString("rNumber"),
                            rs.getString("rType"),
                            rs.getString("rPrice"),
                            rs.getString("lNumber"),
                            rs.getString("lTel")
                        });
                        rowCount++;
                    }
                    
                    // 在EDT中更新UI
                    final int finalRowCount = rowCount;
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose();
                        if (finalRowCount > 0) {
                            updateStatus("数据加载完成，共 " + finalRowCount + " 条记录");
                        } else {
                            updateStatus("没有找到房间记录");
                        }
                    });
                    
                } else {
                    // 在EDT中更新UI
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose();
                        updateStatus("无法连接到数据库");
                    });
                }
                
            } catch (Exception e) {
                handleException("加载房间数据失败", e);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    updateStatus("加载失败: " + e.getMessage());
                });
                
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "关闭数据库资源失败", e);
                }
            }
        });
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new AddRoomModule();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                    null,
                    "启动房间管理模块失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
} 