package servlet;

import Dao.AccountRecordDao;
import Dao.AccountRecordDao.AccountRecord;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

//@WebServlet("/AddRecordServlet")
public class AddRecordServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AddRecordServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 一般情况下，添加记录的操作通过POST方式提交，所以这里可以简单返回一个错误提示或者直接重定向到添加页面等
        response.getWriter().println("请使用POST方法提交添加账务记录的请求");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 从会话中获取用户名，假设登录成功后将用户名存放在名为"username"的会话属性中，你需要根据实际情况调整
        HttpSession session = request.getSession(false);
        String username = (String) session.getAttribute("username");
        if (username == null || username.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("无法获取当前登录用户名，无法添加账务记录");
            return;
        }

        // 获取前端页面提交过来的数据
        String UserId = request.getParameter("username");
        String date = request.getParameter("date");
        String type = request.getParameter("type");
        String amountStr = request.getParameter("amount");
        String category = request.getParameter("category");
        String remark = request.getParameter("remark");

        // 进行必要的参数验证，这里可以更详细完善验证逻辑，目前简单验证非空等情况
        if (date == null || date.isEmpty() || type == null || type.isEmpty() || amountStr == null || amountStr.isEmpty() || category == null || category.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("请确保所有必填字段都已填写完整");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("金额字段必须是合法的数字格式");
            return;
        }

        // 创建AccountRecord对象，用于封装要添加的记录信息，设置获取到的用户名
        AccountRecord record = new AccountRecord();
        record.setUserId(username);
        record.setDate(date);
        record.setType(type);
        record.setAmount(amount);
        record.setCategory(category);
        record.setRemark(remark);

        // 使用AccountRecordDao来操作数据库，添加记录
        AccountRecordDao recordDao = new AccountRecordDao();
        try {
            recordDao.addRecord(record);
            // 添加成功后，将添加成功的记录再次查询出来（假设记录有自增的主键id，这里获取刚插入记录的id来查询，你可以根据实际表结构调整）
            int recordId = getLastInsertedRecordId(recordDao);
            AccountRecord insertedRecord = recordDao.getRecordById(recordId);
            // 将查询到的记录存放在请求属性中，以便在展示页面能获取到并展示详情
            request.setAttribute("record", insertedRecord);
            // 转发到展示记录详情的页面（假设名为function1result.jsp，你可根据实际情况调整）
            request.getRequestDispatcher("function1result.jsp").forward(request, response);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "添加账务记录时出现数据库异常", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("添加账务记录时出现数据库异常，请稍后重试");
        }
    }

    // 辅助方法，用于获取刚插入记录的主键id（假设数据库表中记录有自增主键，且支持通过这种方式获取，比如MySQL可以使用LAST_INSERT_ID()函数，这里简单模拟，你需根据实际数据库调整）
    private int getLastInsertedRecordId(AccountRecordDao recordDao) throws SQLException {
        // 这里只是简单返回一个固定值示例，实际要编写正确的查询刚插入记录id的逻辑
        return 1;
    }
}