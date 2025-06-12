package servlet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Sqlite {
    private static Connection c = null;

    static {
        try {
            // 加载 SQLite JDBC 驱动
            Class.forName("org.sqlite.JDBC");
            // 建立数据库连接
            c = DriverManager.getConnection("jdbc:sqlite:D:/大学/大二/大二上/课程/java web/java web/11.1/jxgl.db");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    // 获取连接对象的方法
    public static Connection getConnection() {
        return c;
    }

    // 通用的关闭资源方法，可传入Connection、Statement等实现Closeable接口的对象来关闭
    public static void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource!= null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}