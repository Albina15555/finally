package servlet;

import Dao.LogDao;
import entity.OperationLog;
import filter.PermissionChecker;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/LogServlet")
public class LogServlet extends HttpServlet {
    private LogDao logDao = new LogDao();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        
        // 检查权限
        if (!PermissionChecker.hasPermission(session, "function8")) {
            resp.sendRedirect(req.getContextPath() + "/noPermission.jsp");
            return;
        }
        
        // 获取查询参数
        String type = req.getParameter("type");
        String startTime = req.getParameter("startTime");
        String endTime = req.getParameter("endTime");
        
        // 查询日志（按时间倒序）
        List<OperationLog> logs = logDao.queryOperationLogs(type, startTime, endTime);
        
        // 传递数据到视图
        req.setAttribute("logs", logs);
        req.setAttribute("type", type);
        req.setAttribute("startTime", startTime);
        req.setAttribute("endTime", endTime);
        
        // 转发到日志列表页面
        req.getRequestDispatcher("/function8.jsp").forward(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}