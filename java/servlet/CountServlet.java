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
import java.util.Arrays;
import java.util.List;

@WebServlet("/CountServlet")
public class CountServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String statisticsType = request.getParameter("statisticsType");

        if (statisticsType == null || (!"generalLedger".equals(statisticsType) &&!"categoryLedger".equals(statisticsType)
                &&!"yearIncomeTotal".equals(statisticsType) &&!"yearExpenseTotal".equals(statisticsType)
                &&!"monthIncomeTotal".equals(statisticsType) &&!"monthExpenseTotal".equals(statisticsType))) {
            response.getWriter().println("无效的统计方式，请选择正确的统计方式进行操作");
            return;
        }

        String selectedCategoriesStr = request.getParameter("selectedCategories");
        List<String> selectedCategories = new ArrayList<>();
        if ("categoryLedger".equals(statisticsType) && selectedCategoriesStr!= null &&!selectedCategoriesStr.isEmpty()) {
            selectedCategories = Arrays.asList(selectedCategoriesStr.split(","));
            selectedCategories = selectedCategories.stream().map(String::trim).toList();
        }

        String year = request.getParameter("date");
        String month = request.getParameter("month");

       /* // 针对总账查询情况跳过年份参数校验
        if (!"generalLedger".equals(statisticsType)) {
            if (year == null || year.trim().isEmpty() ||!year.matches("\\d{4}")) {
                response.getWriter().println("年份输入不合法，请输入格式正确的四位年份（XXXX）");
                return;
            }
            if (month == null || month.trim().isEmpty() ||!month.matches("\\d{2}") || Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12) {
                response.getWriter().println("月份输入不合法，请输入格式正确的两位月份（XX，范围 01 - 12）");
                return;
            }
        }*/

        AccountRecordDao dao = new AccountRecordDao();
        try {
            if ("generalLedger".equals(statisticsType)) {
                double generalLedgerTotal = dao.getGeneralLedgerTotal();
                request.setAttribute("generalLedgerTotal", generalLedgerTotal);
                request.setAttribute("statisticsType", "总账");
            } else if ("categoryLedger".equals(statisticsType)) {
                List<AccountRecordDao.CategoryTotal> categoryLedgerTotals = dao.getCategoryLedgerTotals(selectedCategories);
                request.setAttribute("categoryLedgerTotals", categoryLedgerTotals);
                request.setAttribute("statisticsType", "分类账");
                request.setAttribute("selectedCategories", selectedCategories);
            } 

            request.getRequestDispatcher("function6result.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("统计数据时出现数据库异常，请稍后重试");
        }
    }
}