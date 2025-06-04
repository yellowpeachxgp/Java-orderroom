package com.all.search;

import com.database.helper.DatabaseHelper;
import com.hotel.ui.UnifiedUITheme;
import com.hotel.core.RoomManager;

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

public class add_room extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(add_room.class.getName());
    
    private JTable table;
    private JTextField rNumberField, rTypeField, rPriceField;
    private JLabel statusLabel;
    private RoomManager roomManager;

    public add_room() {
        // 应用现代主题
        UnifiedUITheme.applyGlobalTheme();
        roomManager = RoomManager.getInstance();
        
        // 设置窗口属性
        setTitle("添加房间");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UnifiedUITheme.BACKGROUND);
        
        // 创建顶部标题面板
        JPanel headerPanel = createHeaderPanel();
        
        // 创建表单面板
        JPanel formPanel = createFormPanel();
        
        // 创建表格面板
        JPanel tablePanel = createTablePanel();
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        // 创建状态面板
        JPanel statusPanel = createStatusPanel();
        
        // 组装界面
        JPanel contentPanel = new JPanel(new BorderLayout(0, UnifiedUITheme.SPACING_MD));
        contentPanel.setBackground(UnifiedUITheme.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD, 
                                              UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD));
        
        contentPanel.add(formPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // 初始化数据
        refreshPage();
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UnifiedUITheme.PRIMARY);
        panel.setPreferredSize(new Dimension(0, 60));
        panel.setBorder(new EmptyBorder(UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD, 
                                       UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD));
        
        // 标题
        JLabel titleLabel = UnifiedUITheme.createLabel("房间管理 - 添加房间", UnifiedUITheme.LabelStyle.TITLE);
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
    
    private JPanel createFormPanel() {
        JPanel cardPanel = UnifiedUITheme.createCard();
        cardPanel.setLayout(new BorderLayout());
        
        // 表单标题
        JLabel formTitle = UnifiedUITheme.createLabel("房间信息录入", UnifiedUITheme.LabelStyle.SUBTITLE);
        formTitle.setBorder(new EmptyBorder(0, 0, UnifiedUITheme.SPACING_MD, 0));
        
        // 表单内容
        JPanel formContent = new JPanel(new GridBagLayout());
        formContent.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UnifiedUITheme.SPACING_SM, UnifiedUITheme.SPACING_SM, 
                               UnifiedUITheme.SPACING_SM, UnifiedUITheme.SPACING_SM);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 房间编号
        gbc.gridx = 0; gbc.gridy = 0;
        formContent.add(UnifiedUITheme.createLabel("房间编号:", UnifiedUITheme.LabelStyle.BODY), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        rNumberField = UnifiedUITheme.createTextField("请输入房间编号，如：101、102");
        formContent.add(rNumberField, gbc);
        
        // 房间类型
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formContent.add(UnifiedUITheme.createLabel("房间类型:", UnifiedUITheme.LabelStyle.BODY), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        rTypeField = UnifiedUITheme.createTextField("如：标准间、豪华间、套房");
        formContent.add(rTypeField, gbc);
        
        // 房间价格
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formContent.add(UnifiedUITheme.createLabel("房间价格:", UnifiedUITheme.LabelStyle.BODY), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        rPriceField = UnifiedUITheme.createTextField("请输入价格，如：200");
        formContent.add(rPriceField, gbc);
        
        cardPanel.add(formTitle, BorderLayout.NORTH);
        cardPanel.add(formContent, BorderLayout.CENTER);
        
        return cardPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel cardPanel = UnifiedUITheme.createCard();
        cardPanel.setLayout(new BorderLayout());
        
        // 表格标题
        JLabel tableTitle = UnifiedUITheme.createLabel("房间列表", UnifiedUITheme.LabelStyle.SUBTITLE);
        tableTitle.setBorder(new EmptyBorder(0, 0, UnifiedUITheme.SPACING_MD, 0));
        
        // 创建表格模型
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("房间编号");
        model.addColumn("房间类型");
        model.addColumn("房间价格");
        model.addColumn("房间状态");
        
        // 创建表格
        table = new JTable(model);
        JScrollPane scrollPane = UnifiedUITheme.createModernTable(table);
        
        cardPanel.add(tableTitle, BorderLayout.NORTH);
        cardPanel.add(scrollPane, BorderLayout.CENTER);
        
        return cardPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UnifiedUITheme.SPACING_MD, 0));
        panel.setOpaque(false);
        
        // 添加房间按钮
        JButton addButton = UnifiedUITheme.createButton("添加房间", UnifiedUITheme.ButtonStyle.PRIMARY);
        addButton.addActionListener(this::addRoomAction);
        
        // 刷新按钮
        JButton refreshButton = UnifiedUITheme.createButton("刷新列表", UnifiedUITheme.ButtonStyle.SECONDARY);
        refreshButton.addActionListener(e -> refreshPage());
        
        // 清空按钮
        JButton clearButton = UnifiedUITheme.createButton("清空表单", UnifiedUITheme.ButtonStyle.OUTLINE);
        clearButton.addActionListener(e -> clearFields());
        
        panel.add(clearButton);
        panel.add(refreshButton);
        panel.add(addButton);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UnifiedUITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(UnifiedUITheme.SPACING_SM, UnifiedUITheme.SPACING_MD, 
                                       UnifiedUITheme.SPACING_SM, UnifiedUITheme.SPACING_MD));
        
        statusLabel = UnifiedUITheme.createLabel("就绪", UnifiedUITheme.LabelStyle.CAPTION);
        panel.add(statusLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private void addRoomAction(ActionEvent e) {
        String rNumber = rNumberField.getText().trim();
        String rType = rTypeField.getText().trim();
        String rPrice = rPriceField.getText().trim();
        
        // 验证输入
        if (rNumber.isEmpty() || rType.isEmpty() || rPrice.isEmpty()) {
            showError("请填写完整的房间信息");
            return;
        }
        
        // 验证价格格式
        try {
            Double.parseDouble(rPrice);
        } catch (NumberFormatException ex) {
            showError("请输入有效的价格格式");
            return;
        }
        
        updateStatus("正在添加房间...");
        
        // 使用核心管理器添加房间
        SwingUtilities.invokeLater(() -> {
            try {
                RoomManager.Room room = new RoomManager.Room(rNumber, rType, rPrice);
                RoomManager.OperationResult result = roomManager.addRoom(room);
                
                if (result.isSuccess()) {
                    showSuccess("房间添加成功！");
                    clearFields();
                    refreshPage();
                } else {
                    showError(result.getErrorMessage());
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "添加房间时发生错误", ex);
                showError("添加房间时发生错误: " + ex.getMessage());
            } finally {
                updateStatus("就绪");
            }
        });
    }
    
    private void refreshPage() {
        updateStatus("正在刷新数据...");
        
        SwingUtilities.invokeLater(() -> {
            try {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.setRowCount(0);
                
                var rooms = roomManager.getAllRooms();
                for (RoomManager.Room room : rooms) {
                    model.addRow(new Object[]{
                        room.getRNumber(),
                        room.getRType(),
                        room.getRPrice(),
                        room.getState() != null ? room.getState() : "可预订"
                    });
                }
                
                updateStatus("数据刷新完成，共 " + rooms.size() + " 个房间");
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "刷新数据时发生错误", ex);
                showError("刷新数据时发生错误: " + ex.getMessage());
                updateStatus("刷新失败");
            }
        });
    }
    
    private void clearFields() {
        rNumberField.setText("");
        rTypeField.setText("");
        rPriceField.setText("");
        updateStatus("表单已清空");
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                add_room frame = new add_room();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}