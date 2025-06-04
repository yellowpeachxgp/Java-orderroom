package com.hotel.ui;

/**
 * 模块升级指南
 * 提供如何使用新的UI框架升级现有模块的详细说明
 */
public class ModuleUpgradeGuide {
    
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("      酒店管理系统 - 模块升级指南 v1.0");
        System.out.println("=====================================================");
        
        System.out.println("\n一、升级概述");
        System.out.println("------------------------------------------------------");
        System.out.println("本升级框架旨在统一所有模块的UI风格、错误处理和线程管理，");
        System.out.println("提高系统整体稳定性和用户体验。所有模块都应遵循以下结构：");
        System.out.println("1. 继承BaseModuleFrame基础窗口类");
        System.out.println("2. 使用UIHelper工具类处理异常和线程");
        System.out.println("3. 采用ModernTheme提供的样式和组件");
        System.out.println("4. 遵循线程安全原则，不在EDT中执行耗时操作");
        
        System.out.println("\n二、升级步骤");
        System.out.println("------------------------------------------------------");
        System.out.println("第1步：修改类定义，继承BaseModuleFrame");
        System.out.println("第2步：更新构造函数，调用父类构造器");
        System.out.println("第3步：使用线程池执行所有数据库操作");
        System.out.println("第4步：实现cleanupResources()方法清理资源");
        System.out.println("第5步：使用updateStatus()方法更新状态栏");
        System.out.println("第6步：使用handleException()方法处理异常");
        
        System.out.println("\n三、代码示例");
        System.out.println("------------------------------------------------------");
        System.out.println("1. 类定义示例：");
        System.out.println("public class MyModule extends BaseModuleFrame {");
        System.out.println("    private JTextField nameField;");
        System.out.println("    private JTable dataTable;");
        System.out.println("    ");
        System.out.println("    public MyModule() {");
        System.out.println("        super(\"模块标题\", 800, 600);");
        System.out.println("        initializeComponents();");
        System.out.println("        loadData();");
        System.out.println("        showWithAnimation();");
        System.out.println("    }");
        System.out.println("    // ...");
        System.out.println("}");
        
        System.out.println("\n2. 数据库操作示例：");
        System.out.println("private void loadData() {");
        System.out.println("    updateStatus(\"正在加载数据...\");");
        System.out.println("    ");
        System.out.println("    // 创建加载对话框");
        System.out.println("    final JDialog loadingDialog = UIHelper.createLoadingDialog(this, \"正在加载数据...\");");
        System.out.println("    SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));");
        System.out.println("    ");
        System.out.println("    // 在线程池中执行数据库操作");
        System.out.println("    executorService.submit(() -> {");
        System.out.println("        Connection conn = null;");
        System.out.println("        PreparedStatement stmt = null;");
        System.out.println("        ResultSet rs = null;");
        System.out.println("        ");
        System.out.println("        try {");
        System.out.println("            // 数据库操作...");
        System.out.println("            ");
        System.out.println("            // 更新UI（在EDT中）");
        System.out.println("            SwingUtilities.invokeLater(() -> {");
        System.out.println("                loadingDialog.dispose();");
        System.out.println("                updateStatus(\"数据加载完成\");");
        System.out.println("            });");
        System.out.println("        } catch (Exception e) {");
        System.out.println("            handleException(\"加载数据失败\", e);");
        System.out.println("            SwingUtilities.invokeLater(() -> {");
        System.out.println("                loadingDialog.dispose();");
        System.out.println("                updateStatus(\"加载失败: \" + e.getMessage());");
        System.out.println("            });");
        System.out.println("        } finally {");
        System.out.println("            UIHelper.safeCloseResources(conn, stmt, rs);");
        System.out.println("        }");
        System.out.println("    });");
        System.out.println("}");
        
        System.out.println("\n3. 资源清理示例：");
        System.out.println("@Override");
        System.out.println("protected void cleanupResources() {");
        System.out.println("    // 关闭自定义资源");
        System.out.println("    // ...");
        System.out.println("    ");
        System.out.println("    // 调用父类方法关闭线程池");
        System.out.println("    super.cleanupResources();");
        System.out.println("}");
        
        System.out.println("\n四、UI样式规范");
        System.out.println("------------------------------------------------------");
        System.out.println("1. 颜色：使用ModernTheme中定义的颜色常量");
        System.out.println("   - ModernTheme.PRIMARY_COLOR（主色调）");
        System.out.println("   - ModernTheme.ACCENT_COLOR（强调色）");
        System.out.println("   - ModernTheme.ERROR_COLOR（错误色）");
        System.out.println("   - ModernTheme.BACKGROUND_COLOR（背景色）");
        System.out.println("   - ModernTheme.CARD_COLOR（卡片背景色）");
        System.out.println("");
        System.out.println("2. 字体：使用ModernTheme中定义的字体常量");
        System.out.println("   - ModernTheme.HEADER_FONT（标题字体）");
        System.out.println("   - ModernTheme.REGULAR_FONT（正常字体）");
        System.out.println("   - ModernTheme.SMALL_FONT（小字体）");
        System.out.println("");
        System.out.println("3. 组件：使用ModernTheme和CommonUITemplate提供的组件方法");
        System.out.println("   - ModernTheme.createRoundedButton()");
        System.out.println("   - ModernTheme.createModernTextField()");
        System.out.println("   - ModernTheme.createLabel()");
        System.out.println("   - CommonUITemplate.createContentPanel()");
        
        System.out.println("\n五、常见问题");
        System.out.println("------------------------------------------------------");
        System.out.println("Q: 如何处理特别复杂的UI布局？");
        System.out.println("A: 可以在contentPanel中使用任意布局管理器，推荐使用GridBagLayout或MigLayout。");
        System.out.println("");
        System.out.println("Q: 如何确保线程安全？");
        System.out.println("A: 1) 所有UI操作必须在EDT中执行；");
        System.out.println("   2) 所有耗时操作（如数据库查询）必须在线程池中执行；");
        System.out.println("   3) 使用SwingUtilities.invokeLater()将结果更新回UI。");
        System.out.println("");
        System.out.println("Q: 如何处理不同类型的表单？");
        System.out.println("A: 对于不同类型的表单，可以创建专门的子类继承BaseModuleFrame，");
        System.out.println("   如BaseFormModule或BaseTableModule，提供更具体的功能。");
        
        System.out.println("\n六、参考示例");
        System.out.println("------------------------------------------------------");
        System.out.println("参考升级后的模块实现：");
        System.out.println("1. add_client.java - 表单类模块示例");
        System.out.println("2. allRoom.java - 统计类模块示例");
        System.out.println("3. delete_client.java - 删除操作类模块示例");
        
        System.out.println("\n=====================================================");
        System.out.println("如有问题，请联系系统架构师或开发团队负责人");
        System.out.println("=====================================================");
    }
} 