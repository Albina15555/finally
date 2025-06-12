package servlet;

import Dao.AccountRecordDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/GetRecordDetailServlet")
public class GetRecordDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 设置响应内容类型为HTML，编码为UTF-8，确保中文等字符能正确显示
        response.setContentType("text/html;charset=UTF-8");
        // 获取输出流对象，用于向客户端发送响应数据
        PrintWriter out = response.getWriter();

        // 从请求参数中获取要查询详情的记录ID
        String recordIdStr = request.getParameter("recordId");
        if (recordIdStr == null || recordIdStr.isEmpty()) {
            // 如果没有传入记录ID，返回错误提示信息给前端
            out.println("<h3>缺少记录ID参数，请检查请求</h3>");
            return;
        }

        int recordId = Integer.parseInt(recordIdStr);

        // 创建AccountRecordDao对象，用于操作数据库获取记录详情
        AccountRecordDao dao = new AccountRecordDao();
        try {
            // 调用AccountRecordDao的getRecordById方法获取指定记录ID的账务记录详情
            AccountRecordDao.AccountRecord record = dao.getRecordById(recordId);
            if (record!= null) {
                // 如果获取到记录详情，将其以表单元素的形式返回给前端，并且添加提交按钮，表单提交到EditRecordServlet
                out.println("<form id='recordDetailForm' action='EditRecordServlet' method='post'>");
                out.println("<input type='hidden' id='recordId' name='recordId' value='" + record.getRecordId() + "'>");
                out.println("<label for='date'>日期：</label><input type='text' id='date' name='date' value='" + record.getDate() + "'>");
                out.println("<label for='type'>类型：</label><input type='text' id='type' name='type' value='" + record.getType() + "'>");
                out.println("<label for='amount'>金额：</label><input type='text' id='amount' name='amount' value='" + record.getAmount() + "'>");
                out.println("<label for='category'>项目类别：</label><input type='text' id='category' name='category' value='" + record.getCategory() + "'>");
                out.println("<label for='remark'>备注：</label><input type='text' id='remark' name='remark' value='" + record.getRemark() + "'>");
                out.println("<input type='submit' value='保存修改'>");
                out.println("</form>");
            } else {
                // 如果没有获取到对应记录，返回相应提示信息给前端
                out.println("<h3>未找到对应记录ID的账务记录，请检查记录ID是否正确</h3>");
            }
        } catch (SQLException e) {
            // 如果出现SQLException异常，记录异常信息（此处可根据实际情况进行更完善的日志记录）
            e.printStackTrace();
            out.println("<h3>数据库操作出现异常，获取记录详情失败</h3>");
        }
    }
}