package servlet;

import Dao.PermissionDao;
import filter.PermissionChecker;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/EditPermissionServlet")
public class EditPermissionServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(EditPermissionServlet.class.getName());
    private static final String SUCCESS_MSG = "success";
    private static final String ERROR_MSG = "error";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 设置请求编码
        request.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        // 检查用户是否登录
        if (userId == null) {
            redirectWithError(request, response, "请先登录");
            return;
        }
        
        // 检查用户是否有修改权限
        if (!PermissionChecker.hasPermission(session, "function10")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "您没有编辑权限的权限");
            return;
        }
        
        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            redirectWithError(request, response, "缺少操作类型参数");
            return;
        }
        
        PermissionDao permissionDao = new PermissionDao();
        
        try {
            switch (action) {
                case "addRole":
                    handleAddRole(request, response, permissionDao);
                    break;
                case "deleteRole":
                    handleDeleteRole(request, response, permissionDao);
                    break;
                case "addPermissionToRole":
                    handleAddPermissionToRole(request, response, permissionDao);
                    break;
                case "removePermissionFromRole":
                    handleRemovePermissionFromRole(request, response, permissionDao);
                    break;
                case "assignRoleToUser":
                    handleAssignRoleToUser(request, response, permissionDao);
                    break;
                default:
                    redirectWithError(request, response, "未知操作类型: " + action);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "参数格式错误", e);
            redirectWithError(request, response, "参数格式错误，请提供有效的数值");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "处理权限操作时发生错误", e);
            redirectWithError(request, response, "系统错误: " + e.getMessage());
        }
    }
    
    private void handleAddRole(HttpServletRequest request, HttpServletResponse response, PermissionDao dao) 
            throws IOException {
        String roleName = request.getParameter("roleName");
        String description = request.getParameter("description");
        
        if (roleName == null || roleName.trim().isEmpty()) {
            redirectWithError(request, response, "角色名称不能为空");
            return;
        }
        
        if (dao.addRole(roleName, description)) {
            redirectWithSuccess(request, response, "角色添加成功");
        } else {
            redirectWithError(request, response, "角色添加失败");
        }
    }
    
    private void handleDeleteRole(HttpServletRequest request, HttpServletResponse response, PermissionDao dao) 
            throws IOException, NumberFormatException {
        int roleId = Integer.parseInt(request.getParameter("roleId"));
        
        if (dao.deleteRole(roleId)) {
            redirectWithSuccess(request, response, "角色删除成功");
        } else {
            redirectWithError(request, response, "角色删除失败");
        }
    }
    
    private void handleAddPermissionToRole(HttpServletRequest request, HttpServletResponse response, PermissionDao dao) 
            throws IOException, NumberFormatException {
        int roleId = Integer.parseInt(request.getParameter("roleId"));
        int permissionId = Integer.parseInt(request.getParameter("permissionId"));
        
        if (dao.addPermissionToRole(roleId, permissionId)) {
            redirectWithSuccess(request, response, "权限添加到角色成功");
        } else {
            redirectWithError(request, response, "权限添加到角色失败");
        }
    }
    
    private void handleRemovePermissionFromRole(HttpServletRequest request, HttpServletResponse response, PermissionDao dao) 
            throws IOException, NumberFormatException {
        int roleId = Integer.parseInt(request.getParameter("roleId"));
        int permissionId = Integer.parseInt(request.getParameter("permissionId"));
        
        if (dao.removePermissionFromRole(roleId, permissionId)) {
            redirectWithSuccess(request, response, "权限从角色移除成功");
        } else {
            redirectWithError(request, response, "权限从角色移除失败");
        }
    }
    
    private void handleAssignRoleToUser(HttpServletRequest request, HttpServletResponse response, PermissionDao dao) 
            throws IOException, NumberFormatException {
        int userIdToAssign = Integer.parseInt(request.getParameter("userId"));
        int roleId = Integer.parseInt(request.getParameter("roleId"));
        
        if (dao.assignRoleToUser(userIdToAssign, roleId)) {
            redirectWithSuccess(request, response, "角色分配给用户成功");
        } else {
            redirectWithError(request, response, "角色分配给用户失败");
        }
    }
    
    private void redirectWithSuccess(HttpServletRequest request, HttpServletResponse response, String message) 
            throws IOException {
        request.getSession().setAttribute(SUCCESS_MSG, message);
        response.sendRedirect("PermissionServlet");
    }
    
    private void redirectWithError(HttpServletRequest request, HttpServletResponse response, String message) 
            throws IOException {
        request.getSession().setAttribute(ERROR_MSG, message);
        response.sendRedirect("PermissionServlet");
    }
}