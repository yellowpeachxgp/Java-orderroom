package com.hotel.ui;

/**
 * 酒店管理系统界面升级指南
 * 本文件提供了如何使用现代UI组件优化系统中的二级界面的指导
 */
public class UIUpgradeGuide {
    
    /**
     * UI优化步骤指南
     */
    public static void main(String[] args) {
        System.out.println("==== 酒店管理系统界面升级指南 ====");
        System.out.println("本指南提供了如何使用ModernTheme和CommonUITemplate优化界面的步骤");
        
        System.out.println("\n=== 步骤1: 导入所需的UI包 ===");
        System.out.println("import com.hotel.ui.ModernTheme;");
        System.out.println("import com.hotel.ui.AnimationManager;");
        System.out.println("import com.hotel.ui.CommonUITemplate;");
        System.out.println("import com.hotel.ui.ModernTableRenderer;");
        
        System.out.println("\n=== 步骤2: 应用现代窗口结构 ===");
        System.out.println("1. 创建基本窗口");
        System.out.println("2. 创建主面板");
        System.out.println("3. 创建顶部标题面板");
        System.out.println("4. 创建内容面板");
        System.out.println("5. 创建按钮面板");
        System.out.println("6. 组装主面板");
        System.out.println("7. 应用淡入动画");
        
        System.out.println("\n=== 模板代码示例 ===");
        System.out.println("// 应用现代主题");
        System.out.println("ModernTheme.applyTheme();");
        System.out.println();
        System.out.println("// 创建主面板");
        System.out.println("mainPanel = CommonUITemplate.createMainPanel();");
        System.out.println();
        System.out.println("// 创建顶部标题面板");
        System.out.println("JPanel headerPanel = CommonUITemplate.createHeaderPanel(\"标题文本\", e -> dispose());");
        System.out.println();
        System.out.println("// 创建内容面板");
        System.out.println("JPanel contentPanel = CommonUITemplate.createContentPanel();");
        System.out.println();
        System.out.println("// 创建按钮面板");
        System.out.println("JPanel buttonPanel = CommonUITemplate.createButtonPanel();");
        System.out.println("JButton actionButton = CommonUITemplate.createActionButton(\"按钮文本\", ModernTheme.PRIMARY_COLOR, e -> {});");
        System.out.println("buttonPanel.add(actionButton);");
        System.out.println();
        System.out.println("// 组装主面板");
        System.out.println("mainPanel.add(headerPanel, BorderLayout.NORTH);");
        System.out.println("mainPanel.add(contentPanel, BorderLayout.CENTER);");
        System.out.println("mainPanel.add(buttonPanel, BorderLayout.SOUTH);");
        System.out.println();
        System.out.println("// 添加到窗口");
        System.out.println("add(mainPanel);");
        System.out.println();
        System.out.println("// 应用淡入动画");
        System.out.println("CommonUITemplate.applyFadeInAnimation(mainPanel);");
        
        System.out.println("\n=== 步骤3: 针对不同类型界面的特殊处理 ===");
        System.out.println("1. 表单类界面: 使用GridBagLayout和CommonUITemplate.createFormConstraints()");
        System.out.println("2. 表格类界面: 使用ModernTableRenderer.applyModernStyle()美化表格");
        System.out.println("3. 统计类界面: 创建卡片式统计面板和简单图表");
        
        System.out.println("\n=== 颜色常量 ===");
        System.out.println("ModernTheme.PRIMARY_COLOR - 主要颜色(蓝色)");
        System.out.println("ModernTheme.PRIMARY_DARK_COLOR - 深色主题(深蓝色)");
        System.out.println("ModernTheme.ACCENT_COLOR - 强调色(橙色)");
        System.out.println("ModernTheme.ERROR_COLOR - 错误色(红色)");
        System.out.println("ModernTheme.BACKGROUND_COLOR - 背景色(浅灰色)");
        System.out.println("ModernTheme.CARD_COLOR - 卡片背景色(白色)");
        
        System.out.println("\n=== 字体常量 ===");
        System.out.println("ModernTheme.HEADER_FONT - 标题字体");
        System.out.println("ModernTheme.REGULAR_FONT - 正常字体");
        System.out.println("ModernTheme.SMALL_FONT - 小字体");
        
        System.out.println("\n=== 实用工具方法 ===");
        System.out.println("1. ModernTheme.createLabel() - 创建现代风格标签");
        System.out.println("2. ModernTheme.createModernTextField() - 创建现代风格输入框");
        System.out.println("3. ModernTheme.createRoundedButton() - 创建圆角按钮");
        System.out.println("4. ModernTheme.createTitleLabel() - 创建标题标签");
        System.out.println("5. CommonUITemplate.createLoadingDialog() - 创建加载对话框");
        
        System.out.println("\n=== 代码示例 ===");
        System.out.println("请参考以下文件作为参考示例:");
        System.out.println("1. allRoom.java - 统计类界面示例");
        System.out.println("2. add_client.java - 表单类界面示例");
        System.out.println("3. MainMenu.java - 导航类界面示例");
    }
    
