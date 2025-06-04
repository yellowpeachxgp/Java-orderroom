package com.all.search;

import com.database.helper.DatabaseHelper;
import com.hotel.ui.BaseModuleFrame;
import com.hotel.ui.CommonUITemplate;
import com.hotel.ui.ModernTableRenderer;
import com.hotel.ui.ModernTheme;
import com.hotel.ui.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 删除用户模块
 * 允许管理员查看和删除系统中的用户
 */
public class delete_client extends BaseModuleFrame {
    // 添加日志记录器
    private static final Logger LOGGER = Logger.getLogger(delete_client.class.getName());
    
    private JTable table;
    private String selectedcNumber; // 保存选择的 cNumber
    private JButton deleteButton;
    private JPanel tablePanel;
    private JLabel infoLabel;

    /**
     * 构造函数
     */
    public delete_client() {
        // 使用带权限检查的构造函数，要求管理员权限
        super("删除用户", 800, 600, PERMISSION_ADMIN);
        
        try {
            // 如果权限检查通过，继续初始化
            if (executorService != null) {
                // 初始化组件
                initializeComponents();
                
                // 加载数据
                loadData();
                
                // 显示窗口并应用动画
                showWithAnimation();
            }
        } catch (Exception e) {
            handleException("初始化删除用户界面失败", e);
        }
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        try {
            // 设置内容面板布局
            contentPanel.setLayout(new BorderLayout(10, 10));
            
            // 创建信息面板
            JPanel infoPanel = new JPanel() {
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
            infoPanel.setLayout(new BorderLayout());
            infoPanel.setOpaque(false);
            infoPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            infoLabel = new JLabel("请从下表中选择要删除的用户，然后点击删除用户按钮");
            infoLabel.setFont(ModernTheme.REGULAR_FONT);
            infoLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            infoPanel.add(infoLabel, BorderLayout.CENTER);
            
            // 创建表格面板
            tablePanel = new JPanel(new BorderLayout());
            tablePanel.setOpaque(false);
            tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            // 创建表格标题
            JLabel tableTitle = new JLabel("用户列表");
            tableTitle.setFont(ModernTheme.HEADER_FONT);
            tableTitle.setForeground(ModernTheme.PRIMARY_COLOR);
            tableTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
            
            // 创建表格模型
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // 设置表格不可编辑
                }
            };
            model.addColumn("用户编号");
            model.addColumn("用户姓名");
            model.addColumn("用户年龄");
            model.addColumn("用户性别");
            model.addColumn("用户电话");
            
            // 创建表格
            table = new JTable(model);
            ModernTableRenderer.applyModernStyle(table);
            
            // 添加表格选择事件
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        int selectedRow = table.getSelectedRow();
                        if (selectedRow != -1) {
                            selectedcNumber = (String) table.getValueAt(selectedRow, 0);
                            String userName = (String) table.getValueAt(selectedRow, 1);
                            updateStatus("已选择用户: " + userName + " (" + selectedcNumber + ")");
                            deleteButton.setEnabled(true);
                        } else {
                            selectedcNumber = null;
                            updateStatus("未选择用户");
                            deleteButton.setEnabled(false);
                        }
                    }
                }
            });
            
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(ModernTheme.BACKGROUND_COLOR);
            
            tablePanel.add(tableTitle, BorderLayout.NORTH);
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            
            // 创建按钮面板
            JPanel buttonPanel = CommonUITemplate.createButtonPanel();
            
            // 创建刷新按钮
            JButton refreshButton = CommonUITemplate.createActionButton("刷新列表", new Color(75, 179, 75), e -> loadData());
            
            // 创建删除按钮
            deleteButton = CommonUITemplate.createActionButton("删除用户", ModernTheme.ERROR_COLOR, e -> deleteUser());
            deleteButton.setEnabled(false); // 初始状态下禁用删除按钮
            
            buttonPanel.add(refreshButton);
            buttonPanel.add(deleteButton);
            
            // 将所有面板添加到内容面板
            contentPanel.add(infoPanel, BorderLayout.NORTH);
            contentPanel.add(tablePanel, BorderLayout.CENTER);
            
            // 替换按钮面板
            if (mainPanel.getComponentCount() > 2) {
                mainPanel.remove(2); // 移除原按钮面板
            }
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
        } catch (Exception e) {
            handleException("初始化组件失败", e);
        }
    }
    
    /**
     * 加载表格数据
     */
    private void loadData() {
        updateStatus("正在加载用户数据...");
        
        // 创建加载对话框
        final JDialog loadingDialog = UIHelper.createLoadingDialog(this, "正在加载用户数据...");
        
        // 在EDT中显示对话框
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
        
        // 在线程池中执行数据库操作
        executorService.submit(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            
            try {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.setRowCount(0); // 清空表格内容
                
                conn = DatabaseHelper.getConnection();
                if (conn != null) {
                    String query = "SELECT * FROM client ORDER BY cNumber";
                    stmt = conn.prepareStatement(query);
                    rs = stmt.executeQuery();
                    
                    int rowCount = 0;
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getString("cNumber"),
                            rs.getString("cName"),
                            rs.getString("cAge"),
                            rs.getString("cSex"),
                            rs.getString("cTel")
                        });
                        rowCount++;
                    }
                    
                    // 在EDT中更新UI
                    final int finalRowCount = rowCount;
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose();
                        if (finalRowCount > 0) {
                            updateStatus("数据加载完成，共 " + finalRowCount + " 条记录");
                            infoLabel.setText("请从下表中选择要删除的用户，然后点击删除用户按钮");
                        } else {
                            updateStatus("没有找到用户记录");
                            infoLabel.setText("系统中没有用户记录");
                        }
                        deleteButton.setEnabled(false);
                        selectedcNumber = null;
                    });
                    
                } else {
                    // 在EDT中更新UI
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose();
                        updateStatus("无法连接到数据库");
                        infoLabel.setText("无法连接到数据库，请稍后重试");
                    });
                }
                
            } catch (Exception e) {
                handleException("加载用户数据失败", e);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    updateStatus("加载失败: " + e.getMessage());
                    infoLabel.setText("加载用户数据失败: " + e.getMessage());
                });
                
            } finally {
                // 确保关闭资源
                UIHelper.safeCloseResources(conn, stmt, rs);
            }
        });
    }
    
    /**
     * 删除用户
     */
    private void deleteUser() {
        if (selectedcNumber == null) {
            JOptionPane.showMessageDialog(this, "请选择要删除的用户", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 获取选中行的用户名，用于显示确认消息
        int selectedRow = table.getSelectedRow();
        String userName = (String) table.getValueAt(selectedRow, 1);
        
        // 确认删除
        int option = JOptionPane.showConfirmDialog(
            this,
            "确定要删除用户 " + userName + " (" + selectedcNumber + ") 吗？\n这将同时删除该用户的所有预订记录。",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        
        // 禁用删除按钮防止重复点击
        deleteButton.setEnabled(false);
        
        // 创建加载对话框
        final JDialog loadingDialog = UIHelper.createLoadingDialog(this, "正在删除用户...");
        
        // 在EDT中显示对话框
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
        
        // 使用线程池执行删除操作
        final String userNumberToDelete = selectedcNumber; // 捕获当前选中的用户编号
        updateStatus("正在删除用户: " + userNumberToDelete);
        
        executorService.submit(() -> {
            Connection connection = null;
            PreparedStatement stmt1 = null;
            PreparedStatement stmt2 = null;
            
            try {
                LOGGER.info("开始删除用户: " + userNumberToDelete);
                
                connection = DatabaseHelper.getConnection();
                if (connection == null) {
                    throw new SQLException("无法获取数据库连接");
                }
                
                // 开始事务
                connection.setAutoCommit(false);
                
                // 先删除预订记录
                String query2 = "DELETE FROM bookroom WHERE cNumber = ?";
                stmt2 = connection.prepareStatement(query2);
                stmt2.setString(1, userNumberToDelete);
                int bookroomResult = stmt2.executeUpdate();
                LOGGER.info("删除预订记录: " + bookroomResult + " 条");
                
                // 然后删除用户
                String query1 = "DELETE FROM client WHERE cNumber = ?";
                stmt1 = connection.prepareStatement(query1);
                stmt1.setString(1, userNumberToDelete);
                int clientResult = stmt1.executeUpdate();
                LOGGER.info("删除用户记录: " + clientResult + " 条");
                
                // 提交事务
                connection.commit();
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    
                    if (clientResult > 0) {
                        JOptionPane.showMessageDialog(
                            this, 
                            "用户 " + userName + " (" + userNumberToDelete + ") 删除成功！\n" +
                            "同时删除了 " + bookroomResult + " 条预订记录。",
                            "删除成功", 
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        updateStatus("用户删除成功");
                        
                        // 刷新表格数据
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "用户不存在或已被删除",
                            "删除失败",
                            JOptionPane.ERROR_MESSAGE
                        );
                        updateStatus("用户不存在或已被删除");
                        deleteButton.setEnabled(true);
                    }
                });
                
            } catch (Exception e) {
                // 回滚事务
                try {
                    if (connection != null) {
                        connection.rollback();
                    }
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "回滚事务失败", ex);
                }
                
                handleException("删除用户失败", e);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    updateStatus("删除失败: " + e.getMessage());
                    deleteButton.setEnabled(true);
                });
                
            } finally {
                // 恢复自动提交
                try {
                    if (connection != null) {
                        connection.setAutoCommit(true);
                    }
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "恢复自动提交失败", ex);
                }
                
                // 关闭资源
                try {
                    if (stmt1 != null) stmt1.close();
                    if (stmt2 != null) stmt2.close();
                    if (connection != null) connection.close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "关闭数据库资源失败", ex);
                }
            }
        });
    }

    /**
     * 主方法
     */
    public static void main(String[] args) {
        // 确保在EDT中创建UI
        SwingUtilities.invokeLater(() -> {
            try {
                new delete_client();
            } catch (Exception e) {
                Logger logger = Logger.getLogger(delete_client.class.getName());
                logger.log(Level.SEVERE, "启动应用程序时发生错误", e);
                JOptionPane.showMessageDialog(
                    null,
                    "启动应用程序时发生错误: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
