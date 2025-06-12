package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/main";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "mysql";
    
    // 获取数据库连接
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 添加连接参数，解决时区问题
            String fullUrl = DB_URL + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            return DriverManager.getConnection(fullUrl, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL驱动加载失败", e);
        }
    }
    
    // 关闭资源的工具方法
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}