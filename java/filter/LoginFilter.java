package filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class LoginFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession();
        
        // 白名单路径（无需登录即可访问）
        String[] whiteList = {
            "/login.jsp", "/LoginServlet", "/register.jsp", "/RegisterServlet",
            "/css/", "/js/", "/images/", "/fonts/"
        };
        
        String path = req.getRequestURI();
        boolean isWhiteList = false;
        
        for (String url : whiteList) {
            if (path.startsWith(url)) {
                isWhiteList = true;
                break;
            }
        }
        
        // 白名单路径直接放行
        if (isWhiteList) {
            chain.doFilter(request, response);
            return;
        }
        
        // 检查用户是否已登录
        if (session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        } else {
            chain.doFilter(request, response);
        }
    }
    
    @Override
    public void init(FilterConfig config) throws ServletException {}
    
    @Override
    public void destroy() {}
}