package com.database.helper;

import java.sql.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.net.URL;

/**
 * 数据库连接帮助类
 * 用于管理数据库连接和资源关闭
 */
public class DatabaseHelper {
    // 数据库连接的JDBC URL
    private static final String url = "jdbc:mysql://121.62.31.62:3306/hotelapp";
    // 数据库用户名
    private static final String user = "root";
    // 数据库密码
    private static final String password = "YELLOWpeach0331";
    
    // 连接超时设置（秒）
    private static final int CONNECTION_TIMEOUT = 3;
    private static final int SOCKET_TIMEOUT = 5000; // 5秒Socket超时
    private static final int MAX_RETRY_ATTEMPTS = 3; // 最大重试次数
    
    // 日志记录器
    private static final Logger LOGGER = Logger.getLogger(DatabaseHelper.class.getName());
    
    // 静态初始化块 - 确保驱动程序正确加载
    static {
        initializeDriver();
    }
    
    /**
     * 初始化JDBC驱动程序
     */
    private static void initializeDriver() {
        try {
            // 尝试多种方式加载MySQL驱动
            LOGGER.info("正在初始化MySQL JDBC驱动...");
            
            // 方式1：直接加载类
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                LOGGER.info("MySQL JDBC驱动加载成功 (方式1)");
                return;
            } catch (ClassNotFoundException e) {
                LOGGER.warning("方式1加载驱动失败: " + e.getMessage());
            }
            
            // 方式2：使用当前线程的类加载器
            try {
                Thread.currentThread().getContextClassLoader().loadClass("com.mysql.cj.jdbc.Driver");
                LOGGER.info("MySQL JDBC驱动加载成功 (方式2)");
                return;
            } catch (ClassNotFoundException e) {
                LOGGER.warning("方式2加载驱动失败: " + e.getMessage());
            }
            
            // 方式3：检查驱动JAR文件是否存在
            checkDriverJarFile();
            
            // 方式4：使用DriverManager自动注册
            try {
                // 让DriverManager自动注册驱动
                DriverManager.getDrivers();
                LOGGER.info("使用DriverManager自动注册驱动");
            } catch (Exception e) {
                LOGGER.severe("无法使用DriverManager自动注册驱动: " + e.getMessage());
                // 不抛出异常，继续运行
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "驱动程序初始化失败", e);
            // 不抛出异常，允许程序继续运行并在连接时处理错误
        }
    }
    
    /**
     * 检查驱动JAR文件是否存在
     */
    private static void checkDriverJarFile() {
        String[] possiblePaths = {
            "lib/mysql-connector-j-9.3.0.jar",
            "../mysql-connector-j-9.3.0.jar",
            "mysql-connector-j-9.3.0.jar"
        };
        
        for (String path : possiblePaths) {
            File jarFile = new File(path);
            if (jarFile.exists()) {
                LOGGER.info("找到MySQL驱动JAR文件: " + jarFile.getAbsolutePath());
                return;
            }
        }
        
        // 检查类路径中的驱动
        try {
            URL driverUrl = DatabaseHelper.class.getClassLoader().getResource("com/mysql/cj/jdbc/Driver.class");
            if (driverUrl != null) {
                LOGGER.info("在类路径中找到MySQL驱动: " + driverUrl);
            } else {
                LOGGER.warning("未在类路径中找到MySQL驱动");
            }
        } catch (Exception e) {
            LOGGER.warning("检查类路径中的驱动时出错: " + e.getMessage());
        }
    }
    
    /**
     * 获取数据库连接
     * 每次调用都返回一个新的连接，避免共享连接可能导致的问题
     * @return 数据库连接
     */
    public static Connection getConnection() {
        Connection connection = null;
        Exception lastException = null;
        
        // 重试机制
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                LOGGER.info("尝试数据库连接 (第 " + attempt + " 次)");
                
                // 设置连接属性
                Properties props = new Properties();
                props.setProperty("user", user);
                props.setProperty("password", password);
                props.setProperty("connectTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT)));
                props.setProperty("autoReconnect", "true");
                props.setProperty("useSSL", "false");
                
                // 设置更多连接属性以提高稳定性
                props.setProperty("socketTimeout", String.valueOf(SOCKET_TIMEOUT));
                props.setProperty("connectionTimeoutMillis", String.valueOf(TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT)));
                props.setProperty("maxReconnects", "3");
                props.setProperty("initialTimeout", "1");
                props.setProperty("failOverReadOnly", "false");
                props.setProperty("tcpKeepAlive", "true");
                props.setProperty("useUnicode", "true");
                props.setProperty("characterEncoding", "UTF-8");
                props.setProperty("serverTimezone", "Asia/Shanghai");
                
                // 创建新连接
                connection = DriverManager.getConnection(url, props);
                
                // 验证连接
                if (connection != null && isConnectionValid(connection)) {
                    LOGGER.info("数据库连接成功 (第 " + attempt + " 次尝试)");
                    
                    // 初始化数据库结构（如果需要）
                    initializeDatabaseIfNeeded(connection);
                    
                    return connection;
                } else {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            LOGGER.warning("关闭无效连接时出错: " + e.getMessage());
                        }
                    }
                    throw new SQLException("连接验证失败");
                }
                
            } catch (SQLException e) {
                lastException = e;
                LOGGER.log(Level.WARNING, "数据库连接失败 (第 " + attempt + " 次): " + e.getMessage(), e);
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        // 重试前等待
                        Thread.sleep(1000 * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                lastException = e;
                LOGGER.log(Level.SEVERE, "获取数据库连接时发生异常 (第 " + attempt + " 次): " + e.getMessage(), e);
                break; // 对于非SQL异常，不重试
            }
        }
        
        // 所有重试都失败了
        LOGGER.log(Level.SEVERE, "所有数据库连接尝试都失败了", lastException);
        return null;
    }

    /**
     * 初始化数据库结构（如果需要）
     * 检查并创建必要的表结构
     */
    private static void initializeDatabaseIfNeeded(Connection conn) {
        if (conn == null) return;
        
        try {
            LOGGER.info("检查数据库结构...");
            
            // 检查核心表是否存在
            if (!isTableExists(conn, "rooms")) {
                createRoomsTable(conn);
            }
            
            if (!isTableExists(conn, "client")) {
                createClientTable(conn);
            }
            
            if (!isTableExists(conn, "leader")) {
                createLeaderTable(conn);
            }
            
            if (!isTableExists(conn, "bookroom")) {
                createBookRoomTable(conn);
            }
            
            if (!isTableExists(conn, "roomstate")) {
                createRoomStateTable(conn);
            }
            
            // 检查认证相关表
            if (!isTableExists(conn, "user_auth")) {
                createUserAuthTable(conn);
            }
            
            if (!isTableExists(conn, "permission_group")) {
                createPermissionGroupTable(conn);
            }
            
            if (!isTableExists(conn, "user_group")) {
                createUserGroupTable(conn);
            }
            
            LOGGER.info("数据库结构检查完成");
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "初始化数据库结构失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查表是否存在
     */
    private static boolean isTableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getTables(null, null, tableName, new String[] {"TABLE"});
        boolean exists = rs.next();
        rs.close();
        return exists;
    }
    
    /**
     * 创建rooms表
     */
    private static void createRoomsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE rooms (" +
                    "rNumber VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "rType VARCHAR(50) NOT NULL, " +
                    "rPrice VARCHAR(50) NOT NULL, " +
                    "lNumber VARCHAR(50), " +
                    "lTel VARCHAR(50))";
        executeUpdate(conn, sql, "rooms表创建成功");
    }
    
    /**
     * 创建client表
     */
    private static void createClientTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE client (" +
                    "cNumber VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "cName VARCHAR(50) NOT NULL, " +
                    "cSex VARCHAR(10) NOT NULL, " +
                    "cTel VARCHAR(50), " +
                    "cAddress VARCHAR(100))";
        executeUpdate(conn, sql, "client表创建成功");
    }
    
    /**
     * 创建leader表
     */
    private static void createLeaderTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE leader (" +
                    "lNumber VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "lName VARCHAR(50) NOT NULL, " +
                    "lSex VARCHAR(10) NOT NULL, " +
                    "lTel VARCHAR(50), " +
                    "lAddress VARCHAR(100))";
        executeUpdate(conn, sql, "leader表创建成功");
    }
    
    /**
     * 创建bookroom表
     */
    private static void createBookRoomTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE bookroom (" +
                    "bookNumber VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "cNumber VARCHAR(50) NOT NULL, " +
                    "rNumber VARCHAR(50) NOT NULL, " +
                    "bookdate VARCHAR(50) NOT NULL, " +
                    "checkin VARCHAR(50) NOT NULL)";
        executeUpdate(conn, sql, "bookroom表创建成功");
    }
    
    /**
     * 创建roomstate表
     */
    private static void createRoomStateTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE roomstate (" +
                    "rNumber VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "state VARCHAR(50) NOT NULL)";
        executeUpdate(conn, sql, "roomstate表创建成功");
    }
    
    /**
     * 创建user_auth表
     */
    private static void createUserAuthTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE user_auth (" +
                    "user_id VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "salt VARCHAR(255) NOT NULL, " +
                    "account_type INT NOT NULL, " +
                    "status INT DEFAULT 1, " +
                    "last_login DATETIME, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        executeUpdate(conn, sql, "user_auth表创建成功");
    }
    
    /**
     * 创建permission_group表
     */
    private static void createPermissionGroupTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE permission_group (" +
                    "group_id INT NOT NULL PRIMARY KEY, " +
                    "group_name VARCHAR(50) NOT NULL, " +
                    "description VARCHAR(255))";
        executeUpdate(conn, sql, "permission_group表创建成功");
        
        // 插入默认权限组
        String insertSql = "INSERT INTO permission_group VALUES (1, '普通用户', '基本访问权限'), " +
                          "(2, '管理员', '系统管理权限')";
        executeUpdate(conn, insertSql, "默认权限组添加成功");
    }
    
    /**
     * 创建user_group表
     */
    private static void createUserGroupTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE user_group (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id VARCHAR(50) NOT NULL, " +
                    "group_id INT NOT NULL)";
        executeUpdate(conn, sql, "user_group表创建成功");
    }
    
    /**
     * 执行更新操作并记录日志
     */
    private static void executeUpdate(Connection conn, String sql, String successMessage) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            LOGGER.info(successMessage);
        }
    }

    /**
     * 关闭数据库连接
     * @param connection 要关闭的连接
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "关闭连接失败: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 关闭数据库连接和相关资源
     * @param connection 数据库连接
     * @param statement SQL语句对象
     * @param resultSet 结果集
     */
    public static void closeResources(Connection connection, Statement statement, ResultSet resultSet) {
        // 关闭结果集
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "关闭结果集失败: " + e.getMessage(), e);
            }
        }
        
        // 关闭Statement
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "关闭Statement失败: " + e.getMessage(), e);
            }
        }
        
        // 关闭连接
        closeConnection(connection);
    }
    
    /**
     * 获取下一个可用的ID值（自增主键）
     * @param tableName 表名
     * @param idColumn ID列名
     * @param prefix ID前缀（如"USER"）
     * @return 生成的ID
     */
    public static String getNextId(String tableName, String idColumn, String prefix) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String nextId = prefix + "001"; // 默认初始ID
        
        try {
            conn = getConnection();
            if (conn == null) {
                LOGGER.warning("无法获取数据库连接以生成ID");
                return nextId;
            }
            
            // 查询当前最大ID
            String query = "SELECT MAX(" + idColumn + ") FROM " + tableName;
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            if (rs.next() && rs.getString(1) != null) {
                String currentMaxId = rs.getString(1);
                
                // 提取数字部分
                if (currentMaxId.startsWith(prefix)) {
                    String numPart = currentMaxId.substring(prefix.length());
                    try {
                        int num = Integer.parseInt(numPart);
                        num++; // 增加1
                        // 格式化为3位数，前导零
                        nextId = prefix + String.format("%03d", num);
                    } catch (NumberFormatException e) {
                        LOGGER.warning("ID格式转换异常: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "生成ID失败: " + e.getMessage(), e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return nextId;
    }
    
    /**
     * 测试数据库连接的健康状态
     * @param connection 要测试的连接
     * @return 连接是否可用
     */
    public static boolean isConnectionValid(Connection connection) {
        if (connection == null) return false;
        
        Statement stmt = null;
        ResultSet rs = null;
        try {
            // 设置较短的超时时间
            if (connection.isValid(2)) {
                // 再执行简单查询确认
                stmt = connection.createStatement();
                stmt.setQueryTimeout(2); // 2秒超时
                rs = stmt.executeQuery("SELECT 1");
                return rs.next();
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "连接测试失败: " + e.getMessage(), e);
            return false;
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* 忽略 */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* 忽略 */ }
            }
        }
    }
    
    /**
     * 测试数据库连接
     * @return 是否连接成功
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) return false;
            
            return isConnectionValid(conn);
        } finally {
            closeConnection(conn);
        }
    }
}
