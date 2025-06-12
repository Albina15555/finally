package servlet;

import Dao.AccountRecordDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//@WebServlet("/QueryRecordServlet")
public class QueryRecordServlet extends HttpServlet {
	

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取查询参数
        String conditionType = request.getParameter("conditionType"); // AND 或 OR
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String[] recordTypes = request.getParameterValues("recordType");
        String project = request.getParameter("project");
        String minAmountStr = request.getParameter("minAmount");
        String maxAmountStr = request.getParameter("maxAmount");
        
        // 创建参数列表
        AccountRecordDao accountRecordDao = new AccountRecordDao();
        try {
            // 解析金额参数
            Double minAmount = null;
            Double maxAmount = null;
            
            if (minAmountStr != null && !minAmountStr.isEmpty()) {
                try {
                    minAmount = Double.parseDouble(minAmountStr);
                } catch (NumberFormatException e) {
                    // 处理无效的金额格式
                    request.setAttribute("error", "最小金额格式无效");
                    request.getRequestDispatcher("function2result1.jsp").forward(request, response);
                    return;
                }
            }
            
            if (maxAmountStr != null && !maxAmountStr.isEmpty()) {
                try {
                    maxAmount = Double.parseDouble(maxAmountStr);
                } catch (NumberFormatException e) {
                    // 处理无效的金额格式
                    request.setAttribute("error", "最大金额格式无效");
                    request.getRequestDispatcher("function2result1.jsp").forward(request, response);
                    return;
                }
            }
            
            // 调用存储过程方法
            List<AccountRecordDao.AccountRecord> recordList = 
                accountRecordDao.queryByCustomWithSP(
                    conditionType, 
                    startTime, 
                    endTime, 
                    recordTypes, 
                    project, 
                    minAmount, 
                    maxAmount
                );
            
            // 保存查询参数
            request.setAttribute("conditionType", conditionType);
            request.setAttribute("startTime", startTime);
            request.setAttribute("endTime", endTime);
            request.setAttribute("recordTypes", recordTypes);
            request.setAttribute("project", project);
            request.setAttribute("minAmount", minAmountStr);
            request.setAttribute("maxAmount", maxAmountStr);
            
            request.setAttribute("recordList", recordList);
            request.getRequestDispatcher("function2result1.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "数据库查询错误");
        }
        }
    }
