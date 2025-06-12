package filter;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import Dao.LogDao;
import entity.OperationLog;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class LogFilter implements Filter {
    private LogDao logDao = new LogDao();

    // 操作类型常量定义（统一规范）
    private static final Map<String, String> OPERATION_TYPES = new HashMap<>();
    // 功能代码到中文描述的映射
    private static final Map<String, String> FUNCTION_DESCRIPTIONS = new HashMap<>();

    static {
        // 操作类型定义
        OPERATION_TYPES.put("LOGIN", "用户登录");
        OPERATION_TYPES.put("LOGOUT", "用户登出");
        OPERATION_TYPES.put("PAGE_VIEW", "页面访问");
        OPERATION_TYPES.put("API_GET", "API GET请求");
        OPERATION_TYPES.put("API_POST", "API POST请求");
        OPERATION_TYPES.put("DATA_SUBMIT", "数据提交");
        OPERATION_TYPES.put("RECORD_ADD", "添加记录");
        OPERATION_TYPES.put("RECORD_EDIT", "编辑记录");
        OPERATION_TYPES.put("RECORD_DELETE", "删除记录");
        OPERATION_TYPES.put("RECORD_QUERY", "查询记录");
        OPERATION_TYPES.put("PERMISSION_MANAGE", "权限管理");
        OPERATION_TYPES.put("LOG_VIEW", "查看日志");
        OPERATION_TYPES.put("USER_INFO_VIEW", "查看用户信息页面");
        OPERATION_TYPES.put("USER_INFO_UPDATE", "更新用户信息");

        // 功能代码映射
        FUNCTION_DESCRIPTIONS.put("function1", "添加记录");
        FUNCTION_DESCRIPTIONS.put("function2", "查询记录");
        FUNCTION_DESCRIPTIONS.put("function3", "显示记录");
        FUNCTION_DESCRIPTIONS.put("function4", "编辑记录");
        FUNCTION_DESCRIPTIONS.put("function5", "删除记录");
        FUNCTION_DESCRIPTIONS.put("function6", "统计分析");
        FUNCTION_DESCRIPTIONS.put("function7", "保存记录");
        FUNCTION_DESCRIPTIONS.put("function8", "查看日志");
        FUNCTION_DESCRIPTIONS.put("function9", "排序记录");
        FUNCTION_DESCRIPTIONS.put("function10", "权限管理");
        FUNCTION_DESCRIPTIONS.put("function11", "用户信息修改");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        String ipAddress = httpRequest.getRemoteAddr();

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        try {
            // 执行原始请求
            chain.doFilter(request, response);
        } finally {
            // 记录操作日志
            recordOperationLog(httpRequest, uri, method, ipAddress, startTime);
        }
    }

    private void recordOperationLog(HttpServletRequest request, String uri, String method, String ipAddress, long startTime) {
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");

        // 未登录用户不记录日志
        if (userId == null || username == null) {
            return;
        }

        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperationTime(new Timestamp(System.currentTimeMillis()));
        log.setIpAddress(ipAddress);
        log.setStatus("成功");

        // 计算执行时间
        long executionTime = System.currentTimeMillis() - startTime;
        log.setExecutionTime(executionTime);

        // 确定操作类型和描述
        String operationType = OPERATION_TYPES.get("PAGE_VIEW");
        String operationDesc = "访问页面: " + uri;

        // 根据URI和请求方法确定操作类型
        if (uri.contains("/LogServlet") || uri.contains("/function8.jsp")) {
            operationType = OPERATION_TYPES.get("LOG_VIEW");
            operationDesc = "查看系统操作日志";
        } else if (uri.contains("/LoginServlet") && "POST".equals(method)) {
            operationType = OPERATION_TYPES.get("LOGIN");
            operationDesc = "用户登录系统";
        } else if (uri.contains("/LogoutServlet")) {
            operationType = OPERATION_TYPES.get("LOGOUT");
            operationDesc = "用户退出系统";
        } else if (uri.contains("/FunctionServlet")) {
            String function = request.getParameter("function");
            if (function != null && FUNCTION_DESCRIPTIONS.containsKey(function)) {
                operationType = FUNCTION_DESCRIPTIONS.get(function);
                operationDesc = "访问了" + operationType + "功能";

                // 特殊功能类型映射
                if (function.equals("function8")) {
                    operationType = OPERATION_TYPES.get("LOG_VIEW");
                } else if (function.startsWith("function")) {
                    operationType = FUNCTION_DESCRIPTIONS.get(function);
                }
            }
        } else if (uri.contains("/addRecord")) {
            operationType = OPERATION_TYPES.get("RECORD_ADD");
            operationDesc = "添加了新的账务记录";
        } else if (uri.contains("/editRecord")) {
            operationType = OPERATION_TYPES.get("RECORD_EDIT");
            operationDesc = "编辑了账务记录";
        } else if (uri.contains("/deleteRecord")) {
            operationType = OPERATION_TYPES.get("RECORD_DELETE");
            operationDesc = "删除了账务记录";
        } else if (uri.contains("/queryRecord")) {
            operationType = OPERATION_TYPES.get("RECORD_QUERY");
            operationDesc = "查询账务记录";
        } else if (uri.contains("/permissionManager")) {
            operationType = OPERATION_TYPES.get("PERMISSION_MANAGE");
            operationDesc = "访问权限管理页面";
        } else if (uri.contains("/UserInfo.jsp")) {
            operationType = OPERATION_TYPES.get("USER_INFO_VIEW");
            operationDesc = "查看用户信息页面";
        } else if (uri.contains("/UpdateUserInfoServlet") && "POST".equals(method)) {
            operationType = OPERATION_TYPES.get("USER_INFO_UPDATE");
            operationDesc = "更新了用户信息";
        } else if ("GET".equals(method)) {
            operationType = OPERATION_TYPES.get("API_GET");
            operationDesc = "执行GET请求: " + uri;
        } else if ("POST".equals(method)) {
            operationType = OPERATION_TYPES.get("API_POST");
            operationDesc = "执行POST请求: " + uri;
        }

        log.setOperationType(operationType);
        log.setOperationDesc(operationDesc);

        // 保存日志
        logDao.addOperationLog(log);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}