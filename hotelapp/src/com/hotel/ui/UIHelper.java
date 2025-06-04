package com.hotel.ui;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 统一UI助手类
 * 提供所有模块通用的UI组件、错误处理和线程管理功能
 */
public class UIHelper {
    
    private static final Logger LOGGER = Logger.getLogger(UIHelper.class.getName());
    
    /**
     * 创建标准单线程池
     */
    public static ExecutorService createExecutorService() {
        return Executors.newSingleThreadExecutor();
    }
    
    /**
     * 处理异常并显示错误对话框
     * @param owner 对话框的父窗口
     * @param message 错误消息
     * @param e 异常对象
     */
    public static void handleException(Component owner, String message, Exception e) {
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
                owner,
                new Object[]{message + ": " + e.getMessage(), scrollPane},
                "错误",
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
    
    /**
     * 在单独线程中执行操作，确保不阻塞EDT
     * @param executorService 线程池
     * @param beforeTask 任务执行前的UI操作
     * @param task 要执行的任务
     * @param afterTask 任务执行后的UI操作
     * @param errorHandler 错误处理器
     * @param <T> 任务返回类型
     */
    public static <T> void executeTask(
            ExecutorService executorService,
            Runnable beforeTask,
            Supplier<T> task,
            Consumer<T> afterTask,
            BiConsumer<String, Exception> errorHandler) {
        
        // 在EDT线程中执行前置操作
        SwingUtilities.invokeLater(beforeTask);
        
        // 在线程池中执行主任务
        executorService.submit(() -> {
            try {
                // 执行任务
                T result = task.get();
                
                // 在EDT中执行后置操作
                SwingUtilities.invokeLater(() -> afterTask.accept(result));
                
            } catch (Exception e) {
                // 在EDT中处理错误
                SwingUtilities.invokeLater(() -> errorHandler.accept("执行任务时发生错误", e));
            }
        });
    }
    
    /**
     * 安全关闭数据库资源并记录可能的错误
     * @param connection 数据库连接
     * @param statement SQL语句
     * @param resultSet 结果集
     */
    public static void safeCloseResources(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "关闭数据库资源时发生错误", e);
        }
    }
    
    /**
     * 创建标准状态栏
     * @return 状态栏面板
     */
    public static JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        JLabel statusLabel = new JLabel("就绪");
        statusLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        statusLabel.setBorder(new javax.swing.border.EmptyBorder(5, 0, 0, 0));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        return statusPanel;
    }
    
    /**
     * 更新状态栏文本
     * @param statusLabel 状态标签
     * @param message 状态消息
     */
    public static void updateStatus(JLabel statusLabel, String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            LOGGER.info(message);
        });
    }
    
    /**
     * 创建标准加载对话框
     * @param owner 对话框的父窗口
     * @param message 显示消息
     * @return 配置好的对话框
     */
    public static JDialog createLoadingDialog(JFrame owner, String message) {
        JDialog dialog = new JDialog(owner, "处理中", true);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(owner);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ModernTheme.CARD_COLOR);
                g2d.fill(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.setColor(new Color(230, 230, 230));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new javax.swing.border.EmptyBorder(20, 20, 20, 20));
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(ModernTheme.REGULAR_FONT);
        messageLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(messageLabel, BorderLayout.CENTER);
        dialog.add(panel);
        
        return dialog;
    }
    
    /**
     * 双参数消费者接口
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     */
    @FunctionalInterface
    public interface BiConsumer<T, U> {
        void accept(T t, U u);
    }
} 