package com.hotel.ui;

import com.database.helper.DatabaseHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 表单模块基类
 * 为表单类型的界面提供通用功能
 */
public abstract class BaseFormModule extends BaseModuleFrame {
    
    // 表单组件
    protected JPanel formPanel;
    protected JTable dataTable;
    protected JButton submitButton;
    protected JButton clearButton;
    protected JButton refreshButton;
    
    /**
     * 构造函数
     * @param title 窗口标题
     * @param width 窗口宽度
     * @param height 窗口高度
     */
    public BaseFormModule(String title, int width, int height) {
        this(title, width, height, PERMISSION_NONE);
    }
    
    /**
     * 带权限检查的构造函数
     * @param title 窗口标题
     * @param width 窗口宽度
     * @param height 窗口高度
     * @param permissionLevel 需要的权限级别
     */
    public BaseFormModule(String title, int width, int height, int permissionLevel) {
        super(title, width, height, permissionLevel);
        // 只有权限检查通过后才初始化组件
        if (executorService != null) {
            initializeFormComponents();
        }
    }
    
    /**
     * 初始化表单组件
     */
    protected void initializeFormComponents() {
        try {
            // 设置内容面板布局
            contentPanel.setLayout(new BorderLayout(10, 20));
            
            // 创建表单面板
            formPanel = createFormPanel();
            
            // 创建表格面板
            JPanel tablePanel = createTablePanel();
            
            // 创建按钮面板
            JPanel buttonPanel = CommonUITemplate.createButtonPanel();
            
            // 创建基础按钮
            submitButton = CommonUITemplate.createActionButton("提交", ModernTheme.PRIMARY_COLOR, e -> submitForm());
            clearButton = CommonUITemplate.createActionButton("清空", ModernTheme.ACCENT_COLOR, e -> clearForm());
            refreshButton = CommonUITemplate.createActionButton("刷新", new Color(75, 179, 75), e -> refreshData());
            
            AnimationManager.addButtonClickAnimation(submitButton);
            AnimationManager.addButtonClickAnimation(clearButton);
            AnimationManager.addButtonClickAnimation(refreshButton);
            
            // 添加基础按钮
            buttonPanel.add(refreshButton);
            buttonPanel.add(clearButton);
            buttonPanel.add(submitButton);
            
            // 添加额外按钮
            addAdditionalButtons(buttonPanel);
            
            // 将表单和表格添加到内容面板
            contentPanel.add(formPanel, BorderLayout.NORTH);
            contentPanel.add(tablePanel, BorderLayout.CENTER);
            
            // 替换主面板的按钮区域
            if (mainPanel.getComponentCount() > 2) {
                mainPanel.remove(2); // 移除原按钮面板
            }
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            // 刷新表格数据
            refreshData();
            
        } catch (Exception e) {
            handleException("初始化表单组件失败", e);
        }
    }
    
    /**
     * 创建表单面板
     */
    protected JPanel createFormPanel() {
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
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 创建表单标题
        JLabel formTitle = new JLabel(getFormTitle());
        formTitle.setFont(ModernTheme.HEADER_FONT);
        formTitle.setForeground(ModernTheme.PRIMARY_COLOR);
        
        GridBagConstraints c = CommonUITemplate.createFormConstraints(0, 0, 2, 1.0);
        c.anchor = GridBagConstraints.WEST;
        panel.add(formTitle, c);
        
        // 添加表单字段
        addFormFields(panel);
        
        return panel;
    }
    
    /**
     * 创建表格面板
     */
    protected JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // 创建表格标题
        JLabel tableTitle = new JLabel(getTableTitle());
        tableTitle.setFont(ModernTheme.HEADER_FONT);
        tableTitle.setForeground(ModernTheme.PRIMARY_COLOR);
        tableTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        // 创建表格模型
        DefaultTableModel model = createTableModel();
        
        // 创建表格
        dataTable = new JTable(model);
        ModernTableRenderer.applyModernStyle(dataTable);
        
        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ModernTheme.BACKGROUND_COLOR);
        
        panel.add(tableTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 在线程池中执行数据库操作
     * @param operation 数据库操作
     * @param successMessage 成功消息
     * @param errorMessage 错误消息
     */
    protected void executeDbOperation(Runnable operation, String processingMessage, String successMessage, String errorMessage) {
        updateStatus(processingMessage);
        
        // 创建加载对话框
        final JDialog loadingDialog = UIHelper.createLoadingDialog(this, processingMessage);
        
        // 在EDT中显示对话框
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
        
        // 在线程池中执行操作
        executorService.submit(() -> {
            try {
                // 执行操作
                operation.run();
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    JOptionPane.showMessageDialog(this, successMessage, "成功", JOptionPane.INFORMATION_MESSAGE);
                    updateStatus(successMessage);
                    refreshData();
                    clearForm();
                });
                
            } catch (Exception e) {
                handleException(errorMessage, e);
                
                // 在EDT中更新UI
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    updateStatus("操作失败: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * 安全执行数据库查询
     * @param query SQL查询
     * @param resultProcessor 结果处理器
     */
    protected void safeExecuteQuery(String query, QueryResultProcessor resultProcessor) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // 获取数据库连接
            updateStatus("正在连接数据库...");
            conn = DatabaseHelper.getConnection();
            
            if (conn != null) {
                updateStatus("正在查询数据...");
                stmt = conn.prepareStatement(query);
                rs = stmt.executeQuery();
                
                // 处理结果
                resultProcessor.process(rs);
                updateStatus("查询完成");
            } else {
                updateStatus("无法连接到数据库");
            }
        } catch (Exception e) {
            handleException("执行查询失败", e);
        } finally {
            // 确保关闭资源
            UIHelper.safeCloseResources(conn, stmt, rs);
        }
    }
    
    /**
     * 查询结果处理器接口
     */
    @FunctionalInterface
    public interface QueryResultProcessor {
        void process(ResultSet rs) throws Exception;
    }
    
    /**
     * 获取表单标题
     */
    protected abstract String getFormTitle();
    
    /**
     * 获取表格标题
     */
    protected abstract String getTableTitle();
    
    /**
     * 创建表格模型
     */
    protected abstract DefaultTableModel createTableModel();
    
    /**
     * 添加表单字段
     * @param formPanel 表单面板
     */
    protected abstract void addFormFields(JPanel formPanel);
    
    /**
     * 添加额外按钮
     * @param buttonPanel 按钮面板
     */
    protected void addAdditionalButtons(JPanel buttonPanel) {
        // 子类可以覆盖此方法添加额外按钮
    }
    
    /**
     * 提交表单
     * 子类必须实现此方法
     */
    protected abstract void submitForm();
    
    /**
     * 清空表单
     * 子类必须实现此方法
     */
    protected abstract void clearForm();
    
    /**
     * 刷新数据
     * 子类必须实现此方法
     */
    protected abstract void refreshData();
} 