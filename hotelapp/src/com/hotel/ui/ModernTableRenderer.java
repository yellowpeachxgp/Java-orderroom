package com.hotel.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * 现代化表格渲染器
 * 实现扁平化表格设计
 */
public class ModernTableRenderer {

    /**
     * 应用现代化样式到表格
     * @param table 要应用样式的表格
     */
    public static void applyModernStyle(JTable table) {
        // 设置表格基本属性
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(ModernTheme.PRIMARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.setBackground(ModernTheme.CARD_COLOR);
        table.setForeground(ModernTheme.TEXT_PRIMARY);
        table.setFont(ModernTheme.REGULAR_FONT);
        
        // 设置表头样式
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new ModernHeaderRenderer());
        header.setBackground(ModernTheme.PRIMARY_DARK_COLOR);
        header.setForeground(Color.WHITE);
        header.setFont(ModernTheme.HEADER_FONT);
        header.setBorder(null);
        
        // 设置单元格渲染器
        table.setDefaultRenderer(Object.class, new ModernCellRenderer());
        
        // 添加行交替颜色
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // 设置对齐方式为居中
                setHorizontalAlignment(SwingConstants.CENTER);
                
                // 添加内边距
                setBorder(new EmptyBorder(5, 10, 5, 10));
                
                // 设置字体和颜色
                comp.setFont(ModernTheme.REGULAR_FONT);
                
                if (isSelected) {
                    comp.setBackground(ModernTheme.PRIMARY_COLOR);
                    comp.setForeground(Color.WHITE);
                } else {
                    // 交替行颜色
                    if (row % 2 == 0) {
                        comp.setBackground(ModernTheme.CARD_COLOR);
                    } else {
                        comp.setBackground(new Color(245, 245, 250)); // 稍微浅一点的交替色
                    }
                    comp.setForeground(ModernTheme.TEXT_PRIMARY);
                }
                
                return comp;
            }
        });
        
        // 设置表格滚动面板的边框
        table.setGridColor(new Color(240, 240, 240));
    }

    /**
     * 现代化表头渲染器
     */
    private static class ModernHeaderRenderer extends DefaultTableCellRenderer {
        public ModernHeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(ModernTheme.HEADER_FONT);
            setBorder(new EmptyBorder(10, 10, 10, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            comp.setBackground(ModernTheme.PRIMARY_DARK_COLOR);
            comp.setForeground(Color.WHITE);
            return comp;
        }
    }

    /**
     * 现代化单元格渲染器
     */
    private static class ModernCellRenderer extends DefaultTableCellRenderer {
        public ModernCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (isSelected) {
                comp.setBackground(ModernTheme.PRIMARY_COLOR);
                comp.setForeground(Color.WHITE);
            } else {
                // 交替行颜色
                if (row % 2 == 0) {
                    comp.setBackground(ModernTheme.CARD_COLOR);
                } else {
                    comp.setBackground(new Color(245, 245, 250));
                }
                comp.setForeground(ModernTheme.TEXT_PRIMARY);
            }
            
            return comp;
        }
    }
} 