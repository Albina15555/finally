package servlet;

import Dao.PermissionDao;
import Dao.UserDao;
import entity.Permission;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

//@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("login".equals(action)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            try {
                // 测试数据库连接
                PermissionDao permissionDao = new PermissionDao();
               // permissionDao.testConnection();

                if (UserDao.validateUser(username, password)) {
                    String userIdStr = UserDao.getUserIdByUsernameAndPassword(username, password);
                    if (userIdStr != null) {
                        int userId = Integer.parseInt(userIdStr);
                        HttpSession session = request.getSession();
                        session.setAttribute("username", username);
                        session.setAttribute("userId", userId);

                        // 加载用户权限
                        List<Permission> userPermissions = permissionDao.getUserRolePermissions(userId);
                        System.out.println("用户 " + username + " (ID:" + userId + ") 加载了 " + userPermissions.size() + " 个权限");
                        
                        // 输出所有权限名称
                        for (Permission p : userPermissions) {
                            System.out.println("- 权限: " + p.getPermissionName() + " (ID:" + p.getPermissionId() + ")");
                        }
                        
                        session.setAttribute("userPermissions", userPermissions);

                        response.sendRedirect(request.getContextPath() + "/content.jsp");
                    } else {
                        request.setAttribute("error", "用户ID获取失败");
                        request.getRequestDispatcher("login.jsp").forward(request, response);
                    }
                } else {
                    request.setAttribute("error", "用户名或密码错误");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                request.setAttribute("error", "数据库错误: " + e.getMessage());
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }
        }
    }
}