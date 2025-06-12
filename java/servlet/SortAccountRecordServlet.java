package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import Dao.AccountRecordDao;
import Dao.AccountRecordDao.AccountRecord;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/SortAccountRecordServlet")
public class SortAccountRecordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 获取用户选择的要排序的字段
        String sortField = request.getParameter("sortField");
        // 获取用户选择的排序顺序
        String sortOrder = request.getParameter("sortOrder");

        AccountRecordDao dao = new AccountRecordDao();
        List<AccountRecord> allRecords;
        try {
            allRecords = dao.queryAll();
        } catch (SQLException e) {
            request.setAttribute("error", "获取账务记录用于排序时出现数据库异常，请稍后重试。");
            request.getRequestDispatcher("function9result.jsp").forward(request, response);
            return;
        }

        // 根据选择的排序字段进行排序操作，并处理可能出现的null值情况
        if ("user_id".equals(sortField)) {
            if ("asc".equals(sortOrder)) {
                // 使用nullsFirst将null值排在前面（升序时），按照用户ID排序，可根据业务需求调整为nullsLast等其他方式
                Collections.sort(allRecords, Comparator.comparing(AccountRecord::getUserId, Comparator.nullsFirst(String::compareTo)));
            } else {
                // 降序时同样处理null值情况，先按用户ID排序（nullsFirst），再反转顺序实现降序排列
                Collections.sort(allRecords, Comparator.comparing(AccountRecord::getUserId, Comparator.nullsFirst(String::compareTo)).reversed());
            }
        } else if ("date".equals(sortField)) {
            if ("asc".equals(sortOrder)) {
                // 假设date字段是String类型存储日期，这里使用nullsFirst处理null值，按日期升序排序，若date字段实际为其他日期类型（如LocalDate等）需相应调整比较逻辑
                Collections.sort(allRecords, Comparator.comparing(AccountRecord::getDate, Comparator.nullsFirst(String::compareTo)));
            } else {
                Collections.sort(allRecords, Comparator.comparing(AccountRecord::getDate, Comparator.nullsFirst(String::compareTo)).reversed());
            }
        } else if ("type".equals(sortField)) {
            if ("asc".equals(sortOrder)) {
                // 处理记录类型字段的null值情况（升序排序），同样可根据业务决定nullsFirst还是nullsLast等处理方式
                Collections.sort(allRecords, Comparator.comparing(AccountRecord::getType, Comparator.nullsFirst(String::compareTo)));
            } else {
                Collections.sort(allRecords, Comparator.comparing(AccountRecord::getType, Comparator.nullsFirst(String::compareTo)).reversed());
            }
        } else if ("amount".equals(sortField)) {
            if ("asc".equals(sortOrder)) {
                Collections.sort(allRecords, Comparator.comparingDouble(record -> {
                    Double amount = record.getAmount();  // 将double类型的amount字段值转换为Double对象
                    return amount == null? 0.0 : amount;  // 再进行和null的比较判断，根据情况返回默认值或实际值
                }));
            } else {
                Collections.sort(allRecords, Comparator.comparingDouble(record -> {
                    Double amount = ((AccountRecord) record).getAmount();
                    return amount == null? 0.0 : amount;
                }).reversed());
            }
        }
        // 将排序后的账务记录列表设置到请求属性中，以便传递给JSP页面展示
        request.setAttribute("sortedRecords", allRecords);

        // 转发请求到展示排序结果的JSP页面（function9result.jsp）
        request.getRequestDispatcher("function9result.jsp").forward(request, response);
    }
}