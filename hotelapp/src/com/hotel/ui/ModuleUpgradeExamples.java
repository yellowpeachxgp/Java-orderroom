package com.hotel.ui;

/**
 * 模块升级代码示例
 * 提供常用的代码模板，帮助开发者快速升级现有模块
 */
public class ModuleUpgradeExamples {
    
    /**
     * 基础模块模板
     */
    public static final String BASE_MODULE_TEMPLATE = 
        "import com.database.helper.DatabaseHelper;\n" +
        "import com.hotel.ui.BaseModuleFrame;\n" +
        "import com.hotel.ui.CommonUITemplate;\n" +
        "import com.hotel.ui.ModernTableRenderer;\n" +
        "import com.hotel.ui.ModernTheme;\n" +
        "import com.hotel.ui.UIHelper;\n\n" +
        
        "import javax.swing.*;\n" +
        "import javax.swing.border.EmptyBorder;\n" +
        "import javax.swing.table.DefaultTableModel;\n" +
        "import java.awt.*;\n" +
        "import java.sql.Connection;\n" +
        "import java.sql.PreparedStatement;\n" +
        "import java.sql.ResultSet;\n" +
        "import java.util.logging.Level;\n" +
        "import java.util.logging.Logger;\n\n" +
        
        "/**\n" +
        " * 模块名称\n" +
        " * 模块功能描述\n" +
        " */\n" +
        "public class MyModule extends BaseModuleFrame {\n" +
        "    // 添加日志记录器\n" +
        "    private static final Logger LOGGER = Logger.getLogger(MyModule.class.getName());\n" +
        "    \n" +
        "    // 声明UI组件\n" +
        "    private JTable dataTable;\n" +
        "    private JButton actionButton;\n" +
        "    \n" +
        "    /**\n" +
        "     * 构造函数\n" +
        "     */\n" +
        "    public MyModule() {\n" +
        "        super(\"模块标题\", 800, 600);\n" +
        "        \n" +
        "        try {\n" +
        "            // 初始化组件\n" +
        "            initializeComponents();\n" +
        "            \n" +
        "            // 加载数据\n" +
        "            loadData();\n" +
        "            \n" +
        "            // 显示窗口并应用动画\n" +
        "            showWithAnimation();\n" +
        "            \n" +
        "        } catch (Exception e) {\n" +
        "            handleException(\"初始化界面失败\", e);\n" +
        "        }\n" +
        "    }\n" +
        "    \n" +
        "    /**\n" +
        "     * 初始化组件\n" +
        "     */\n" +
        "    private void initializeComponents() {\n" +
        "        try {\n" +
        "            // 设置内容面板布局\n" +
        "            contentPanel.setLayout(new BorderLayout(10, 10));\n" +
        "            \n" +
        "            // 创建表格面板\n" +
        "            JPanel tablePanel = new JPanel(new BorderLayout());\n" +
        "            tablePanel.setOpaque(false);\n" +
        "            tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));\n" +
        "            \n" +
        "            // 创建表格标题\n" +
        "            JLabel tableTitle = new JLabel(\"数据列表\");\n" +
        "            tableTitle.setFont(ModernTheme.HEADER_FONT);\n" +
        "            tableTitle.setForeground(ModernTheme.PRIMARY_COLOR);\n" +
        "            tableTitle.setBorder(new EmptyBorder(0, 0, 10, 0));\n" +
        "            \n" +
        "            // 创建表格模型\n" +
        "            DefaultTableModel model = new DefaultTableModel() {\n" +
        "                @Override\n" +
        "                public boolean isCellEditable(int row, int column) {\n" +
        "                    return false; // 设置表格不可编辑\n" +
        "                }\n" +
        "            };\n" +
        "            // 添加表格列\n" +
        "            model.addColumn(\"列1\");\n" +
        "            model.addColumn(\"列2\");\n" +
        "            \n" +
        "            // 创建表格\n" +
        "            dataTable = new JTable(model);\n" +
        "            ModernTableRenderer.applyModernStyle(dataTable);\n" +
        "            \n" +
        "            JScrollPane scrollPane = new JScrollPane(dataTable);\n" +
        "            scrollPane.setBorder(BorderFactory.createEmptyBorder());\n" +
        "            scrollPane.getViewport().setBackground(ModernTheme.BACKGROUND_COLOR);\n" +
        "            \n" +
        "            tablePanel.add(tableTitle, BorderLayout.NORTH);\n" +
        "            tablePanel.add(scrollPane, BorderLayout.CENTER);\n" +
        "            \n" +
        "            // 创建按钮面板\n" +
        "            JPanel buttonPanel = CommonUITemplate.createButtonPanel();\n" +
        "            \n" +
        "            // 创建刷新按钮\n" +
        "            JButton refreshButton = CommonUITemplate.createActionButton(\"刷新\", new Color(75, 179, 75), e -> loadData());\n" +
        "            \n" +
        "            // 创建操作按钮\n" +
        "            actionButton = CommonUITemplate.createActionButton(\"操作\", ModernTheme.PRIMARY_COLOR, e -> performAction());\n" +
        "            \n" +
        "            buttonPanel.add(refreshButton);\n" +
        "            buttonPanel.add(actionButton);\n" +
        "            \n" +
        "            // 将面板添加到内容面板\n" +
        "            contentPanel.add(tablePanel, BorderLayout.CENTER);\n" +
        "            \n" +
        "            // 替换按钮面板\n" +
        "            if (mainPanel.getComponentCount() > 2) {\n" +
        "                mainPanel.remove(2); // 移除原按钮面板\n" +
        "            }\n" +
        "            mainPanel.add(buttonPanel, BorderLayout.SOUTH);\n" +
        "            \n" +
        "        } catch (Exception e) {\n" +
        "            handleException(\"初始化组件失败\", e);\n" +
        "        }\n" +
        "    }\n" +
        "    \n" +
        "    /**\n" +
        "     * 加载数据\n" +
        "     */\n" +
        "    private void loadData() {\n" +
        "        updateStatus(\"正在加载数据...\");\n" +
        "        \n" +
        "        // 创建加载对话框\n" +
        "        final JDialog loadingDialog = UIHelper.createLoadingDialog(this, \"正在加载数据...\");\n" +
        "        \n" +
        "        // 在EDT中显示对话框\n" +
        "        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));\n" +
        "        \n" +
        "        // 在线程池中执行数据库操作\n" +
        "        executorService.submit(() -> {\n" +
        "            Connection conn = null;\n" +
        "            PreparedStatement stmt = null;\n" +
        "            ResultSet rs = null;\n" +
        "            \n" +
        "            try {\n" +
        "                DefaultTableModel model = (DefaultTableModel) dataTable.getModel();\n" +
        "                model.setRowCount(0); // 清空表格内容\n" +
        "                \n" +
        "                conn = DatabaseHelper.getConnection();\n" +
        "                if (conn != null) {\n" +
        "                    String query = \"SELECT * FROM table_name\";\n" +
        "                    stmt = conn.prepareStatement(query);\n" +
        "                    rs = stmt.executeQuery();\n" +
        "                    \n" +
        "                    int rowCount = 0;\n" +
        "                    while (rs.next()) {\n" +
        "                        model.addRow(new Object[]{\n" +
        "                            rs.getString(\"column1\"),\n" +
        "                            rs.getString(\"column2\")\n" +
        "                        });\n" +
        "                        rowCount++;\n" +
        "                    }\n" +
        "                    \n" +
        "                    // 在EDT中更新UI\n" +
        "                    final int finalRowCount = rowCount;\n" +
        "                    SwingUtilities.invokeLater(() -> {\n" +
        "                        loadingDialog.dispose();\n" +
        "                        updateStatus(\"数据加载完成，共 \" + finalRowCount + \" 条记录\");\n" +
        "                    });\n" +
        "                    \n" +
        "                } else {\n" +
        "                    // 在EDT中更新UI\n" +
        "                    SwingUtilities.invokeLater(() -> {\n" +
        "                        loadingDialog.dispose();\n" +
        "                        updateStatus(\"无法连接到数据库\");\n" +
        "                    });\n" +
        "                }\n" +
        "                \n" +
        "            } catch (Exception e) {\n" +
        "                handleException(\"加载数据失败\", e);\n" +
        "                \n" +
        "                // 在EDT中更新UI\n" +
        "                SwingUtilities.invokeLater(() -> {\n" +
        "                    loadingDialog.dispose();\n" +
        "                    updateStatus(\"加载失败: \" + e.getMessage());\n" +
        "                });\n" +
        "                \n" +
        "            } finally {\n" +
        "                // 确保关闭资源\n" +
        "                UIHelper.safeCloseResources(conn, stmt, rs);\n" +
        "            }\n" +
        "        });\n" +
        "    }\n" +
        "    \n" +
        "    /**\n" +
        "     * 执行操作\n" +
        "     */\n" +
        "    private void performAction() {\n" +
        "        // 在这里实现具体操作\n" +
        "        updateStatus(\"执行操作...\");\n" +
        "        \n" +
        "        // 执行数据库操作示例\n" +
        "        final JDialog loadingDialog = UIHelper.createLoadingDialog(this, \"执行操作中...\");\n" +
        "        \n" +
        "        // 在EDT中显示对话框\n" +
        "        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));\n" +
        "        \n" +
        "        // 在线程池中执行操作\n" +
        "        executorService.submit(() -> {\n" +
        "            try {\n" +
        "                // 模拟耗时操作\n" +
        "                Thread.sleep(1000);\n" +
        "                \n" +
        "                // 在EDT中更新UI\n" +
        "                SwingUtilities.invokeLater(() -> {\n" +
        "                    loadingDialog.dispose();\n" +
        "                    JOptionPane.showMessageDialog(this, \"操作成功！\", \"成功\", JOptionPane.INFORMATION_MESSAGE);\n" +
        "                    updateStatus(\"操作成功\");\n" +
        "                    loadData(); // 刷新数据\n" +
        "                });\n" +
        "                \n" +
        "            } catch (Exception e) {\n" +
        "                handleException(\"执行操作失败\", e);\n" +
        "                \n" +
        "                // 在EDT中更新UI\n" +
        "                SwingUtilities.invokeLater(() -> {\n" +
        "                    loadingDialog.dispose();\n" +
        "                    updateStatus(\"操作失败: \" + e.getMessage());\n" +
        "                });\n" +
        "            }\n" +
        "        });\n" +
        "    }\n" +
        "    \n" +
        "    /**\n" +
        "     * 主方法\n" +
        "     */\n" +
        "    public static void main(String[] args) {\n" +
        "        // 确保在EDT中创建UI\n" +
        "        SwingUtilities.invokeLater(() -> {\n" +
        "            try {\n" +
        "                new MyModule();\n" +
        "            } catch (Exception e) {\n" +
        "                Logger logger = Logger.getLogger(MyModule.class.getName());\n" +
        "                logger.log(Level.SEVERE, \"启动应用程序时发生错误\", e);\n" +
        "                JOptionPane.showMessageDialog(\n" +
        "                    null,\n" +
        "                    \"启动应用程序时发生错误: \" + e.getMessage(),\n" +
        "                    \"错误\",\n" +
        "                    JOptionPane.ERROR_MESSAGE\n" +
        "                );\n" +
        "            }\n" +
        "        });\n" +
        "    }\n" +
        "}";
    
