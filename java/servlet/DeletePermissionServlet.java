package servlet;

import Dao.PermissionDao;
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

@WebServlet("/DeletePermissionServlet")
public class DeletePermissionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
        // 检查用户是否有删除权限
        if (!PermissionChecker.hasPermission(session, "function10")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "您没有删除权限的权限");
            return;
        }
        
        // 获取要删除的权限ID和角色ID
        String permissionIdStr = request.getParameter("permission_id");
        String roleIdStr = request.getParameter("role_id");
        
        int permissionId = 0;
        int roleId = 0;
        
        try {
            permissionId = Integer.parseInt(permissionIdStr);
            roleId = Integer.parseInt(roleIdStr);
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "无效的权限ID或角色ID");
            request.getRequestDispatcher("function10.jsp").forward(request, response);
            return;
        }
        
        // 执行删除操作
        boolean success = deleteRolePermission(permissionId, roleId);
        
        if (success) {
            // 删除成功，使用重定向并传递成功消息
            response.sendRedirect("PermissionServlet?success=角色权限关联已成功删除");
            return;
        } else {
            // 删除失败，使用重定向并传递错误消息
            response.sendRedirect("PermissionServlet?error=删除角色权限关联失败，请重试");
            return;
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    private boolean deleteRolePermission(int permissionId, int roleId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;
        
        try {
            conn = DBConnection.getConnection();
            
            // 开始事务
            conn.setAutoCommit(false);
            
            // 删除指定角色的指定权限关联
            String deleteRolePermissionSql = "DELETE FROM role_permission WHERE permission_id = ? AND role_id = ?";
            stmt = conn.prepareStatement(deleteRolePermissionSql);
            stmt.setInt(1, permissionId);
            stmt.setInt(2, roleId);
            int rowsAffected = stmt.executeUpdate();
            
            // 如果成功删除至少一行，提交事务
            if (rowsAffected > 0) {
                conn.commit();
                success = true;
            } else {
                conn.rollback();
            }
            
        } catch (SQLException e) {
            // 发生异常时回滚事务
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return success;
    }
}
    
    