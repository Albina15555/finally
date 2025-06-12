package servlet;
import Dao.AccountRecordDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/saveRecords")
public class SaveRecordsServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AccountRecordDao dao = new AccountRecordDao();
        // 指定保存文件的路径，这里示例为项目根目录下的一个名为 records.txt 的文件，你可根据实际情况修改路径
        String filePath = "records.txt";
        dao.saveRecordsToFile(filePath, response);
    }
}