    /**
     * 表单模块模板
     */
    public static final String FORM_MODULE_TEMPLATE = 
        "import com.database.helper.DatabaseHelper;\n" +
        "import com.hotel.ui.BaseFormModule;\n" +
        "import com.hotel.ui.ModernTheme;\n" +
        "import com.hotel.ui.CommonUITemplate;\n\n" +
        
        "import javax.swing.*;\n" +
        "import javax.swing.table.DefaultTableModel;\n" +
        "import java.awt.*;\n" +
        "import java.sql.ResultSet;\n" +
        "import java.util.logging.Level;\n" +
        "import java.util.logging.Logger;\n\n" +
        
        "/**\n" +
        " * 表单模块名称\n" +
        " * 表单模块功能描述\n" +
        " */\n" +
        "public class MyFormModule extends BaseFormModule {\n" +
        "    // 添加日志记录器\n" +
        "    private static final Logger LOGGER = Logger.getLogger(MyFormModule.class.getName());\n" +
        "    \n" +
        "    // 声明表单字段\n" +
        "    private JTextField field1;\n" +
        "    private JTextField field2;\n" +
        "    \n" +
        "    /**\n" +
        "     * 构造函数\n" +
        "     */\n" +
        "    public MyFormModule() {\n" +
        "        super(\"表单模块标题\", 800, 600);\n" +
        "        showWithAnimation();\n" +
        "    }\n" +
        "    \n" +
        "    @Override\n" +
        "    protected String getFormTitle() {\n" +
        "        return \"表单标题\";\n" +
        "    }\n" +
        "    \n" +
        "    @Override\n" +
        "    protected String getTableTitle() {\n" +
        "        return \"数据列表\";\n" +
        "    }\n" +
        "    \n" +
        "    @Override\n" +
        "    protected DefaultTableModel createTableModel() {\n" +
        "        DefaultTableModel model = new DefaultTableModel() {\n" +
        "            @Override\n" +
        "            public boolean isCellEditable(int row, int column) {\n" +
        "                return false; // 设置表格不可编辑\n" +
        "            }\n" +
        "        };\n" +
        "        model.addColumn(\"列1\");\n" +
        "        model.addColumn(\"列2\");\n" +
        "        return model;\n" +
        "    }\n" +
        "    \n" +
        "    @Override\n" +
        "    protected void addFormFields(JPanel formPanel) {\n" +
        "        // 创建文本输入字段\n" +
        "        field1 = ModernTheme.createModernTextField(15);\n" +
        "        field2 = ModernTheme.createModernTextField(15);\n" +
        "        \n" +
        "        // 添加输入字段到表单\n" +
        "        GridBagConstraints c;\n" +
        "        \n" +
        "        // 字段1\n" +
        "        c = CommonUITemplate.createFormConstraints(0, 1, 1, 0.0);\n" +
        "        c.anchor = GridBagConstraints.EAST;\n" +
        "        formPanel.add(ModernTheme.createLabel(\"字段1:\"), c);\n" +
        "        \n" +
        "        c = CommonUITemplate.createFormConstraints(1, 1, 1, 1.0);\n" +
        "        formPanel.add(field1, c);\n" +
        "        \n" +
        "        // 字段2\n" +
        "        c = CommonUITemplate.createFormConstraints(0, 2, 1, 0.0);\n" +
        "        c.anchor = GridBagConstraints.EAST;\n" +
        "        formPanel.add(ModernTheme.createLabel(\"字段2:\"), c);\n" +
        "        \n" +
        "        c = CommonUITemplate.createFormConstraints(1, 2, 1, 1.0);\n" +
        "        formPanel.add(field2, c);\n" +
        "    }\n" +
        "    \n" +
        "    @Override\n" +
        "    protected void submitForm() {\n" +
        "        try {\n" +
        "            String value1 = field1.getText();\n" +
        "            String value2 = field2.getText();\n" +
        "            \n" +
        "            // 验证表单\n" +
        "            if (value1.isEmpty() || value2.isEmpty()) {\n" +
        "                JOptionPane.showMessageDialog(this, \"请填写完整信息\", \"输入错误\", JOptionPane.ERROR_MESSAGE);\n" +
        "                return;\n" +
        "            }\n" +
        "            \n" +
        "            // 执行数据库操作\n" +
        "            executeDbOperation(\n" +
        "                // 数据库操作\n" +
        "                () -> {\n" +
        "                    // 实现具体的数据库操作\n" +
        "                    LOGGER.info(\"执行表单提交操作\");\n" +
        "                    Thread.sleep(1000); // 模拟耗时操作\n" +
        "                    return true;\n" +
        "                },\n" +
        "                \"正在处理数据...\",\n" +
        "                \"数据提交成功\",\n" +
        "                \"提交数据失败\"\n" +
        "            );\n" +
        "            \n" +
        "        } catch (Exception e) {\n" +
        "            handleException(\"提交表单失败\", e);\n" +
        "        }\n" +
        "    }\n" +
        "    \n" +
        "    @Override\n" +
        "    protected void clearForm() {\n" +
        "        field1.setText(\"\");\n" +
        "        field2.setText(\"\");\n" +
        "        field1.requestFocus();\n" +
        "        updateStatus(\"表单已清空\");\n" +
        "    }\n" +
        "    \n" +
        "    @Override\n" +
        "    protected void refreshData() {\n" +
        "        updateStatus(\"正在刷新数据...\");\n" +
        "        \n" +
        "        // 安全执行数据库查询\n" +
        "        safeExecuteQuery(\n" +
        "            \"SELECT * FROM table_name\",\n" +
        "            (ResultSet rs) -> {\n" +
        "                DefaultTableModel model = (DefaultTableModel) dataTable.getModel();\n" +
        "                model.setRowCount(0); // 清空表格内容\n" +
        "                \n" +
        "                int rowCount = 0;\n" +
        "                while (rs.next()) {\n" +
        "                    model.addRow(new Object[]{\n" +
        "                        rs.getString(\"column1\"),\n" +
        "                        rs.getString(\"column2\")\n" +
        "                    });\n" +
        "                    rowCount++;\n" +
        "                }\n" +
        "                \n" +
        "                updateStatus(\"数据刷新完成，共 \" + rowCount + \" 条记录\");\n" +
        "            }\n" +
        "        );\n" +
        "    }\n" +
        "    \n" +
        "    /**\n" +
        "     * 主方法\n" +
        "     */\n" +
        "    public static void main(String[] args) {\n" +
        "        // 确保在EDT中创建UI\n" +
        "        SwingUtilities.invokeLater(() -> {\n" +
        "            try {\n" +
        "                new MyFormModule();\n" +
        "            } catch (Exception e) {\n" +
        "                Logger logger = Logger.getLogger(MyFormModule.class.getName());\n" +
        "                logger.log(Level.SEVERE, \"启动应用程序时发生错误\", e);\n" +
        "                JOptionPane.showMessageDialog(\n" +
        "                    null,\n" +
        "                    \"启动应用程序时发生错误: \" + e.getMessage(),\n" +
        "                    \"错误\",\n" +
        "                    JOptionPane.ERROR_MESSAGE\n" +
        "                );\n" +
        "            }\n" +
        "        });\n" +
        "    }\n" +
        "}";
    
    /**
     * 主方法 - 打印示例代码
     */
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("      酒店管理系统 - 模块升级代码示例");
        System.out.println("=====================================================");
        
        System.out.println("\n一、基础模块模板");
        System.out.println("------------------------------------------------------");
        System.out.println(BASE_MODULE_TEMPLATE);
        
        System.out.println("\n二、表单模块模板");
        System.out.println("------------------------------------------------------");
        System.out.println(FORM_MODULE_TEMPLATE);
        
        System.out.println("\n=====================================================");
    }
} 