package com.all.search;

import com.database.helper.DatabaseHelper;
import com.hotel.ui.AnimationManager;
import com.hotel.ui.CommonUITemplate;
import com.hotel.ui.ModernTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class allRoom extends JFrame {
    // 静态常量
    private static final Logger LOGGER = Logger.getLogger(allRoom.class.getName());
    private static final int DB_TIMEOUT_SECONDS = 3;
    private static final int REFRESH_TIMEOUT_SECONDS = 10;
    
    // UI组件
    private JPanel mainPanel;
    private JPanel statsPanel;
    private JPanel chartPanel;
    private JButton refreshButton;
    
    // 线程安全控制
    private final AtomicBoolean isRefreshInProgress = new AtomicBoolean(false);
    private volatile boolean isWindowClosing = false;

    public allRoom() {
        // 使用通用模板创建基本窗口
        setTitle("房间信息统计");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 添加窗口关闭监听器
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                isWindowClosing = true;
                LOGGER.info("房间统计窗口正在关闭");
            }
        });
        
        // 应用现代主题
        ModernTheme.applyTheme();
        
        initializeUI();
        loadInitialData();
        
        // 显示窗口
        setVisible(true);
        
        // 应用淡入动画
        AnimationManager.fadeIn(mainPanel, 500);
    }
    
    /**
     * 初始化UI界面
     */
    private void initializeUI() {
        // 创建主面板
        mainPanel = CommonUITemplate.createMainPanel();
        
        // 创建顶部标题面板
        JPanel headerPanel = CommonUITemplate.createHeaderPanel("房间信息统计", e -> dispose());
        
        // 创建内容面板
        JPanel contentPanel = CommonUITemplate.createContentPanel();
        
        // 创建统计信息面板
        statsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ModernTheme.BACKGROUND_COLOR);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        statsPanel.setLayout(new GridLayout(2, 2, 20, 20));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 初始化空的统计卡片
        updateStatsPanel(new RoomStats());
        
        // 创建图表面板
        chartPanel = createChartPanel(new RoomStats());
        
        // 添加到内容面板
        contentPanel.add(new JLabel("房间状态统计", SwingConstants.CENTER), BorderLayout.NORTH);
        contentPanel.add(statsPanel, BorderLayout.CENTER);
        contentPanel.add(chartPanel, BorderLayout.SOUTH);
        
        // 创建按钮面板
        JPanel buttonPanel = CommonUITemplate.createButtonPanel();
        refreshButton = CommonUITemplate.createActionButton("刷新数据", ModernTheme.PRIMARY_COLOR, 
            e -> refreshDataAsync());
        buttonPanel.add(refreshButton);
        
        // 组装主面板
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 添加到窗口
        add(mainPanel);
    }
    
    /**
     * 加载初始数据
     */
    private void loadInitialData() {
        refreshDataAsync();
    }
    
    /**
     * 异步刷新数据
     */
    private void refreshDataAsync() {
        // 防止重复点击
        if (!isRefreshInProgress.compareAndSet(false, true)) {
            LOGGER.info("刷新操作正在进行中，忽略重复点击");
            return;
        }
        
        if (isWindowClosing) {
            LOGGER.info("窗口正在关闭，取消刷新操作");
            isRefreshInProgress.set(false);
            return;
        }
        
        LOGGER.info("开始刷新房间统计数据");
        
        // 禁用刷新按钮
        refreshButton.setEnabled(false);
        refreshButton.setText("刷新中...");
        
        // 创建进度指示器
        JDialog loadingDialog = createLoadingDialog("正在刷新数据...");
        
        // 使用CompletableFuture异步处理
        CompletableFuture<RoomStats> refreshFuture = CompletableFuture.supplyAsync(() -> {
            try {
                if (isWindowClosing) {
                    LOGGER.info("窗口正在关闭，取消数据查询");
                    return null;
                }
                
                // 模拟短暂延迟，让用户看到加载提示
                Thread.sleep(200);
                
                LOGGER.info("开始从数据库获取房间统计数据");
                RoomStats stats = getRoomStatsFromDatabase();
                LOGGER.info("房间统计数据获取完成");
                
                return stats;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "获取房间统计数据时出错", e);
                throw new RuntimeException(e);
            }
        });
        
        // 显示对话框
        SwingUtilities.invokeLater(() -> {
            if (!isWindowClosing) {
                loadingDialog.setVisible(true);
            }
        });
        
        // 处理刷新结果
        refreshFuture.orTimeout(REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .whenComplete((stats, throwable) -> {
                SwingUtilities.invokeLater(() -> {
                    try {
                        // 检查窗口是否已关闭
                        if (isWindowClosing) {
                            LOGGER.info("窗口已关闭，跳过UI更新");
                            return;
                        }
                        
                        // 关闭进度对话框
                        if (loadingDialog.isDisplayable()) {
                            loadingDialog.dispose();
                        }
                        
                        if (throwable != null) {
                            // 处理异常
                            String errorMessage;
                            if (throwable instanceof java.util.concurrent.TimeoutException) {
                                errorMessage = "刷新操作超时，请稍后重试";
                                LOGGER.warning("刷新操作超时");
                            } else {
                                errorMessage = "刷新失败: " + throwable.getMessage();
                                LOGGER.log(Level.SEVERE, "刷新操作失败", throwable);
                            }
                            
                            JOptionPane.showMessageDialog(
                                allRoom.this, 
                                errorMessage, 
                                "错误", 
                                JOptionPane.ERROR_MESSAGE
                            );
                        } else if (stats != null) {
                            // 更新成功
                            updateUIWithNewData(stats);
                            
                            JOptionPane.showMessageDialog(
                                allRoom.this, 
                                "数据刷新成功！", 
                                "成功", 
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            // 数据为空（可能是窗口已关闭）
                            LOGGER.info("未获取到数据，可能是窗口已关闭");
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "更新UI时发生错误", ex);
                    } finally {
                        // 重置状态并重新启用按钮
                        refreshButton.setEnabled(true);
                        refreshButton.setText("刷新数据");
                        isRefreshInProgress.set(false);
                    }
                });
            });
    }
    
    /**
     * 使用新数据更新UI
     */
    private void updateUIWithNewData(RoomStats stats) {
        if (isWindowClosing) return;
        
        // 更新统计面板
        updateStatsPanel(stats);
        
        // 更新图表面板
        updateChartPanel(stats);
        
        // 刷新界面
        mainPanel.revalidate();
        mainPanel.repaint();
        
        LOGGER.info("UI更新完成");
    }
    
    /**
     * 更新统计面板
     */
    private void updateStatsPanel(RoomStats stats) {
        statsPanel.removeAll();
        statsPanel.add(createStatsCard("房间总数", stats.totalRooms, ModernTheme.PRIMARY_COLOR));
        statsPanel.add(createStatsCard("已入住房间", stats.occupiedRooms, new Color(46, 204, 113)));
        statsPanel.add(createStatsCard("已预订房间", stats.bookedRooms, ModernTheme.ACCENT_COLOR));
        statsPanel.add(createStatsCard("已退房房间", stats.checkedOutRooms, new Color(231, 76, 60)));
    }
    
    /**
     * 更新图表面板
     */
    private void updateChartPanel(RoomStats stats) {
        Container parent = chartPanel.getParent();
        if (parent != null) {
            parent.remove(chartPanel);
            chartPanel = createChartPanel(stats);
            parent.add(chartPanel, BorderLayout.SOUTH);
        }
    }
    
    /**
     * 创建统计卡片
     */
    private JPanel createStatsCard(String title, int value, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制背景
                g2d.setColor(ModernTheme.CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                
                // 绘制顶部颜色条
                g2d.setColor(color);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), 5, 0, 0));
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // 数值
        JLabel valueLabel = new JLabel(String.valueOf(value), SwingConstants.CENTER);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        valueLabel.setForeground(color);
        
        // 标题
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(ModernTheme.REGULAR_FONT);
        titleLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        
        return card;
    }

    /**
     * 创建图表面板
     */
    private JPanel createChartPanel(RoomStats stats) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int total = stats.totalRooms;
                if (total > 0) {
                    int width = getWidth() - 40;
                    int height = 30;
                    int x = 20;
                    int y = getHeight() / 2 - height / 2;
                    
                    // 绘制背景
                    g2d.setColor(new Color(240, 240, 240));
                    g2d.fill(new RoundRectangle2D.Double(x, y, width, height, 10, 10));
                    
                    // 绘制各状态比例
                    int occupiedWidth = (int)((double)stats.occupiedRooms / total * width);
                    int bookedWidth = (int)((double)stats.bookedRooms / total * width);
                    int checkoutWidth = (int)((double)stats.checkedOutRooms / total * width);
                    
                    // 已入住部分
                    g2d.setColor(new Color(46, 204, 113));
                    g2d.fill(new RoundRectangle2D.Double(x, y, occupiedWidth, height, 10, 10));
                    
                    // 已预订部分
                    g2d.setColor(ModernTheme.ACCENT_COLOR);
                    g2d.fill(new Rectangle(x + occupiedWidth, y, bookedWidth, height));
                    
                    // 已退房部分
                    g2d.setColor(new Color(231, 76, 60));
                    g2d.fill(new Rectangle(x + occupiedWidth + bookedWidth, y, checkoutWidth, height));
                    
                    // 绘制图例
                    int legendY = y + height + 20;
                    
                    g2d.setColor(new Color(46, 204, 113));
                    g2d.fillRect(x, legendY, 15, 15);
                    g2d.setColor(ModernTheme.TEXT_PRIMARY);
                    g2d.drawString("已入住: " + stats.occupiedRooms, x + 20, legendY + 12);
                    
                    g2d.setColor(ModernTheme.ACCENT_COLOR);
                    g2d.fillRect(x + 120, legendY, 15, 15);
                    g2d.setColor(ModernTheme.TEXT_PRIMARY);
                    g2d.drawString("已预订: " + stats.bookedRooms, x + 140, legendY + 12);
                    
                    g2d.setColor(new Color(231, 76, 60));
                    g2d.fillRect(x + 240, legendY, 15, 15);
                    g2d.setColor(ModernTheme.TEXT_PRIMARY);
                    g2d.drawString("已退房: " + stats.checkedOutRooms, x + 260, legendY + 12);
                }
                
                g2d.dispose();
            }
        };
        
        panel.setPreferredSize(new Dimension(0, 100));
        panel.setOpaque(false);
        
        return panel;
    }
    
    /**
     * 从数据库获取房间统计数据
     */
    private RoomStats getRoomStatsFromDatabase() {
        RoomStats stats = new RoomStats();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            LOGGER.info("连接数据库获取房间统计数据");
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                throw new SQLException("无法连接到数据库");
            }
            
            // 验证数据库连接有效性
            if (!DatabaseHelper.isConnectionValid(conn)) {
                throw new SQLException("数据库连接无效");
            }
            
            // 查询房间总数
            String query1 = "SELECT COUNT(*) FROM rooms";
            stmt = conn.prepareStatement(query1);
            stmt.setQueryTimeout(DB_TIMEOUT_SECONDS);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.totalRooms = rs.getInt(1);
            }
            rs.close();
            stmt.close();
            
            // 查询已入住房间数
            String query2 = "SELECT COUNT(*) FROM roomstate WHERE state='已入住'";
            stmt = conn.prepareStatement(query2);
            stmt.setQueryTimeout(DB_TIMEOUT_SECONDS);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.occupiedRooms = rs.getInt(1);
            }
            rs.close();
            stmt.close();
            
            // 查询已预订房间数
            String query3 = "SELECT COUNT(*) FROM roomstate WHERE state='已预订'";
            stmt = conn.prepareStatement(query3);
            stmt.setQueryTimeout(DB_TIMEOUT_SECONDS);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.bookedRooms = rs.getInt(1);
            }
            rs.close();
            stmt.close();
            
            // 查询已退房房间数
            String query4 = "SELECT COUNT(*) FROM roomstate WHERE state='已退房'";
            stmt = conn.prepareStatement(query4);
            stmt.setQueryTimeout(DB_TIMEOUT_SECONDS);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.checkedOutRooms = rs.getInt(1);
            }
            
            LOGGER.info("房间统计数据查询完成: 总房间=" + stats.totalRooms + 
                       ", 已入住=" + stats.occupiedRooms + 
                       ", 已预订=" + stats.bookedRooms + 
                       ", 已退房=" + stats.checkedOutRooms);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "获取房间统计数据失败", e);
            throw new RuntimeException("获取房间信息失败: " + e.getMessage(), e);
        } finally {
            // 确保关闭所有资源
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        return stats;
    }
    
    /**
     * 创建加载对话框
     */
    private JDialog createLoadingDialog(String message) {
        JDialog dialog = new JDialog(this, "处理中", true);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        
        // 创建面板
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ModernTheme.CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.setColor(new Color(230, 230, 230));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 创建消息标签
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(ModernTheme.REGULAR_FONT);
        messageLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(messageLabel, BorderLayout.CENTER);
        dialog.add(panel);
        
        return dialog;
    }
    
    /**
     * 房间统计数据类
     */
    private static class RoomStats {
        int totalRooms = 0;
        int occupiedRooms = 0;
        int bookedRooms = 0;
        int checkedOutRooms = 0;
    }

    // 主方法
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new allRoom());
    }
}
