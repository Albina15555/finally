package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import Dao.AccountRecordDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 配置该Servlet的访问路径为 /DisplayAllRecordsServlet
@WebServlet("/DisplayAllRecordsServlet")
public class DisplayServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AccountRecordDao accountRecordDao = new AccountRecordDao();
        try {
            // 调用AccountRecordDao中的方法查询所有账务记录，这里使用查询项目名称为空字符串的方式来模拟查询所有记录（当前是基于type字段筛选）
            List<AccountRecordDao.AccountRecord> recordList = accountRecordDao.queryByProject("");
            request.setAttribute("recordList", recordList);
            request.getRequestDispatcher("function3result.jsp").forward(request, response);
        } finally {
            // 此处不需要再调用AccountRecordDao类中的关闭方法，因为在AccountRecordDao的queryByProject方法的finally块里已经做了关闭连接资源的操作
            // 所以这里不需要重复关闭操作，避免出现异常（比如重复关闭连接可能导致的问题等）
        }
    }
}