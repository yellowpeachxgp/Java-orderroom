package com.hotel.user;

import com.database.helper.DatabaseHelper;
import com.hotel.auth.AuthService;
import com.hotel.auth.UserAuthManager;
import com.hotel.ui.BaseModuleFrame;
import com.hotel.ui.ModernTableRenderer;
import com.hotel.ui.ModernTheme;
import com.hotel.ui.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 用户权限管理界面
 * 允许管理员管理用户权限组和账户类型
 */
public class UserPermissionManager extends BaseModuleFrame {
    
    private static final Logger LOGGER = Logger.getLogger(UserPermissionManager.class.getName());
    
    private JTable userTable;
    private JTable groupTable;
    private JComboBox<AuthService.PermissionGroup> groupComboBox;
    private JButton assignButton;
    private JButton removeButton;
    private JButton setAdminButton;
    private JButton resetPasswordButton;
    private String selectedUserId;
    private int selectedGroupId;
    
    /**
     * 构造函数
     */
    public UserPermissionManager() {
        // 要求管理员权限
        super("用户权限管理", 900, 650, PERMISSION_ADMIN);
        
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
            handleException("初始化用户权限管理界面失败", e);
        }
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        try {
            // 设置内容面板布局
            contentPanel.setLayout(new BorderLayout(10, 10));
            contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            // 创建分割面板
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setResizeWeight(0.6); // 左侧占比
            splitPane.setDividerLocation(500);
            splitPane.setBorder(null);
            
            // 创建左侧用户面板
            JPanel userPanel = createUserPanel();
            
            // 创建右侧权限组面板
            JPanel groupPanel = createGroupPanel();
            
            // 添加到分割面板
            splitPane.setLeftComponent(userPanel);
            splitPane.setRightComponent(groupPanel);
            
            // 添加分割面板到内容面板
            contentPanel.add(splitPane, BorderLayout.CENTER);
            
            // 创建底部按钮面板
            JPanel buttonPanel = createButtonPanel();
            contentPanel.add(buttonPanel, BorderLayout.SOUTH);
            
        } catch (Exception e) {
            handleException("初始化组件失败", e);
        }
    }
    
    /**
     * 创建用户面板
     */
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        
        // 创建标题
        JLabel titleLabel = new JLabel("用户列表");
        titleLabel.setFont(ModernTheme.HEADER_FONT);
        titleLabel.setForeground(ModernTheme.PRIMARY_COLOR);
        
        // 创建搜索面板
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        
        JTextField searchField = new JTextField(20);
        searchField.setFont(ModernTheme.REGULAR_FONT);
        
        JButton searchButton = new JButton("搜索");
        searchButton.setFont(ModernTheme.SMALL_FONT);
        searchButton.addActionListener(e -> searchUsers(searchField.getText()));
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        // 创建顶部面板
        JPanel topPanel = new JPanel(new BorderLayout(0, 5));
        topPanel.setOpaque(false);
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        // 创建表格模型
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("用户ID");
        model.addColumn("用户名");
        model.addColumn("账户类型");
        model.addColumn("最后登录");
        
        // 创建表格
        userTable = new JTable(model);
        ModernTableRenderer.applyModernStyle(userTable);
        
        // 添加表格选择事件
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow != -1) {
                    selectedUserId = (String) userTable.getValueAt(selectedRow, 0);
                    loadUserGroups(selectedUserId);
                    updateButtonStatus();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ModernTheme.BACKGROUND_COLOR);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建权限组面板
     */
    private JPanel createGroupPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        
        // 创建标题
        JLabel titleLabel = new JLabel("用户权限组");
        titleLabel.setFont(ModernTheme.HEADER_FONT);
        titleLabel.setForeground(ModernTheme.PRIMARY_COLOR);
        
        // 创建表格模型
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("组ID");
        model.addColumn("组名称");
        model.addColumn("描述");
        
        // 创建表格
        groupTable = new JTable(model);
        ModernTableRenderer.applyModernStyle(groupTable);
        
        // 添加表格选择事件
        groupTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = groupTable.getSelectedRow();
                if (selectedRow != -1) {
                    selectedGroupId = (int) groupTable.getValueAt(selectedRow, 0);
                    updateButtonStatus();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(groupTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ModernTheme.BACKGROUND_COLOR);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建底部按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // 创建权限组下拉框
        groupComboBox = new JComboBox<>();
        groupComboBox.setFont(ModernTheme.REGULAR_FONT);
        groupComboBox.setPreferredSize(new Dimension(150, 35));
        
        // 加载所有权限组
        loadAllGroups();
        
        // 创建分配按钮
        assignButton = new JButton("分配权限组");
        assignButton.setFont(ModernTheme.REGULAR_FONT);
        assignButton.setEnabled(false);
        assignButton.addActionListener(e -> assignUserToGroup());
        
        // 创建移除按钮
        removeButton = new JButton("移除权限组");
        removeButton.setFont(ModernTheme.REGULAR_FONT);
        removeButton.setEnabled(false);
        removeButton.addActionListener(e -> removeUserFromGroup());
        
        // 创建设置管理员按钮
        setAdminButton = new JButton("设置为管理员");
        setAdminButton.setFont(ModernTheme.REGULAR_FONT);
        setAdminButton.setEnabled(false);
        setAdminButton.addActionListener(e -> setUserAsAdmin());
        
        // 创建重置密码按钮
        resetPasswordButton = new JButton("重置密码");
        resetPasswordButton.setFont(ModernTheme.REGULAR_FONT);
        resetPasswordButton.setEnabled(false);
        resetPasswordButton.addActionListener(e -> resetUserPassword());
        
        panel.add(groupComboBox);
        panel.add(assignButton);
        panel.add(removeButton);
        panel.add(setAdminButton);
        panel.add(resetPasswordButton);
        
        return panel;
    }
    
    /**
     * 加载数据
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
                DefaultTableModel model = (DefaultTableModel) userTable.getModel();
                model.setRowCount(0); // 清空表格内容
                
                conn = DatabaseHelper.getConnection();
                if (conn != null) {
                    String query = "SELECT c.cNumber, c.cName, ua.account_type, ua.last_login " +
                                   "FROM client c " +
                                   "LEFT JOIN user_auth ua ON c.cNumber = ua.user_id " +
                                   "ORDER BY ua.last_login DESC, c.cNumber";
                    stmt = conn.prepareStatement(query);
                    rs = stmt.executeQuery();
                    
                    int rowCount = 0;
                    while (rs.next()) {
                        String userId = rs.getString("cNumber");
                        String userName = rs.getString("cName");
                        int accountType = rs.getInt("account_type");
                        String accountTypeStr = accountType == UserAuthManager.USER_TYPE_LEADER ? "管理员" : "普通用户";
                        String lastLogin = rs.getTimestamp("last_login") != null ? 
                                        rs.getTimestamp("last_login").toString() : "从未登录";
                        
                        model.addRow(new Object[]{
                            userId,
                            userName,
                            accountTypeStr,
                            lastLogin
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
                            updateStatus("没有找到用户记录");
                        }
                        
                        // 禁用按钮
                        assignButton.setEnabled(false);
                        removeButton.setEnabled(false);
                        setAdminButton.setEnabled(false);
                        resetPasswordButton.setEnabled(false);
                    });
                    
                } else {
                    // 在EDT中更新UI
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose();
                        updateStatus("无法连接到数据库");
                    });
                }
                
            } catch (Exception e) {
                handleException("加载用户数据失败", e);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    updateStatus("加载失败: " + e.getMessage());
                });
                
            } finally {
                // 确保关闭资源
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "关闭数据库资源失败", ex);
                }
            }
        });
    }
    
    /**
     * 加载所有权限组
     */
    private void loadAllGroups() {
        List<AuthService.PermissionGroup> groups = AuthService.getAllGroups();
        
        // 更新下拉框
        groupComboBox.removeAllItems();
        for (AuthService.PermissionGroup group : groups) {
            groupComboBox.addItem(group);
        }
    }
    
    /**
     * 加载用户权限组
     * @param userId 用户ID
     */
    private void loadUserGroups(String userId) {
        updateStatus("正在加载用户权限组...");
        
        // 在线程池中执行数据库操作
        executorService.submit(() -> {
            try {
                DefaultTableModel model = (DefaultTableModel) groupTable.getModel();
                model.setRowCount(0); // 清空表格内容
                
                List<AuthService.PermissionGroup> groups = AuthService.getUserGroups(userId);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    for (AuthService.PermissionGroup group : groups) {
                        model.addRow(new Object[]{
                            group.getGroupId(),
                            group.getGroupName(),
                            group.getDescription()
                        });
                    }
                    
                    if (groups.isEmpty()) {
                        updateStatus("用户没有分配权限组");
                    } else {
                        updateStatus("已加载用户权限组");
                    }
                });
                
            } catch (Exception e) {
                handleException("加载用户权限组失败", e);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    updateStatus("加载失败: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * 搜索用户
     * @param keyword 搜索关键字
     */
    private void searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadData();
            return;
        }
        
        updateStatus("正在搜索用户...");
        
        // 创建加载对话框
        final JDialog loadingDialog = UIHelper.createLoadingDialog(this, "正在搜索用户...");
        
        // 在EDT中显示对话框
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
        
        // 在线程池中执行数据库操作
        executorService.submit(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            
            try {
                DefaultTableModel model = (DefaultTableModel) userTable.getModel();
                model.setRowCount(0); // 清空表格内容
                
                conn = DatabaseHelper.getConnection();
                if (conn != null) {
                    String query = "SELECT c.cNumber, c.cName, ua.account_type, ua.last_login " +
                                   "FROM client c " +
                                   "LEFT JOIN user_auth ua ON c.cNumber = ua.user_id " +
                                   "WHERE c.cNumber LIKE ? OR c.cName LIKE ? " +
                                   "ORDER BY ua.last_login DESC, c.cNumber";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, "%" + keyword + "%");
                    stmt.setString(2, "%" + keyword + "%");
                    rs = stmt.executeQuery();
                    
                    int rowCount = 0;
                    while (rs.next()) {
                        String userId = rs.getString("cNumber");
                        String userName = rs.getString("cName");
                        int accountType = rs.getInt("account_type");
                        String accountTypeStr = accountType == UserAuthManager.USER_TYPE_LEADER ? "管理员" : "普通用户";
                        String lastLogin = rs.getTimestamp("last_login") != null ? 
                                        rs.getTimestamp("last_login").toString() : "从未登录";
                        
                        model.addRow(new Object[]{
                            userId,
                            userName,
                            accountTypeStr,
                            lastLogin
                        });
                        rowCount++;
                    }
                    
                    // 在EDT中更新UI
                    final int finalRowCount = rowCount;
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose();
                        if (finalRowCount > 0) {
                            updateStatus("搜索完成，找到 " + finalRowCount + " 条记录");
                        } else {
                            updateStatus("没有找到匹配的用户");
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
                handleException("搜索用户失败", e);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    updateStatus("搜索失败: " + e.getMessage());
                });
                
            } finally {
                // 确保关闭资源
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "关闭数据库资源失败", ex);
                }
            }
        });
    }
    
    /**
     * 更新按钮状态
     */
    private void updateButtonStatus() {
        boolean userSelected = selectedUserId != null && !selectedUserId.isEmpty();
        boolean groupSelected = groupTable.getSelectedRow() != -1;
        
        assignButton.setEnabled(userSelected);
        removeButton.setEnabled(userSelected && groupSelected);
        setAdminButton.setEnabled(userSelected);
        resetPasswordButton.setEnabled(userSelected);
    }
    
    /**
     * 分配用户到权限组
     */
    private void assignUserToGroup() {
        if (selectedUserId == null || selectedUserId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择用户", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 获取选中的权限组
        AuthService.PermissionGroup selectedGroup = (AuthService.PermissionGroup) groupComboBox.getSelectedItem();
        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "请选择权限组", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 确认分配
        int option = JOptionPane.showConfirmDialog(
            this,
            "确定要将用户 " + selectedUserId + " 分配到权限组 " + selectedGroup.getGroupName() + " 吗？",
            "确认分配",
            JOptionPane.YES_NO_OPTION
        );
        
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        
        // 禁用按钮防止重复点击
        assignButton.setEnabled(false);
        
        // 创建加载对话框
        final JDialog loadingDialog = UIHelper.createLoadingDialog(this, "正在分配用户到权限组...");
        
        // 在EDT中显示对话框
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
        
        // 使用线程池执行分配操作
        executorService.submit(() -> {
            try {
                boolean success = AuthService.assignUserToGroup(selectedUserId, selectedGroup.getGroupId());
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    
                    if (success) {
                        JOptionPane.showMessageDialog(
                            this, 
                            "用户已成功分配到权限组",
                            "成功", 
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        loadUserGroups(selectedUserId);
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "分配失败，可能该用户已经在此权限组中",
                            "失败",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                    updateButtonStatus();
                });
                
            } catch (Exception e) {
                handleException("分配用户到权限组失败", e);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    updateStatus("分配失败: " + e.getMessage());
                    updateButtonStatus();
                });
            }
        });
    }
    
    /**
     * 从权限组中移除用户
     */
    private void removeUserFromGroup() {
        if (selectedUserId == null || selectedUserId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择用户", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int selectedRow = groupTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要移除的权限组", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int groupId = (int) groupTable.getValueAt(selectedRow, 0);
        String groupName = (String) groupTable.getValueAt(selectedRow, 1);
        
        // 确认移除
        int option = JOptionPane.showConfirmDialog(
            this,
            "确定要将用户 " + selectedUserId + " 从权限组 " + groupName + " 中移除吗？",
            "确认移除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        
        // 禁用按钮防止重复点击
        removeButton.setEnabled(false);
        
        // 创建加载对话框
        final JDialog loadingDialog = UIHelper.createLoadingDialog(this, "正在从权限组移除用户...");
        
        // 在EDT中显示对话框
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
        
        // 使用线程池执行移除操作
        executorService.submit(() -> {
            try {
                boolean success = AuthService.removeUserFromGroup(selectedUserId, groupId);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    
                    if (success) {
                        JOptionPane.showMessageDialog(
                            this, 
                            "用户已成功从权限组中移除",
                            "成功", 
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        loadUserGroups(selectedUserId);
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "移除失败",
                            "失败",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                    updateButtonStatus();
                });
                
            } catch (Exception e) {
                handleException("从权限组移除用户失败", e);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    updateStatus("移除失败: " + e.getMessage());
                    updateButtonStatus();
                });
            }
        });
    }
    
    /**
     * 设置用户为管理员
     */
    private void setUserAsAdmin() {
        if (selectedUserId == null || selectedUserId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择用户", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 获取当前用户类型
        int selectedRow = userTable.getSelectedRow();
        String userType = (String) userTable.getValueAt(selectedRow, 2);
        boolean isAdmin = "管理员".equals(userType);
        
        // 确认操作
        int option = JOptionPane.showConfirmDialog(
            this,
            isAdmin ? 
                "确定要将用户 " + selectedUserId + " 设置为普通用户吗？" :
                "确定要将用户 " + selectedUserId + " 设置为管理员吗？",
            "确认操作",
            JOptionPane.YES_NO_OPTION
        );
        
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        
        // 禁用按钮防止重复点击
        setAdminButton.setEnabled(false);
        
        // 创建加载对话框
        final JDialog loadingDialog = UIHelper.createLoadingDialog(this, "正在更新用户类型...");
        
        // 在EDT中显示对话框
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
        
        // 使用线程池执行操作
        final int newAccountType = isAdmin ? 
                UserAuthManager.USER_TYPE_CLIENT : 
                UserAuthManager.USER_TYPE_LEADER;
        
        executorService.submit(() -> {
            try {
                boolean success = AuthService.setUserAccountType(selectedUserId, newAccountType);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    
                    if (success) {
                        JOptionPane.showMessageDialog(
                            this, 
                            "用户类型已成功更新",
                            "成功", 
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        // 更新表格数据
                        userTable.setValueAt(
                            newAccountType == UserAuthManager.USER_TYPE_LEADER ? "管理员" : "普通用户", 
                            selectedRow, 
                            2
                        );
                        
                        // 如果是设置为管理员，加载权限组
                        loadUserGroups(selectedUserId);
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "更新失败",
                            "失败",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                    updateButtonStatus();
                });
                
            } catch (Exception e) {
                handleException("更新用户类型失败", e);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    updateStatus("更新失败: " + e.getMessage());
                    updateButtonStatus();
                });
            }
        });
    }
    
    /**
     * 重置用户密码
     */
    private void resetUserPassword() {
        if (selectedUserId == null || selectedUserId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择用户", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 确认重置
        int option = JOptionPane.showConfirmDialog(
            this,
            "确定要重置用户 " + selectedUserId + " 的密码吗？",
            "确认重置",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        
        // 禁用按钮防止重复点击
        resetPasswordButton.setEnabled(false);
        
        // 创建加载对话框
        final JDialog loadingDialog = UIHelper.createLoadingDialog(this, "正在重置密码...");
        
        // 在EDT中显示对话框
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
        
        // 使用线程池执行重置操作
        executorService.submit(() -> {
            try {
                String newPassword = AuthService.resetPassword(selectedUserId);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    
                    if (newPassword != null) {
                        JOptionPane.showMessageDialog(
                            this, 
                            "密码已成功重置\n新密码: " + newPassword + "\n请记下这个密码！",
                            "成功", 
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "重置密码失败",
                            "失败",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                    resetPasswordButton.setEnabled(true);
                });
                
            } catch (Exception e) {
                handleException("重置密码失败", e);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    updateStatus("重置失败: " + e.getMessage());
                    resetPasswordButton.setEnabled(true);
                });
            }
        });
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new UserPermissionManager();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "启动应用程序时发生错误", e);
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