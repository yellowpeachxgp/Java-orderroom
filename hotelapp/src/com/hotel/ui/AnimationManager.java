package com.hotel.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 动画效果管理器
 * 提供各种UI动画效果
 */
public class AnimationManager {
    
    /**
     * 按钮点击动画效果
     * @param button 要添加动画的按钮
     */
    public static void addButtonClickAnimation(JButton button) {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    try {
                        Color originalColor = button.getBackground();
                        button.setBackground(originalColor.darker());
                        Thread.sleep(100);
                        button.setBackground(originalColor);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }
    
    /**
     * 组件淡入效果
     * @param component 要添加效果的组件
     * @param duration 动画持续时间（毫秒）
     */
    public static void fadeIn(final JComponent component, final int duration) {
        final Timer timer = new Timer(20, null);
        final long startTime = System.currentTimeMillis();
        
        component.setVisible(true);
        component.setOpaque(false);
        if (component instanceof JPanel) {
            ((JPanel) component).setBackground(new Color(
                component.getBackground().getRed(),
                component.getBackground().getGreen(),
                component.getBackground().getBlue(),
                0
            ));
        }
        
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = (float) elapsed / duration;
                
                if (progress >= 1f) {
                    progress = 1f;
                    timer.stop();
                }
                
                int alpha = (int) (progress * 255);
                
                if (component instanceof JPanel) {
                    ((JPanel) component).setBackground(new Color(
                        component.getBackground().getRed(),
                        component.getBackground().getGreen(),
                        component.getBackground().getBlue(),
                        alpha
                    ));
                }
                
                component.repaint();
            }
        });
        
        timer.start();
    }
    
    /**
     * 组件淡出效果
     * @param component 要添加效果的组件
     * @param duration 动画持续时间（毫秒）
     */
    public static void fadeOut(final JComponent component, final int duration) {
        final Timer timer = new Timer(20, null);
        final long startTime = System.currentTimeMillis();
        
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = (float) elapsed / duration;
                
                if (progress >= 1f) {
                    progress = 1f;
                    timer.stop();
                    component.setVisible(false);
                }
                
                int alpha = (int) ((1f - progress) * 255);
                
                if (component instanceof JPanel) {
                    ((JPanel) component).setBackground(new Color(
                        component.getBackground().getRed(),
                        component.getBackground().getGreen(),
                        component.getBackground().getBlue(),
                        alpha
                    ));
                }
                
                component.repaint();
            }
        });
        
        timer.start();
    }
    
    /**
     * 滑入效果（从上方滑入）
     * @param component 要添加效果的组件
     * @param container 组件的容器
     * @param duration 动画持续时间（毫秒）
     */
    public static void slideInFromTop(final JComponent component, final Container container, final int duration) {
        final int targetY = component.getY();
        final int startY = -component.getHeight();
        component.setLocation(component.getX(), startY);
        component.setVisible(true);
        
        final Timer timer = new Timer(20, null);
        final long startTime = System.currentTimeMillis();
        
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = (float) elapsed / duration;
                
                if (progress >= 1f) {
                    progress = 1f;
                    timer.stop();
                }
                
                int y = startY + (int) (progress * (targetY - startY));
                component.setLocation(component.getX(), y);
                container.repaint();
            }
        });
        
        timer.start();
    }
    
    /**
     * 缩放动画效果
     * @param component 要添加效果的组件
     * @param startScale 起始缩放比例（0.0-1.0）
     * @param endScale 结束缩放比例（通常为1.0）
     * @param duration 动画持续时间（毫秒）
     */
    public static void scaleAnimation(final JComponent component, final float startScale, final float endScale, final int duration) {
        component.setVisible(true);
        final Timer timer = new Timer(20, null);
        final long startTime = System.currentTimeMillis();
        
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = (float) elapsed / duration;
                
                if (progress >= 1f) {
                    progress = 1f;
                    timer.stop();
                }
                
                float scale = startScale + (progress * (endScale - startScale));
                
                // 应用缩放
                component.setSize((int)(component.getPreferredSize().width * scale), 
                                 (int)(component.getPreferredSize().height * scale));
                component.revalidate();
                component.repaint();
            }
        });
        
        timer.start();
    }
    
    /**
     * 给组件添加鼠标悬停效果
     * @param component 要添加效果的组件
     */
    public static void addHoverEffect(final JComponent component) {
        component.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Color backgroundColor = component.getBackground();
                component.setBackground(backgroundColor.brighter());
                component.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Color backgroundColor = component.getBackground();
                component.setBackground(backgroundColor.darker());
                component.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
} 