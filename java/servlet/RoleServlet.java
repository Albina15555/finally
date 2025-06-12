package servlet;

import Dao.RoleDao;
import entity.Role;
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

@WebServlet("/RoleServlet")
public class RoleServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(RoleServlet.class.getName());
    private static final String SUCCESS_MSG = "success";
    private static final String ERROR_MSG = "error";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleRequest(request, response);
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        // 检查用户是否登录
        if (userId == null) {
            redirectWithError(request, response, "请先登录");
            return;
        }

        // 检查用户是否有修改角色的权限
        if (!PermissionChecker.hasPermission(session, "function10")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "您没有修改角色的权限");
            return;
        }

        String action = request.getParameter("action");
        RoleDao roleDao = new RoleDao();

        try {
            switch (action) {
                case "delete":
                    handleDeleteRole(request, response, roleDao);
                    break;
                case "add":
                    handleAddRole(request, response, roleDao);
                    break;
                default:
                    redirectWithError(request, response, "未知操作类型: " + action);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "参数格式错误", e);
            redirectWithError(request, response, "参数格式错误，请提供有效的数值");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "处理角色操作时发生错误", e);
            redirectWithError(request, response, "系统错误: " + e.getMessage());
        }
    }

    private void handleDeleteRole(HttpServletRequest request, HttpServletResponse response, RoleDao roleDao)
            throws IOException, NumberFormatException {
        int roleId = Integer.parseInt(request.getParameter("role_id"));
        if (roleDao.deleteRole(roleId)) {
            redirectWithSuccess(request, response, "角色删除成功");
        } else {
            redirectWithError(request, response, "角色删除失败");
        }
    }

    private void handleAddRole(HttpServletRequest request, HttpServletResponse response, RoleDao roleDao)
            throws IOException {
        String roleName = request.getParameter("role_name");
        String description = request.getParameter("description");

        if (roleName == null || roleName.trim().isEmpty()) {
            redirectWithError(request, response, "角色名称不能为空");
            return;
        }

        Role role = new Role();
        role.setRoleName(roleName);
        role.setDescription(description);

        if (roleDao.addRole(role)) {
            redirectWithSuccess(request, response, "角色添加成功");
        } else {
            redirectWithError(request, response, "角色添加失败");
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