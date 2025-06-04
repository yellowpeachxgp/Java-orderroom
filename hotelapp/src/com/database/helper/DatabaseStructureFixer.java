package com.database.helper;

import java.sql.*;
import java.util.logging.Logger;

/**
 * 数据库表结构修复工具
 * 修正表结构与代码不匹配的问题
 */
public class DatabaseStructureFixer {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseStructureFixer.class.getName());
    
    public static void main(String[] args) {
        System.out.println("=== 数据库表结构修复工具 ===");
        
        if (!DatabaseHelper.testConnection()) {
            System.out.println("数据库连接失败，无法进行修复");
            return;
        }
        
        System.out.println("数据库连接正常\n");
        
        // 修复roomstate表结构
        fixRoomStateTable();
        
        // 修复bookroom表结构  
        fixBookRoomTable();
        
        // 修复client表结构
        fixClientTable();
        
        System.out.println("\n=== 修复完成 ===");
        
        // 重新运行诊断
        System.out.println("\n=== 验证修复结果 ===");
        DatabaseDiagnostic.main(new String[]{});
    }
    
    private static void fixRoomStateTable() {
        System.out.println("=== 修复roomstate表结构 ===");
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);
            
            // 检查表是否存在
            DatabaseMetaData meta = conn.getMetaData();
            rs = meta.getTables(null, null, "roomstate", new String[]{"TABLE"});
            
            if (rs.next()) {
                // 表存在，需要重建
                System.out.println("检测到roomstate表结构不匹配，正在重建...");
                
                rs.close();
                stmt = conn.createStatement();
                
                // 备份现有数据（如果有的话）
                System.out.println("备份现有数据...");
                stmt.executeUpdate("CREATE TEMPORARY TABLE roomstate_backup AS SELECT * FROM roomstate");
                
                // 删除原表
                stmt.executeUpdate("DROP TABLE roomstate");
                
                // 重新创建表
                String createSQL = "CREATE TABLE roomstate (" +
                                  "rNumber VARCHAR(50) NOT NULL PRIMARY KEY, " +
                                  "state VARCHAR(50) NOT NULL" +
                                  ")";
                stmt.executeUpdate(createSQL);
                System.out.println("roomstate表重建成功");
                
                // 尝试恢复数据（映射字段）
                try {
                    stmt.executeUpdate("INSERT INTO roomstate (rNumber, state) " +
                                     "SELECT rnumber, state FROM roomstate_backup " +
                                     "WHERE rnumber IS NOT NULL");
                    System.out.println("数据恢复成功");
                } catch (SQLException e) {
                    System.out.println("数据恢复失败，但表结构已修复: " + e.getMessage());
                }
                
                // 删除临时表
                stmt.executeUpdate("DROP TEMPORARY TABLE roomstate_backup");
                
            } else {
                // 表不存在，创建新表
                System.out.println("roomstate表不存在，创建新表...");
                stmt = conn.createStatement();
                String createSQL = "CREATE TABLE roomstate (" +
                                  "rNumber VARCHAR(50) NOT NULL PRIMARY KEY, " +
                                  "state VARCHAR(50) NOT NULL" +
                                  ")";
                stmt.executeUpdate(createSQL);
                System.out.println("roomstate表创建成功");
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            System.out.println("修复roomstate表时出错: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        System.out.println();
    }
    
    private static void fixBookRoomTable() {
        System.out.println("=== 修复bookroom表结构 ===");
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);
            
            // 检查表是否存在及其结构
            DatabaseMetaData meta = conn.getMetaData();
            rs = meta.getTables(null, null, "bookroom", new String[]{"TABLE"});
            
            if (rs.next()) {
                rs.close();
                
                // 检查列结构
                rs = meta.getColumns(null, null, "bookroom", "%");
                boolean needsRestructure = false;
                
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    String dataType = rs.getString("TYPE_NAME");
                    
                    if ("bookdate".equals(columnName) && "DATE".equals(dataType)) {
                        needsRestructure = true;
                        break;
                    }
                }
                rs.close();
                
                if (needsRestructure) {
                    System.out.println("检测到bookroom表结构不匹配，正在重建...");
                    
                    stmt = conn.createStatement();
                    
                    // 备份现有数据
                    System.out.println("备份现有数据...");
                    stmt.executeUpdate("CREATE TEMPORARY TABLE bookroom_backup AS SELECT * FROM bookroom");
                    
                    // 删除原表
                    stmt.executeUpdate("DROP TABLE bookroom");
                    
                    // 重新创建表
                    String createSQL = "CREATE TABLE bookroom (" +
                                      "bookNumber VARCHAR(50) NOT NULL PRIMARY KEY, " +
                                      "cNumber VARCHAR(50) NOT NULL, " +
                                      "rNumber VARCHAR(50) NOT NULL, " +
                                      "bookdate VARCHAR(50) NOT NULL, " +
                                      "checkin VARCHAR(50) NOT NULL" +
                                      ")";
                    stmt.executeUpdate(createSQL);
                    System.out.println("bookroom表重建成功");
                    
                    // 恢复数据（转换日期格式）
                    try {
                        stmt.executeUpdate("INSERT INTO bookroom (bookNumber, cNumber, rNumber, bookdate, checkin) " +
                                         "SELECT bookNumber, cNumber, rNumber, DATE_FORMAT(bookdate, '%Y-%m-%d'), checkin " +
                                         "FROM bookroom_backup");
                        System.out.println("数据恢复成功");
                    } catch (SQLException e) {
                        System.out.println("数据恢复失败，但表结构已修复: " + e.getMessage());
                    }
                    
                    // 删除临时表
                    stmt.executeUpdate("DROP TEMPORARY TABLE bookroom_backup");
                    
                } else {
                    System.out.println("bookroom表结构正确，无需修复");
                }
                
            } else {
                // 表不存在，创建新表
                System.out.println("bookroom表不存在，创建新表...");
                stmt = conn.createStatement();
                String createSQL = "CREATE TABLE bookroom (" +
                                  "bookNumber VARCHAR(50) NOT NULL PRIMARY KEY, " +
                                  "cNumber VARCHAR(50) NOT NULL, " +
                                  "rNumber VARCHAR(50) NOT NULL, " +
                                  "bookdate VARCHAR(50) NOT NULL, " +
                                  "checkin VARCHAR(50) NOT NULL" +
                                  ")";
                stmt.executeUpdate(createSQL);
                System.out.println("bookroom表创建成功");
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            System.out.println("修复bookroom表时出错: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        System.out.println();
    }
    
    private static void fixClientTable() {
        System.out.println("=== 修复client表结构 ===");
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);
            
            // 检查表是否存在
            DatabaseMetaData meta = conn.getMetaData();
            rs = meta.getTables(null, null, "client", new String[]{"TABLE"});
            
            if (rs.next()) {
                rs.close();
                
                // 检查是否有cAddress列
                rs = meta.getColumns(null, null, "client", "cAddress");
                boolean hasAddressColumn = rs.next();
                rs.close();
                
                if (!hasAddressColumn) {
                    System.out.println("添加cAddress列到client表...");
                    stmt = conn.createStatement();
                    stmt.executeUpdate("ALTER TABLE client ADD COLUMN cAddress VARCHAR(100)");
                    System.out.println("cAddress列添加成功");
                } else {
                    System.out.println("client表结构正确，无需修复");
                }
                
            } else {
                // 表不存在，创建新表
                System.out.println("client表不存在，创建新表...");
                stmt = conn.createStatement();
                String createSQL = "CREATE TABLE client (" +
                                  "cNumber VARCHAR(50) NOT NULL PRIMARY KEY, " +
                                  "cName VARCHAR(50) NOT NULL, " +
                                  "cSex VARCHAR(10) NOT NULL, " +
                                  "cTel VARCHAR(50), " +
                                  "cAddress VARCHAR(100)" +
                                  ")";
                stmt.executeUpdate(createSQL);
                System.out.println("client表创建成功");
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            System.out.println("修复client表时出错: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        
        System.out.println();
    }
} 