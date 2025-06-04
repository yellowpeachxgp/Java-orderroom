package com.all.search;

import com.database.helper.DatabaseHelper;
import com.hotel.ui.UnifiedUITheme;
import com.hotel.core.BookingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class checkOut extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(checkOut.class.getName());
    
    private JTable table;
    private JLabel statusLabel;
    private BookingManager bookingManager;

    public checkOut() {
        // 应用现代主题
        UnifiedUITheme.applyGlobalTheme();
        bookingManager = BookingManager.getInstance();
        
        // 设置窗口属性
        setTitle("退房管理");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UnifiedUITheme.BACKGROUND);
        
        // 创建顶部标题面板
        JPanel headerPanel = createHeaderPanel();
        
        // 创建内容面板
        JPanel contentPanel = createContentPanel();
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        // 创建状态面板
        JPanel statusPanel = createStatusPanel();
        
        // 组装界面
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 创建底部状态栏
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(UnifiedUITheme.BACKGROUND);
        bottomPanel.add(statusPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.PAGE_END);
        
        add(mainPanel);
        
        // 加载数据
        loadCheckedInBookings();
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UnifiedUITheme.PRIMARY);
        panel.setPreferredSize(new Dimension(0, 60));
        panel.setBorder(new EmptyBorder(UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD, 
                                       UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD));
        
        // 标题
        JLabel titleLabel = UnifiedUITheme.createLabel("退房管理", UnifiedUITheme.LabelStyle.TITLE);
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
    
    private JPanel createContentPanel() {
        JPanel cardPanel = UnifiedUITheme.createCard();
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBorder(new EmptyBorder(UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD, 
                                           UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD));
        
        // 内容标题
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel contentTitle = UnifiedUITheme.createLabel("已入住客户列表", UnifiedUITheme.LabelStyle.SUBTITLE);
        JLabel helpText = UnifiedUITheme.createLabel("选择一个客户记录，然后点击\"确认退房\"按钮", UnifiedUITheme.LabelStyle.CAPTION);
        
        titlePanel.add(contentTitle, BorderLayout.NORTH);
        titlePanel.add(helpText, BorderLayout.SOUTH);
        titlePanel.setBorder(new EmptyBorder(0, 0, UnifiedUITheme.SPACING_MD, 0));
        
        // 创建表格模型
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("预订编号");
        model.addColumn("客户编号");
        model.addColumn("客户姓名");
        model.addColumn("房间编号");
        model.addColumn("房间类型");
        model.addColumn("入住日期");
        model.addColumn("入住状态");
        
        // 创建表格
        table = new JTable(model);
        JScrollPane scrollPane = UnifiedUITheme.createModernTable(table);
        
        // 设置表格选择模式
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        cardPanel.add(titlePanel, BorderLayout.NORTH);
        cardPanel.add(scrollPane, BorderLayout.CENTER);
        
        return cardPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UnifiedUITheme.SPACING_MD, UnifiedUITheme.SPACING_MD));
        panel.setBackground(UnifiedUITheme.BACKGROUND);
        
        // 刷新按钮
        JButton refreshButton = UnifiedUITheme.createButton("刷新列表", UnifiedUITheme.ButtonStyle.SECONDARY);
        refreshButton.addActionListener(e -> loadCheckedInBookings());
        
        // 确认退房按钮
        JButton confirmButton = UnifiedUITheme.createButton("确认退房", UnifiedUITheme.ButtonStyle.WARNING);
        confirmButton.addActionListener(this::confirmCheckOut);
        
        panel.add(refreshButton);
        panel.add(confirmButton);
        
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
    
    private void confirmCheckOut(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("请选择一个客户记录");
            return;
        }
        
        String bookNumber = (String) table.getValueAt(selectedRow, 0);
        String customerName = (String) table.getValueAt(selectedRow, 2);
        String roomNumber = (String) table.getValueAt(selectedRow, 3);
        
        // 确认对话框
        int option = JOptionPane.showConfirmDialog(
            this,
            String.format("确认为客户 %s 办理房间 %s 的退房手续？", customerName, roomNumber),
            "确认退房",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        
        updateStatus("正在办理退房手续...");
        
        // 使用核心管理器处理退房
        SwingUtilities.invokeLater(() -> {
            try {
                BookingManager.BookingResult result = bookingManager.checkOut(bookNumber);
                
                if (result.isSuccess()) {
                    showSuccess("退房手续办理成功！\n客户：" + customerName + "\n房间：" + roomNumber);
                    loadCheckedInBookings(); // 刷新列表
                } else {
                    showError("退房办理失败：" + result.getErrorMessage());
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "办理退房时发生错误", ex);
                showError("办理退房时发生错误：" + ex.getMessage());
            } finally {
                updateStatus("就绪");
            }
        });
    }
    
    private void loadCheckedInBookings() {
        updateStatus("正在加载已入住客户...");
        
        SwingUtilities.invokeLater(() -> {
            try {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.setRowCount(0);
                
                // 获取所有预订并筛选已入住的
                var allBookings = bookingManager.getAllBookings();
                int checkedInCount = 0;
                
                for (BookingManager.Booking booking : allBookings) {
                    if ("已入住".equals(booking.getStatus())) {
                        model.addRow(new Object[]{
                            booking.getBookNumber(),
                            booking.getCustomerId(),
                            booking.getCustomerName(),
                            booking.getRoomNumber(),
                            booking.getRoomType(),
                            booking.getBookDate(),
                            booking.getStatus()
                        });
                        checkedInCount++;
                    }
                }
                
                updateStatus("加载完成，共 " + checkedInCount + " 个已入住客户");
                
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "加载已入住客户时发生错误", ex);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                checkOut frame = new checkOut();
                frame.setVisible(true);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "启动退房管理界面时发生错误", e);
                e.printStackTrace();
            }
        });
    }
}
