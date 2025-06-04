package com.database.helper;

import java.sql.Connection;

/**
 * 测试数据库连接的工具类
 */
public class TestConnection {

    public static void main(String[] args) {
        System.out.println("开始测试数据库连接...");
        
        // 测试JDBC驱动是否可以加载
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("成功加载JDBC驱动");
        } catch (ClassNotFoundException e) {
            System.out.println("加载JDBC驱动失败: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // 测试数据库连接
        Connection conn = null;
        try {
            System.out.println("尝试连接到数据库...");
            conn = DatabaseHelper.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("数据库连接成功!");
                System.out.println("数据库URL: " + conn.getMetaData().getURL());
                System.out.println("数据库产品名称: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("数据库版本: " + conn.getMetaData().getDatabaseProductVersion());
            } else {
                System.out.println("数据库连接失败!");
            }
        } catch (Exception e) {
            System.out.println("连接数据库时出错: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭连接
            DatabaseHelper.closeConnection(conn);
        }
    }
} 