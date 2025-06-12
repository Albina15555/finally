package servlet;

import Dao.PermissionDao;
import Dao.RoleDao;
import entity.Permission;
import entity.Role;
import filter.PermissionChecker;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/UpdatePermissionServlet")
public class UpdatePermissionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        // 检查用户是否登录
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
        // 检查用户是否有修改权限
        if (!PermissionChecker.hasPermission(session, "function10")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "您没有编辑权限的权限");
            return;
        }
        
        try {
            // 获取表单参数
            int permissionId = Integer.parseInt(request.getParameter("permission_id"));
            String permissionName = request.getParameter("permission_name");
            String permissionCode = request.getParameter("permission_code");
            String description = request.getParameter("description");
            
            // 创建权限对象
            Permission permission = new Permission();
            permission.setPermissionId(permissionId);
            permission.setPermissionName(permissionName);
            permission.setPermissionCode(permissionCode);
            permission.setDescription(description);
            
            // 更新权限
            PermissionDao permissionDao = new PermissionDao();
            boolean success = permissionDao.updatePermission(permission);
            
            if (success) {
                // 重定向回权限管理页面，带成功消息
                response.sendRedirect("PermissionServlet?success=权限更新成功");
            } else {
                // 重定向回权限管理页面，带错误消息
                response.sendRedirect("PermissionServlet?error=权限更新失败");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect("PermissionServlet?error=无效的权限ID格式");
        } catch (Exception e) {
            response.sendRedirect("PermissionServlet?error=系统错误：" + e.getMessage());
        }
    }
}

