package servlet;

import Dao.PermissionDao;
import Dao.UserDao;
import entity.Permission;
import entity.Role;
import entity.User;
import filter.PermissionChecker;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        // 处理未登录情况
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        
        PermissionDao permissionDao = new PermissionDao();
        UserDao userDao = new UserDao();
        
        // 检查用户是否为管理员
        boolean isAdmin = PermissionChecker.hasPermission(session, "function10");
        
        // 获取所有权限
        List<Permission> userPerms;
        if (isAdmin) {
            // 管理员获取所有用户的权限
            userPerms = permissionDao.getAllUserPermissions();
            System.out.println("管理员获取了所有用户的权限");
        } else {
            // 非管理员仅获取自身权限
            userPerms = permissionDao.getUserRolePermissions(userId);
            System.out.println("非管理员获取了自身权限");
        }
        
        // 获取所有用户的角色映射（仅管理员需要）
        Map<Integer, Map<String, String>> userRoles = new HashMap<>();
        if (isAdmin) {
            userRoles = userDao.getAllUserRoles();
            // 输出用户角色数据，检查是否正确
            System.out.println("用户角色数据: " + userRoles); 
        }
        
        // 获取成功和错误消息参数 (修复了变量名)
        String success = req.getParameter("success"); // 使用 req 而不是 request
        String error = req.getParameter("error");    // 使用 req 而不是 request
        
        if (success != null) {
            req.setAttribute("successMessage", success);
        }
        
        if (error != null) {
            req.setAttribute("errorMessage", error);
        }
        
        // 获取所有用户信息
        List<User> allUsers = userDao.getAllUsers();
        Map<Role, List<Permission>> rolePermissionsMap = permissionDao.getAllRolesWithPermissions();
        req.setAttribute("rolePermissionsMap", rolePermissionsMap);
        // 设置请求属性
        req.setAttribute("userPerms", userPerms);
        req.setAttribute("userRoles", userRoles);
        req.setAttribute("allRoles", permissionDao.getAllRoles());
        req.setAttribute("allPermissions", permissionDao.getAllPermissions());
        req.setAttribute("allUsers", allUsers);
        
        // 转发到权限管理页面
        req.getRequestDispatcher("/function10.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("assignPermissionsToRole".equals(action)) {
            handleAssignPermissionsToRole(req, resp);
        } else if ("assignRoleToUser".equals(action)) {
            handleAssignRoleToUser(req, resp);
        } else {
            doGet(req, resp);
        }
    }

    private void handleAssignPermissionsToRole(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        // 检查用户是否登录
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        // 检查用户是否有分配权限的权限
        if (!PermissionChecker.hasPermission(session, "function10")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "您没有分配权限的权限");
            return;
        }

        try {
            // 获取表单参数
            int roleId = Integer.parseInt(req.getParameter("role_id"));
            String[] permissionIds = req.getParameterValues("permissions");

            PermissionDao permissionDao = new PermissionDao();
            boolean success = false;

            if (permissionIds != null && permissionIds.length > 0) {
                int[] perms = new int[permissionIds.length];
                for (int i = 0; i < permissionIds.length; i++) {
                    perms[i] = Integer.parseInt(permissionIds[i]);
                }
                // 调用 PermissionDao 中的方法将权限分配给角色
                success = permissionDao.addPermissionsToRole(roleId, perms);
            }

            if (success) {
                // 重定向回权限管理页面，带成功消息
                session.removeAttribute("userRoles"); // 清除会话中的用户角色数据
                resp.sendRedirect("PermissionServlet?success=权限分配给角色成功");
            } else {
                // 重定向回权限管理页面，带错误消息
                resp.sendRedirect("PermissionServlet?error=权限分配给角色失败");
            }
        } catch (NumberFormatException e) {
            resp.sendRedirect("PermissionServlet?error=无效的参数格式");
        } catch (Exception e) {
            resp.sendRedirect("PermissionServlet?error=系统错误：" + e.getMessage());
        }
    }

    private void handleAssignRoleToUser(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        // 检查用户是否登录
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        // 检查用户是否有分配角色的权限
        if (!PermissionChecker.hasPermission(session, "function10")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "您没有分配角色的权限");
            return;
        }

        try {
            // 获取表单参数
            int targetUserId = Integer.parseInt(req.getParameter("user_id"));
            int roleId = Integer.parseInt(req.getParameter("role_id"));

            PermissionDao permissionDao = new PermissionDao();
            boolean success = permissionDao.assignRoleToUser(targetUserId, roleId);

            if (success) {
                // 重定向回权限管理页面，带成功消息
                session.removeAttribute("userRoles"); // 清除会话中的用户角色数据
                resp.sendRedirect("PermissionServlet?success=角色分配给用户成功");
            } else {
                // 重定向回权限管理页面，带错误消息
                resp.sendRedirect("PermissionServlet?error=角色分配给用户失败");
            }
        } catch (NumberFormatException e) {
            resp.sendRedirect("PermissionServlet?error=无效的参数格式");
        } catch (Exception e) {
            resp.sendRedirect("PermissionServlet?error=系统错误：" + e.getMessage());
        }
    }
}