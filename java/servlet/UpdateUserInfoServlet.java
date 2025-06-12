package servlet;

import Dao.UserDao;
import entity.User;
import filter.PermissionChecker;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/UpdateUserInfoServlet")
public class UpdateUserInfoServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 设置响应头，禁止缓存
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        int currentUserId = (int) session.getAttribute("userId");
        
        try {
            // 确定目标用户ID
            int targetUserId = "adminUpdateUserInfo".equals(action) && 
                              PermissionChecker.hasPermission(session, "function10") ?
                              Integer.parseInt(request.getParameter("targetUserId")) :
                              currentUserId;
            
            // 获取表单数据
            String newUsername = request.getParameter("newUsername");
            String newPassword = request.getParameter("newPassword");
            String newEmail = request.getParameter("newEmail");
            String newPhone = request.getParameter("newPhone");
            
            // 验证用户名是否已存在（除当前用户外）
            if (newUsername != null && !newUsername.isEmpty()) {
                UserDao userDao = new UserDao();
                if (userDao.isUsernameExists(newUsername, targetUserId)) {
                    session.setAttribute("error", "用户名已被使用");
                    response.sendRedirect("function11.jsp?action=" + action);
                    return;
                }
            }
            
            // 执行更新
            updateUserInfo(targetUserId, newUsername, newPassword, newEmail, newPhone);
            
            // 设置成功消息
            String successMsg = "adminUpdateUserInfo".equals(action) ? 
                              "用户信息更新成功" : "个人信息更新成功";
            session.setAttribute("message", successMsg);
            
            // 如果更新的是当前用户，更新会话信息
            if (currentUserId == targetUserId) {
                // 获取最新的用户信息
                UserDao userDao = new UserDao();
                User updatedUser = userDao.getUserById(targetUserId);
                
                // 使旧会话无效
                session.invalidate();
                
                // 创建新会话并设置用户信息
                session = request.getSession(true);
                session.setAttribute("userId", updatedUser.getUserId());
                session.setAttribute("username", updatedUser.getUsername());
                session.setAttribute("user", updatedUser); // 存储完整用户对象
                
                // 重新验证权限
                boolean isAdmin = PermissionChecker.hasPermission(session, "function10");
                session.setAttribute("isAdmin", isAdmin);
            }
            
            // 重定向回页面
            redirectAfterUpdate(action, targetUserId, response);
            
        } catch (SQLException e) {
            handleSQLException(session, e, action, response);
        } catch (NumberFormatException e) {
            session.setAttribute("error", "无效的用户ID格式");
            response.sendRedirect("function11.jsp?action=" + action);
        } catch (Exception e) {
            session.setAttribute("error", "系统错误: " + e.getMessage());
            response.sendRedirect("function11.jsp?action=" + action);
        }
    }
    
    // 执行数据库更新
    private void updateUserInfo(int targetUserId, String newUsername, 
                               String newPassword, String newEmail, 
                               String newPhone) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            StringBuilder sql = new StringBuilder("UPDATE user_table SET ");
            
            // 构建动态SQL
            List<String> updates = new ArrayList<>();
            List<Object> params = new ArrayList<>();
            
            if (newUsername != null && !newUsername.isEmpty()) {
                updates.add("username = ?");
                params.add(newUsername);
            }
            
            
            
            if (newEmail != null && !newEmail.isEmpty()) {
                updates.add("email = ?");
                params.add(newEmail);
            }
            
            if (newPhone != null && !newPhone.isEmpty()) {
                updates.add("phone = ?");
                params.add(newPhone);
            }
            
            // 如果没有要更新的字段
            if (updates.isEmpty()) {
                throw new SQLException("没有提供要更新的信息");
            }
            
            sql.append(String.join(", ", updates));
            sql.append(" WHERE user_id = ?");
            pstmt = conn.prepareStatement(sql.toString());
            
            // 设置参数
            int paramIndex = 1;
            for (Object param : params) {
                if (param instanceof String) {
                    pstmt.setString(paramIndex++, (String) param);
                } else {
                    pstmt.setObject(paramIndex++, param);
                }
            }
            pstmt.setInt(paramIndex, targetUserId);
            
            // 执行更新
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("未更新任何记录，可能用户不存在");
            }
            
        } finally {
            // 关闭资源
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        }
    }
    
    // 重定向逻辑
    private void redirectAfterUpdate(String action, int targetUserId, 
                                    HttpServletResponse response) throws IOException {
        if ("adminUpdateUserInfo".equals(action)) {
            response.sendRedirect("function11.jsp?action=adminUpdateUserInfo&targetUserId=" + targetUserId);
        } else {
            response.sendRedirect("function11.jsp?action=userUpdateUserInfo");
        }
    }
    
    // 处理SQL异常
    private void handleSQLException(HttpSession session, SQLException e, 
                                   String action, HttpServletResponse response) 
            throws IOException {
        
        String errorMsg;
        
        // 根据错误类型提供用户友好的消息
        switch (e.getErrorCode()) {
            case 0: 
                errorMsg = e.getMessage();
                break;
            case 1062:
                errorMsg = "用户名已被使用";
                break;
            case 1406:
                errorMsg = "输入数据过长";
                break;
            case 1452:
                errorMsg = "关联数据异常";
                break;
            default:
                errorMsg = "数据库错误: " + e.getErrorCode();
        }
        
        session.setAttribute("error", errorMsg);
        response.sendRedirect("function11.jsp?action=" + action);
    }
}