    /**
     * 参考模板：表单类界面示例代码
     */
    public static String getFormTemplateCode() {
        return "// 创建表单面板\n" +
               "JPanel formPanel = new JPanel() {\n" +
               "    @Override\n" +
               "    protected void paintComponent(Graphics g) {\n" +
               "        Graphics2D g2d = (Graphics2D) g.create();\n" +
               "        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);\n" +
               "        g2d.setColor(ModernTheme.CARD_COLOR);\n" +
               "        g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));\n" +
               "        g2d.dispose();\n" +
               "        super.paintComponent(g);\n" +
               "    }\n" +
               "};\n" +
               "formPanel.setLayout(new GridBagLayout());\n" +
               "formPanel.setOpaque(false);\n" +
               "formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));\n\n" +
               
               "// 创建表单标题\n" +
               "JLabel formTitle = new JLabel(\"表单标题\");\n" +
               "formTitle.setFont(ModernTheme.HEADER_FONT);\n" +
               "formTitle.setForeground(ModernTheme.PRIMARY_COLOR);\n" +
               "GridBagConstraints c = CommonUITemplate.createFormConstraints(0, 0, 2, 1.0);\n" +
               "c.anchor = GridBagConstraints.WEST;\n" +
               "formPanel.add(formTitle, c);\n\n" +
               
               "// 创建输入字段\n" +
               "JTextField textField = ModernTheme.createModernTextField(15);\n\n" +
               
               "// 添加标签和输入字段\n" +
               "c = CommonUITemplate.createFormConstraints(0, 1, 1, 0.0);\n" +
               "c.anchor = GridBagConstraints.EAST;\n" +
               "formPanel.add(ModernTheme.createLabel(\"字段名称:\"), c);\n\n" +
               
               "c = CommonUITemplate.createFormConstraints(1, 1, 1, 1.0);\n" +
               "formPanel.add(textField, c);";
    }
    
    /**
     * 参考模板：表格类界面示例代码
     */
    public static String getTableTemplateCode() {
        return "// 创建表格模型\n" +
               "DefaultTableModel model = new DefaultTableModel() {\n" +
               "    @Override\n" +
               "    public boolean isCellEditable(int row, int column) {\n" +
               "        return false; // 设置表格不可编辑\n" +
               "    }\n" +
               "};\n" +
               "model.addColumn(\"列标题1\");\n" +
               "model.addColumn(\"列标题2\");\n\n" +
               
               "// 创建表格\n" +
               "JTable table = new JTable(model);\n" +
               "ModernTableRenderer.applyModernStyle(table);\n\n" +
               
               "// 创建表格面板\n" +
               "JPanel tablePanel = new JPanel(new BorderLayout());\n" +
               "tablePanel.setOpaque(false);\n" +
               "tablePanel.setBorder(new EmptyBorder(20, 0, 0, 0));\n\n" +
               
               "// 添加表格标题\n" +
               "JLabel tableTitle = new JLabel(\"表格标题\");\n" +
               "tableTitle.setFont(ModernTheme.HEADER_FONT);\n" +
               "tableTitle.setForeground(ModernTheme.PRIMARY_COLOR);\n" +
               "tableTitle.setBorder(new EmptyBorder(0, 0, 10, 0));\n\n" +
               
               "JScrollPane scrollPane = new JScrollPane(table);\n" +
               "scrollPane.setBorder(BorderFactory.createEmptyBorder());\n" +
               "scrollPane.getViewport().setBackground(ModernTheme.BACKGROUND_COLOR);\n\n" +
               
               "tablePanel.add(tableTitle, BorderLayout.NORTH);\n" +
               "tablePanel.add(scrollPane, BorderLayout.CENTER);";
    }
    
    /**
     * 参考模板：加载动画示例代码
     */
    public static String getLoadingTemplateCode() {
        return "// 禁用按钮防止重复提交\n" +
               "actionButton.setEnabled(false);\n\n" +
               
               "// 创建加载对话框\n" +
               "JDialog loadingDialog = CommonUITemplate.createLoadingDialog(this, \"正在处理...\");\n" +
               "loadingDialog.setVisible(true);\n\n" +
               
               "// 在新线程中执行操作\n" +
               "new Thread(() -> {\n" +
               "    try {\n" +
               "        Thread.sleep(500); // 模拟处理延迟\n" +
               "        \n" +
               "        // 执行实际操作\n" +
               "        boolean success = performOperation();\n" +
               "        \n" +
               "        // 在EDT中更新UI\n" +
               "        SwingUtilities.invokeLater(() -> {\n" +
               "            loadingDialog.dispose();\n" +
               "            \n" +
               "            if (success) {\n" +
               "                JOptionPane.showMessageDialog(this, \"操作成功！\", \"成功\", JOptionPane.INFORMATION_MESSAGE);\n" +
               "            } else {\n" +
               "                JOptionPane.showMessageDialog(this, \"操作失败\", \"错误\", JOptionPane.ERROR_MESSAGE);\n" +
               "            }\n" +
               "            \n" +
               "            // 恢复按钮状态\n" +
               "            actionButton.setEnabled(true);\n" +
               "        });\n" +
               "        \n" +
               "    } catch (Exception e) {\n" +
               "        e.printStackTrace();\n" +
               "        SwingUtilities.invokeLater(() -> {\n" +
               "            loadingDialog.dispose();\n" +
               "            JOptionPane.showMessageDialog(this, \"操作失败: \" + e.getMessage(), \"错误\", JOptionPane.ERROR_MESSAGE);\n" +
               "            actionButton.setEnabled(true);\n" +
               "        });\n" +
               "    }\n" +
               "}).start();";
    }
} 