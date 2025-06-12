package servlet;

import Dao.AccountRecordDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/DeleteRecordServlet")
public class DeleteRecordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int recordId = Integer.parseInt(request.getParameter("recordId"));
        AccountRecordDao dao = new AccountRecordDao();
        try {
            boolean deleteSuccess = dao.deleteRecord(recordId);
            if (deleteSuccess) {
                // 查询所有账务记录（也可根据实际需求调整查询逻辑）
                List<AccountRecordDao.AccountRecord> updatedRecordList = dao.queryAll();
                // 将记录列表设置到请求属性中，以便在目标页面获取
                request.setAttribute("recordList", updatedRecordList);
                // 转发到function4result.jsp
                request.getRequestDispatcher("function4result.jsp").forward(request, response);
            } else {
                // 如果删除失败，设置一个失败标志到请求属性中
                request.setAttribute("deleteFailed", true);
                // 同样转发到function4result.jsp，以便统一处理
                request.getRequestDispatcher("function4result.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // 如果出现异常，设置一个异常标志到请求属性中
            request.setAttribute("databaseError", true);
            // 转发到function4result.jsp，以便统一处理异常情况
            request.getRequestDispatcher("function4result.jsp").forward(request, response);
        }
    }
}