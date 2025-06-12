package servlet;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//@WebServlet("/FunctionServlet")
public class functionservlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String function = request.getParameter("function");
        if ("function1".equals(function)) {
            // 处理功能 1 的逻辑，比如跳转到相应页面或执行其他操作
            response.sendRedirect("function1.jsp");
        } else if ("function2".equals(function)) {
            response.sendRedirect("function2.jsp");
        } else if ("function3".equals(function)) {
            response.sendRedirect("DisplayServlet");
        } else if ("function4".equals(function)) {
            response.sendRedirect("function4.jsp");
        } else if ("function5".equals(function)) {
            response.sendRedirect("function5.jsp");
        } else if ("function6".equals(function)) {
            response.sendRedirect("function6.jsp");
        }
        else if ("function7".equals(function)) {
            response.sendRedirect("function7.jsp");
        }
        else if ("function8".equals(function)) {
            response.sendRedirect("function8.jsp");
        }
        else if ("function9".equals(function)) {
            response.sendRedirect("function9.jsp");
        }
    }
}