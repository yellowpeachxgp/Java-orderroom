# 界面现代化改造模板

## 🎯 目标

本模板用于快速将传统Swing界面改造为现代扁平化风格，确保系统界面的统一性。

## 📋 待现代化界面清单

### 高优先级（核心功能界面）
- [ ] `alter_room.java` - 房间信息修改
- [ ] `alter_client.java` - 客户信息修改  
- [ ] `alter_leader.java` - 管理员信息修改
- [ ] `delete_room.java` - 房间删除
- [ ] `delete_client.java` - 客户删除
- [ ] `delete_leader.java` - 管理员删除
- [ ] `add_leader.java` - 添加管理员
- [ ] `changeRoom.java` - 房间变更

### 中优先级（辅助功能界面）
- [ ] `admin.java` - 管理员面板
- [ ] `SimpleRoomBooking.java` - 简化预订界面
- [ ] Dialog类 - 各种对话框

## 🔧 标准改造步骤

### 1. 导入现代主题
```java
// 添加导入
import com.hotel.ui.UnifiedUITheme;
import com.hotel.core.*; // 相关的核心管理器
import javax.swing.border.EmptyBorder;
import java.util.logging.Level;
import java.util.logging.Logger;

// 在构造函数开头添加
UnifiedUITheme.applyGlobalTheme();
```

### 2. 标准窗口结构
```java
public class ClassName extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(ClassName.class.getName());
    
    private JTable table; // 如有表格
    private JLabel statusLabel;
    private ManagerClass manager; // 对应的核心管理器

    public ClassName() {
        // 应用现代主题
        UnifiedUITheme.applyGlobalTheme();
        manager = ManagerClass.getInstance();
        
        // 设置窗口属性
        setTitle("界面标题");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UnifiedUITheme.BACKGROUND);
        
        // 创建各个面板
        JPanel headerPanel = createHeaderPanel();
        JPanel contentPanel = createContentPanel();
        JPanel buttonPanel = createButtonPanel();
        JPanel statusPanel = createStatusPanel();
        
        // 组装界面
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 底部状态栏
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(UnifiedUITheme.BACKGROUND);
        bottomPanel.add(statusPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.PAGE_END);
        
        add(mainPanel);
        
        // 初始化数据
        loadData();
    }
}
```

### 3. 创建标题栏
```java
private JPanel createHeaderPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(UnifiedUITheme.PRIMARY);
    panel.setPreferredSize(new Dimension(0, 60));
    panel.setBorder(new EmptyBorder(UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD, 
                                   UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD));
    
    // 标题
    JLabel titleLabel = UnifiedUITheme.createLabel("界面标题", UnifiedUITheme.LabelStyle.TITLE);
    titleLabel.setForeground(Color.WHITE);
    
    // 关闭按钮
    JButton closeButton = new JButton("×");
    closeButton.setFont(new Font("Arial", Font.BOLD, 20));
    closeButton.setForeground(Color.WHITE);
    closeButton.setContentAreaFilled(false);
    closeButton.setBorderPainted(false);
    closeButton.setFocusPainted(false);
    closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    closeButton.addActionListener(e -> dispose());
    
    panel.add(titleLabel, BorderLayout.WEST);
    panel.add(closeButton, BorderLayout.EAST);
    
    return panel;
}
```

### 4. 创建内容区域
```java
private JPanel createContentPanel() {
    JPanel cardPanel = UnifiedUITheme.createCard();
    cardPanel.setLayout(new BorderLayout());
    cardPanel.setBorder(new EmptyBorder(UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD, 
                                       UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD));
    
    // 内容标题
    JPanel titlePanel = new JPanel(new BorderLayout());
    titlePanel.setOpaque(false);
    
    JLabel contentTitle = UnifiedUITheme.createLabel("内容标题", UnifiedUITheme.LabelStyle.SUBTITLE);
    JLabel helpText = UnifiedUITheme.createLabel("帮助说明文字", UnifiedUITheme.LabelStyle.CAPTION);
    
    titlePanel.add(contentTitle, BorderLayout.NORTH);
    titlePanel.add(helpText, BorderLayout.SOUTH);
    titlePanel.setBorder(new EmptyBorder(0, 0, UnifiedUITheme.SPACING_MD, 0));
    
    // 根据需要添加表格或表单
    // 表格示例:
    DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    // 添加列...
    
    JTable table = new JTable(model);
    JScrollPane scrollPane = UnifiedUITheme.createModernTable(table);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    cardPanel.add(titlePanel, BorderLayout.NORTH);
    cardPanel.add(scrollPane, BorderLayout.CENTER);
    
    return cardPanel;
}
```

### 5. 创建按钮区域
```java
private JPanel createButtonPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD));
    panel.setBackground(UnifiedUITheme.BACKGROUND);
    
    // 根据功能添加不同按钮
    JButton refreshButton = UnifiedUITheme.createButton("刷新", UnifiedUITheme.ButtonStyle.SECONDARY);
    refreshButton.addActionListener(e -> loadData());
    
    JButton actionButton = UnifiedUITheme.createButton("主操作", UnifiedUITheme.ButtonStyle.PRIMARY);
    actionButton.addActionListener(this::performAction);
    
    panel.add(refreshButton);
    panel.add(actionButton);
    
    return panel;
}
```

