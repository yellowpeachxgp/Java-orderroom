package com.all.search;

import com.database.helper.DatabaseHelper;
import com.hotel.ui.AnimationManager;
import com.hotel.ui.CommonUITemplate;
import com.hotel.ui.ModernTableRenderer;
import com.hotel.ui.ModernTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class add_client extends JFrame {
    // 添加日志记录器
    private static final Logger LOGGER = Logger.getLogger(add_client.class.getName());
    
    // 创建线程池
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    private JTable table; // 表格
    // 文本框：客户姓名，客户年龄，客户性别，客户电话
    private JTextField cNameField, cAgeField, cSexField, cTelField;
    // 用户编号标签（自动生成，不可编辑）
    private JLabel cNumberLabel;
    private String generatedUserNumber;
    private JButton addButton; // 添加按钮
    private JPanel mainPanel; // 主面板
    // 状态标签
    private JLabel statusLabel;

    // 构造函数
    public add_client() {
        try {
            // 使用通用模板创建基本窗口
            setTitle("添加用户");
            setSize(800, 600);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);
            
            // 应用现代主题
            ModernTheme.applyTheme();
            
            // 创建主面板
            mainPanel = CommonUITemplate.createMainPanel();
            
            // 创建顶部标题面板
            JPanel headerPanel = CommonUITemplate.createHeaderPanel("添加用户", e -> {
                executorService.shutdown(); // 关闭线程池
                dispose();
            });
            
            // 创建内容面板
            JPanel contentPanel = CommonUITemplate.createContentPanel();
            contentPanel.setLayout(new BorderLayout(10, 20));
            
            // 创建表单面板
            JPanel formPanel = new JPanel() {
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
            formPanel.setLayout(new GridBagLayout());
            formPanel.setOpaque(false);
            formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            // 创建标题
            JLabel formTitle = new JLabel("输入用户信息");
            formTitle.setFont(ModernTheme.HEADER_FONT);
            formTitle.setForeground(ModernTheme.PRIMARY_COLOR);
            GridBagConstraints c = CommonUITemplate.createFormConstraints(0, 0, 2, 1.0);
            c.anchor = GridBagConstraints.WEST;
            formPanel.add(formTitle, c);
            
            // 自动生成用户编号
            generatedUserNumber = generateUserNumber();
            
            // 创建用户编号显示区域（只读）
            JPanel cNumberPanel = new JPanel(new BorderLayout(10, 0));
            cNumberPanel.setOpaque(false);
            JLabel cNumberTitleLabel = ModernTheme.createLabel("用户编号:");
            cNumberLabel = new JLabel(generatedUserNumber);
            cNumberLabel.setFont(new Font(cNumberLabel.getFont().getName(), Font.BOLD, 14));
            cNumberLabel.setForeground(ModernTheme.PRIMARY_COLOR);
            cNumberPanel.add(cNumberTitleLabel, BorderLayout.WEST);
            cNumberPanel.add(cNumberLabel, BorderLayout.CENTER);
            
            // 创建文本输入字段
            cNameField = ModernTheme.createModernTextField(15);
            cAgeField = ModernTheme.createModernTextField(15);
            cSexField = ModernTheme.createModernTextField(15);
            cTelField = ModernTheme.createModernTextField(15);
            
            // 添加输入字段到表单
            // 用户编号（自动生成）
            c = CommonUITemplate.createFormConstraints(0, 1, 2, 1.0);
            formPanel.add(cNumberPanel, c);
            
            // 用户姓名
            c = CommonUITemplate.createFormConstraints(0, 2, 1, 0.0);
            c.anchor = GridBagConstraints.EAST;
            formPanel.add(ModernTheme.createLabel("用户姓名:"), c);
            
            c = CommonUITemplate.createFormConstraints(1, 2, 1, 1.0);
            formPanel.add(cNameField, c);
            
            // 用户年龄
            c = CommonUITemplate.createFormConstraints(0, 3, 1, 0.0);
            c.anchor = GridBagConstraints.EAST;
            formPanel.add(ModernTheme.createLabel("用户年龄:"), c);
            
            c = CommonUITemplate.createFormConstraints(1, 3, 1, 1.0);
            formPanel.add(cAgeField, c);
            
            // 用户性别
            c = CommonUITemplate.createFormConstraints(0, 4, 1, 0.0);
            c.anchor = GridBagConstraints.EAST;
            formPanel.add(ModernTheme.createLabel("用户性别:"), c);
            
            c = CommonUITemplate.createFormConstraints(1, 4, 1, 1.0);
            formPanel.add(cSexField, c);
            
            // 用户电话
            c = CommonUITemplate.createFormConstraints(0, 5, 1, 0.0);
            c.anchor = GridBagConstraints.EAST;
            formPanel.add(ModernTheme.createLabel("用户电话:"), c);
            
            c = CommonUITemplate.createFormConstraints(1, 5, 1, 1.0);
            formPanel.add(cTelField, c);
            
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
            
            // 创建表格面板
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setOpaque(false);
            tablePanel.setBorder(new EmptyBorder(20, 0, 0, 0));
            
            // 添加表格标题
            JLabel tableTitle = new JLabel("用户列表");
            tableTitle.setFont(ModernTheme.HEADER_FONT);
            tableTitle.setForeground(ModernTheme.PRIMARY_COLOR);
            tableTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
            
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(ModernTheme.BACKGROUND_COLOR);
            
            tablePanel.add(tableTitle, BorderLayout.NORTH);
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            
            // 创建状态面板和状态标签
            JPanel statusPanel = new JPanel(new BorderLayout());
            statusPanel.setOpaque(false);
            statusLabel = new JLabel("就绪");
            statusLabel.setForeground(ModernTheme.TEXT_SECONDARY);
            statusLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
            statusPanel.add(statusLabel, BorderLayout.WEST);
            
            // 将表单和表格添加到内容面板
            contentPanel.add(formPanel, BorderLayout.NORTH);
            contentPanel.add(tablePanel, BorderLayout.CENTER);
            contentPanel.add(statusPanel, BorderLayout.SOUTH);
            
            // 创建按钮面板
            JPanel buttonPanel = CommonUITemplate.createButtonPanel();
            
            // 创建添加按钮
            addButton = CommonUITemplate.createActionButton("添加用户", ModernTheme.PRIMARY_COLOR, 
                e -> addClientAction());
            
            // 创建清空按钮
            JButton clearButton = CommonUITemplate.createActionButton("清空输入", ModernTheme.ACCENT_COLOR, 
                e -> clearFields());
            
            // 创建刷新按钮（生成新的用户编号）
            JButton refreshButton = CommonUITemplate.createActionButton("刷新编号", new Color(75, 179, 75), 
                e -> refreshUserNumber());
            
            buttonPanel.add(refreshButton);
            buttonPanel.add(clearButton);
            buttonPanel.add(addButton);
            
            // 组装主面板
            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(contentPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            // 添加到窗口
            add(mainPanel);
            
            // 显示窗口
            setVisible(true);
            
            // 应用淡入动画
            CommonUITemplate.applyFadeInAnimation(mainPanel);
            
            // 初始化表格数据
            updateStatus("正在加载用户数据...");
            refreshPage();
            
        } catch (Exception e) {
            handleException("初始化界面时发生错误", e);
        }
    }
    
    /**
     * 添加用户操作
     */
    private void addClientAction() {
        try {
            String cName = cNameField.getText();
            String cAge = cAgeField.getText();
            String cSex = cSexField.getText();
            String cTel = cTelField.getText();
            
            if (cName.isEmpty() || cAge.isEmpty() || cSex.isEmpty() || cTel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请填写完整信息", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 验证年龄格式
            try {
                int age = Integer.parseInt(cAge);
                if (age <= 0 || age > 120) {
                    JOptionPane.showMessageDialog(this, "请输入有效的年龄（1-120）", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "年龄必须是数字", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 验证性别格式
            if (!cSex.equals("男") && !cSex.equals("女")) {
                JOptionPane.showMessageDialog(this, "性别必须是\"男\"或\"女\"", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 验证电话格式
            if (!cTel.matches("\\d{11}")) {
                JOptionPane.showMessageDialog(this, "请输入有效的11位电话号码", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 禁用按钮防止重复提交
            addButton.setEnabled(false);
            
            // 创建加载对话框
            final JDialog loadingDialog = CommonUITemplate.createLoadingDialog(this, "正在添加用户...");
            
            // 使用线程池执行耗时操作，避免阻塞EDT线程
            final String userNumber = this.generatedUserNumber; // 捕获当前编号
            updateStatus("正在添加用户...");
            
            // 在事件分发线程中显示对话框（避免线程问题）
            SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
            
            // 使用线程池执行数据库操作
            executorService.submit(() -> {
                try {
                    LOGGER.info("开始添加用户: " + userNumber);
                    // 添加用户
                    boolean success = addClient(userNumber, cName, cAge, cSex, cTel);
                    
                    // 在EDT中更新UI
                    SwingUtilities.invokeLater(() -> {
                        try {
                            // 关闭加载对话框
                            loadingDialog.dispose();
                            
                            if (success) {
                                JOptionPane.showMessageDialog(this, "用户添加成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                                // 添加成功后，刷新用户编号和表单
                                refreshUserNumber();
                                clearFields();
                                refreshPage();
                                updateStatus("用户添加成功");
                            } else {
                                JOptionPane.showMessageDialog(this, "添加失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
                                updateStatus("用户添加失败");
                            }
                        } catch (Exception e) {
                            handleException("更新UI时发生错误", e);
                        } finally {
                            // 恢复按钮状态
                            addButton.setEnabled(true);
                        }
                    });
                    
                } catch (Exception e) {
                    handleException("添加用户过程中发生错误", e);
                    
                    // 在EDT中更新UI
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose();
                        addButton.setEnabled(true);
                        updateStatus("操作失败: " + e.getMessage());
                    });
                }
            });
        } catch (Exception e) {
            handleException("处理添加用户请求时发生错误", e);
            addButton.setEnabled(true);
        }
    }
    
    /**
     * 生成用户编号
     */
    private String generateUserNumber() {
        try {
            updateStatus("正在生成用户编号...");
            // 使用DatabaseHelper中的方法生成下一个用户ID
            String id = DatabaseHelper.getNextId("client", "cNumber", "USER");
            updateStatus("用户编号生成成功");
            return id;
        } catch (Exception e) {
            handleException("生成用户编号时发生错误", e);
            return "USER001"; // 出错时的默认值
        }
    }
    
    /**
     * 刷新用户编号
     */
    private void refreshUserNumber() {
        try {
            final String oldNumber = generatedUserNumber;
            generatedUserNumber = generateUserNumber();
            cNumberLabel.setText(generatedUserNumber);
            LOGGER.info("用户编号已刷新: " + oldNumber + " -> " + generatedUserNumber);
        } catch (Exception e) {
            handleException("刷新用户编号时发生错误", e);
        }
    }
    
    /**
     * 清空输入字段
     */
    private void clearFields() {
        try {
            cNameField.setText("");
            cAgeField.setText("");
            cSexField.setText("");
            cTelField.setText("");
            cNameField.requestFocus();
            updateStatus("表单已清空");
        } catch (Exception e) {
            handleException("清空表单时发生错误", e);
        }
    }
    
    /**
     * 更新状态栏文本
     */
    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(message);
            }
            LOGGER.info(message);
        });
    }
    
    /**
     * 处理异常并显示错误对话框
     */
    private void handleException(String message, Exception e) {
        // 记录错误到日志
        LOGGER.log(Level.SEVERE, message, e);
        
        // 获取完整堆栈跟踪
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        
        // 在EDT中显示错误对话框
        SwingUtilities.invokeLater(() -> {
            // 创建可以滚动的错误消息
            JTextArea textArea = new JTextArea(stackTrace);
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            
            // 显示错误对话框
            JOptionPane.showMessageDialog(
                this,
                new Object[]{message + ": " + e.getMessage(), scrollPane},
                "错误",
                JOptionPane.ERROR_MESSAGE
            );
            
            // 更新状态栏
            updateStatus("发生错误: " + e.getMessage());
        });
    }

    public static void main(String[] args) {
        // 确保在EDT中创建UI
        SwingUtilities.invokeLater(() -> {
            try {
                new add_client();
            } catch (Exception e) {
                Logger logger = Logger.getLogger(add_client.class.getName());
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

    /**
     * 添加用户信息
     */
    private boolean addClient(String cNumber, String cName, String cAge, String cSex, String cTel) {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            LOGGER.info("开始添加用户 - 编号: " + cNumber);
            updateStatus("正在连接数据库...");
            
            connection = DatabaseHelper.getConnection();
            if (connection == null) {
                LOGGER.severe("无法获取数据库连接");
                return false;
            }
            
            updateStatus("正在执行SQL插入...");
            // 添加新用户
            String query = "INSERT INTO client (cNumber, cName, cAge, cSex, cTel) VALUES (?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(query);
            stmt.setString(1, cNumber);
            stmt.setString(2, cName);
            stmt.setString(3, cAge);
            stmt.setString(4, cSex);
            stmt.setString(5, cTel);
            
            LOGGER.info("执行SQL: " + query + " [参数: " + cNumber + ", " + cName + ", " + cAge + ", " + cSex + ", " + cTel + "]");
            int result = stmt.executeUpdate();
            
            LOGGER.info("SQL执行结果: 影响 " + result + " 行");
            updateStatus("数据库操作完成");
            
            return result > 0; // 如果影响的行数大于0，则添加成功
            
        } catch (SQLException e) {
            handleException("数据库操作失败", e);
            return false;
        } finally {
            // 确保关闭资源
            try {
                updateStatus("正在关闭数据库连接...");
                DatabaseHelper.closeResources(connection, stmt, null);
                updateStatus("数据库连接已关闭");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "关闭数据库资源时发生错误", e);
            }
        }
    }

    /**
     * 刷新表格数据
     */
    public void refreshPage() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // 清空表格内容
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            updateStatus("正在刷新表格数据...");
            
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
                
                updateStatus("数据加载完成，共 " + rowCount + " 条记录");
            } else {
                updateStatus("无法连接到数据库");
            }
        } catch (SQLException e) {
            handleException("刷新表格数据失败", e);
        } finally {
            // 确保关闭资源
            try {
                DatabaseHelper.closeResources(conn, stmt, rs);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "关闭数据库资源时发生错误", e);
            }
        }
    }
    
    /**
     * 窗口关闭时清理资源
     */
    @Override
    public void dispose() {
        try {
            // 关闭线程池
            executorService.shutdown();
            LOGGER.info("应用正在关闭，已关闭线程池");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "关闭线程池时发生错误", e);
        }
        super.dispose();
    }
}
