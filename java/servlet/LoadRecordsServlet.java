package servlet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import Dao.AccountRecordDao;
import Dao.AccountRecordDao.AccountRecord;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@WebServlet("/LoadRecordsServlet")
// 这里不需要再次导入自己所在的类（Dao.AccountRecordDao），会造成循环依赖等问题，原导入多余，已移除
public class LoadRecordsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(LoadRecordsServlet.class.getName());
    // 假设此处文件路径固定，如果需要灵活配置可考虑其他方式，比如配置文件读取等
    private static final String FILE_PATH = "records.txt";  

    // 从文件读取账务记录的方法，参数 filePath 为文件路径，这里也可以考虑将FILE_PATH作为默认值，外部可传入其他路径覆盖默认值，更灵活些
    public List<AccountRecord> readRecordsFromFile(String filePath) {
        List<AccountRecord> recordList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 跳过文件头行（假设文件第一行是标题行，格式如 "record_id,user_id,date,type,amount,category,remark"）
            reader.readLine();
            String line;
            while ((line = reader.readLine())!= null) {
                String[] fields = line.split(",");
                if (fields.length == 7) {
                    AccountRecord record = new AccountRecord();
                    record.setRecordId(Integer.parseInt(fields[0]));
                     // 这里假设recordId在数据库中是字符串类型存储，如果是int类型需转换，可参考之前AccountRecordDao里的处理方式
                    record.setUserId(fields[1]);
                    record.setDate(fields[2]);
                    record.setType(fields[3]);
                    record.setAmount(Double.parseDouble(fields[4]));
                    record.setCategory(fields[5]);
                    record.setRemark(fields[6]);
                    recordList.add(record);
                } else {
                    LOGGER.log(Level.WARNING, "文件中某行记录格式不符合要求，跳过该行：" + line);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "读取文件记录时出现IO异常", e);
        }
        return recordList;
    }
    public class SaveRecordsServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            // 获取前端传递的文件保存路径参数
            String filePath = request.getParameter("savePath");
            if (filePath == null || filePath.trim().isEmpty()) {
                response.getWriter().println("未指定有效的保存路径，将使用默认路径保存，若要自主选择路径请重新输入。");
            }
            AccountRecordDao dao = new AccountRecordDao();
            dao.saveRecordsToFile(filePath, response);
        }

    // 获取数据库连接的方法（用于可能的数据库相关操作，此处保留原有逻辑，暂未详细扩展），不过在这个类里目前没看到使用数据库连接的必要，可根据实际情况决定是否保留此方法
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "加载数据库驱动失败", e);
            throw new SQLException("加载数据库驱动失败", e);
        }
        return DriverManager.getConnection("jdbc:sqlite:D:/大学/大二/大二上/课程/java web/java web/11.1/jxgl.db");
    }
}}