### 6. 创建状态栏
```java
private JPanel createStatusPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(UnifiedUITheme.BACKGROUND);
    panel.setBorder(new EmptyBorder(UnifiedUITheme.SPACING_SM, UnifiedUITheme.SPACING_MD, 
                                   UnifiedUITheme.SPACING_SM, UnifiedUITheme.SPACING_MD));
    
    statusLabel = UnifiedUITheme.createLabel("就绪", UnifiedUITheme.LabelStyle.CAPTION);
    panel.add(statusLabel, BorderLayout.WEST);
    
    return panel;
}
```

### 7. 业务逻辑方法
```java
private void performAction(ActionEvent e) {
    // 获取选中项
    int selectedRow = table.getSelectedRow();
    if (selectedRow == -1) {
        showError("请选择一项");
        return;
    }
    
    // 确认操作
    int option = JOptionPane.showConfirmDialog(
        this,
        "确认执行此操作？",
        "确认",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );
    
    if (option != JOptionPane.YES_OPTION) {
        return;
    }
    
    updateStatus("正在处理...");
    
    // 使用核心管理器处理业务逻辑
    SwingUtilities.invokeLater(() -> {
        try {
            // 调用管理器方法
            ManagerClass.Result result = manager.performOperation(params);
            
            if (result.isSuccess()) {
                showSuccess("操作成功！");
                loadData(); // 刷新数据
            } else {
                showError("操作失败：" + result.getErrorMessage());
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "操作时发生错误", ex);
            showError("操作时发生错误：" + ex.getMessage());
        } finally {
            updateStatus("就绪");
        }
    });
}

private void loadData() {
    updateStatus("正在加载数据...");
    
    SwingUtilities.invokeLater(() -> {
        try {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            
            // 获取数据
            var dataList = manager.getAllData();
            for (DataType data : dataList) {
                model.addRow(new Object[]{
                    data.getField1(),
                    data.getField2(),
                    // ...
                });
            }
            
            updateStatus("加载完成，共 " + dataList.size() + " 条记录");
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "加载数据时发生错误", ex);
            showError("加载数据时发生错误：" + ex.getMessage());
            updateStatus("加载失败");
        }
    });
}

private void updateStatus(String message) {
    if (statusLabel != null) {
        statusLabel.setText(message);
    }
}

private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
}

private void showSuccess(String message) {
    JOptionPane.showMessageDialog(this, message, "成功", JOptionPane.INFORMATION_MESSAGE);
}
```

## 🎨 按钮样式指南

根据功能选择合适的按钮样式：

```java
// 主要操作（添加、确认、保存）
UnifiedUITheme.ButtonStyle.PRIMARY

// 次要操作（刷新、取消）
UnifiedUITheme.ButtonStyle.SECONDARY

// 成功操作（入住、完成）
UnifiedUITheme.ButtonStyle.SUCCESS

// 警告操作（退房、修改）
UnifiedUITheme.ButtonStyle.WARNING

// 危险操作（删除）
UnifiedUITheme.ButtonStyle.ERROR

// 边框样式（清空、重置）
UnifiedUITheme.ButtonStyle.OUTLINE
```

## 📱 表单创建指南

对于包含表单的界面：

```java
private JPanel createFormPanel() {
    JPanel cardPanel = UnifiedUITheme.createCard();
    cardPanel.setLayout(new BorderLayout());
    
    // 表单标题
    JLabel formTitle = UnifiedUITheme.createLabel("表单标题", UnifiedUITheme.LabelStyle.SUBTITLE);
    formTitle.setBorder(new EmptyBorder(0, 0, UnifiedUITheme.SPACING_MD, 0));
    
    // 表单内容
    JPanel formContent = new JPanel(new GridBagLayout());
    formContent.setOpaque(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(UnifiedUITheme.SPACING_SM, UnifiedUITheme.SPACING_SM, 
                           UnifiedUITheme.SPACING_SM, UnifiedUITheme.SPACING_SM);
    gbc.anchor = GridBagConstraints.WEST;
    
    // 添加表单字段
    gbc.gridx = 0; gbc.gridy = 0;
    formContent.add(UnifiedUITheme.createLabel("字段名:", UnifiedUITheme.LabelStyle.BODY), gbc);
    
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JTextField field = UnifiedUITheme.createTextField("占位符文本");
    formContent.add(field, gbc);
    
    cardPanel.add(formTitle, BorderLayout.NORTH);
    cardPanel.add(formContent, BorderLayout.CENTER);
    
    return cardPanel;
}
```

## ✅ 检查清单

改造完成后，确保：

- [ ] 导入UnifiedUITheme并应用全局主题
- [ ] 使用统一的窗口大小 (900x650)
- [ ] 标题栏使用PRIMARY颜色背景
- [ ] 内容区域使用卡片式布局
- [ ] 按钮使用适当的样式枚举
- [ ] 表格使用createModernTable方法
- [ ] 包含状态栏提示
- [ ] 集成对应的核心管理器
- [ ] 添加适当的错误处理
- [ ] 日志记录器配置
- [ ] main方法使用SwingUtilities.invokeLater

## 🚀 快速开始

1. 复制标准窗口结构代码
2. 根据界面功能调整业务逻辑
3. 选择合适的按钮样式
4. 集成对应的核心管理器
5. 测试界面功能和样式

---

**注意**: 所有现代化界面都应该遵循这个统一的模板结构，确保系统界面的一致性和用户体验的连贯性。 