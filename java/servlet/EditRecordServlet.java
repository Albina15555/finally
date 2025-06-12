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

@WebServlet("/EditRecordServlet")
public class EditRecordServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 设置响应内容类型为HTML，编码为UTF-8，确保中文等字符能正确显示
        response.setContentType("text/html;charset=UTF-8");

        // 从前端页面提交的表单数据中获取各个字段的值
        int recordId = Integer.parseInt(request.getParameter("recordId"));
        String date = request.getParameter("date");
        String type = request.getParameter("type");
        double amount = Double.parseDouble(request.getParameter("amount"));
        String category = request.getParameter("category");
        String remark = request.getParameter("remark");

        // 创建AccountRecordDao对象，用于操作数据库
        AccountRecordDao dao = new AccountRecordDao();
        // 创建一个AccountRecord对象，用于封装要更新的数据
        AccountRecordDao.AccountRecord record = new AccountRecordDao.AccountRecord();
        record.setRecordId(recordId);
        record.setDate(date);
        record.setType(type);
        record.setAmount(amount);
        record.setCategory(category);
        record.setRemark(remark);

        try {
            // 调用AccountRecordDao的updateRecord方法来更新数据库中的记录
            boolean isUpdated = dao.updateRecord(record);
            if (isUpdated) {
                // 如果更新成功，查询所有账务记录（你也可以根据实际需求只查询当前编辑记录所在的相关记录等）
                List<AccountRecordDao.AccountRecord> updatedRecordList = dao.queryAll();
                // 将查询到的记录列表添加到request作用域中，键名为"recordList"
                request.setAttribute("recordList", updatedRecordList);
                // 设置提示信息，表示编辑成功
                request.setAttribute("message", "编辑记录成功，以下是所有账务记录数据");
                // 转发到function4result.jsp页面进行展示，使用转发可以保持request作用域中的数据
                request.getRequestDispatcher("function4result.jsp").forward(request, response);
            } else {
                // 如果更新失败，设置相应的错误提示信息到request作用域中
                request.setAttribute("errorMessage", "更新记录失败，请检查数据是否合法或联系管理员");
                // 转发到function4result.jsp页面进行展示，这样可以统一在该页面处理成功和失败的提示展示
                request.getRequestDispatcher("function4result.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            // 如果出现SQLException异常，记录异常信息（此处可根据实际情况进行更完善的日志记录）
            e.printStackTrace();
            // 设置数据库操作异常的提示信息到request作用域中
            request.setAttribute("errorMessage", "数据库操作出现异常，编辑记录失败");
            // 转发到function4result.jsp页面进行展示，统一展示异常相关提示
            request.getRequestDispatcher("function4result.jsp").forward(request, response);
        }
    }
}