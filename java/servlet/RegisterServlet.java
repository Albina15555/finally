package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Dao.UserDao;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(RegisterServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("register".equals(action)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone"); // 获取手机号

            try {
                // 验证必填字段
                if (username == null || username.isEmpty() || 
                    password == null || password.isEmpty() ||
                    email == null || email.isEmpty() ||
                    phone == null || phone.isEmpty()) {
                    request.setAttribute("error", "所有字段都必须填写");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                    return;
                }

                // 检查用户名是否已存在
                if (UserDao.checkUsernameExists(username)) {
                    request.setAttribute("error", "用户名已存在，请更换用户名");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                    return;
                }

                // 执行注册（不加密密码）
                boolean success = UserDao.registerUser(username, password, email, phone);
                
                if (success) {
                    request.setAttribute("message", "注册成功，请登录");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                } else {
                    request.setAttribute("error", "注册失败，请稍后再试");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "注册失败: " + e.getMessage(), e);
                request.setAttribute("error", "数据库错误：" + e.getMessage());
                request.getRequestDispatcher("login.jsp").forward(request, response);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "未知错误: " + e.getMessage(), e);
                request.setAttribute("error", "系统错误：" + e.getMessage());
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }
        }
    }
}