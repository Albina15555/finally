package Dao;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import utils.DBConnection;

public class AccountRecordDao {

    public void saveRecordsToFile(String filePath, HttpServletResponse response) {
        List<AccountRecord> allRecords = null;
        try {
            allRecords = queryAll();
        } catch (SQLException e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("查询记录用于保存时出现数据库异常，请稍后重试。");
            } catch (IOException ex) {
            }
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("record_id,user_id,date,type,amount,category,remark\n");
            for (AccountRecord record : allRecords) {
                writer.write(record.getRecordId() + "," +
                        record.getUserId() + "," +
                        record.getDate() + "," +
                        record.getType() + "," +
                        record.getAmount() + "," +
                        record.getCategory() + "," +
                        record.getRemark() + "\n");
            }
            // 保存成功后设置响应相关信息
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("保存记录到文件时出现IO异常，请稍后重试。");
            } catch (IOException ex) {
            }
        }
    }

    public double getGeneralLedgerTotal() throws SQLException {
        double total = 0;
        String sql = "SELECT SUM(amount) AS total_amount FROM account_record_table";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                total = rs.getDouble("total_amount");
            }
        } catch (SQLException e) {
            System.err.println("统计总账时出现数据库异常: " + e.getMessage());
            throw e;
        }
        return total;
    }

    // 统计分类账的方法，根据传入的项目类别列表统计对应金额总和，返回一个包含类别和对应金额总和的列表
    public List<CategoryTotal> getCategoryLedgerTotals(List<String> selectedCategories) throws SQLException {
        Map<String, Double> categoryAmountMap = new HashMap<>();
        if (selectedCategories == null || selectedCategories.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder sqlBuilder = new StringBuilder("SELECT category, amount FROM account_record_table WHERE category IN (");
        for (int i = 0; i < selectedCategories.size(); i++) {
            sqlBuilder.append("?");
            if (i < selectedCategories.size() - 1) {
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.append(")");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < selectedCategories.size(); i++) {
                stmt.setString(i + 1, selectedCategories.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("category");
                    double amount = rs.getDouble("amount");
                    categoryAmountMap.put(category, categoryAmountMap.getOrDefault(category, 0.0) + amount);
                }
            }
        } catch (SQLException e) {
            System.err.println("统计分类账时出现数据库异常: " + e.getMessage());
            throw e;
        }

        List<CategoryTotal> categoryTotals = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryAmountMap.entrySet()) {
            CategoryTotal categoryTotal = new CategoryTotal();
            categoryTotal.setCategory(entry.getKey());
            categoryTotal.setTotalAmount(entry.getValue());
            categoryTotals.add(categoryTotal);
        }

        return categoryTotals;
    }

    public static class CategoryTotal {
        private String category;
        private double totalAmount;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
        }
    }

    public boolean updateRecord(AccountRecord record) throws SQLException {
        String sql = "UPDATE account_record_table SET date =?, type =?, amount =?, category =?, remark =? WHERE record_id =?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, record.getDate());
            preparedStatement.setString(2, record.getType());
            preparedStatement.setDouble(3, record.getAmount());
            preparedStatement.setString(4, record.getCategory());
            preparedStatement.setString(5, record.getRemark());
            preparedStatement.setInt(6, record.getRecordId());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw e;
        }
    }

    // 根据日期范围查询账务记录
    public List<AccountRecord> queryByDateRange(String startDate, String endDate) {
        List<AccountRecord> recordList = new ArrayList<>();
        String sql = "SELECT record_id, user_id, date, type, amount, category, remark FROM account_record_table WHERE date BETWEEN? AND?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, startDate);
            preparedStatement.setString(2, endDate);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    AccountRecord record = new AccountRecord();
                    record.setRecordId(rs.getInt("record_id"));
                    record.setUserId(rs.getString("user_id"));
                    record.setDate(rs.getString("date"));
                    record.setType(rs.getString("type"));
                    record.setAmount(rs.getDouble("amount"));
                    record.setCategory(rs.getString("category"));
                    record.setRemark(rs.getString("remark"));
                    recordList.add(record);
                }
            }
        } catch (SQLException e) {
        }
        return recordList;
    }

    // 内部类，用于封装账务记录的实体信息，方便数据传递和操作
    public static class AccountRecord {
        private int recordId;
        private String userId;
        private String date;
        private String type;
        private double amount;
        private String category;
        private String remark;

        // 以下是各个属性的Getter和Setter方法
        public int getRecordId() {
            return recordId;
        }

        public void setRecordId(int recordId) {
            this.recordId = recordId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

    }

    // 按时间查询账务记录的方法
    public List<AccountRecord> queryByTime(String time) {
        List<AccountRecord> resultList = new ArrayList<>();
        String sql = "SELECT record_id, user_id, date, type, amount, category, remark FROM account_record_table WHERE date =?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, time);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    AccountRecord record = new AccountRecord();
                    record.setRecordId(rs.getInt("recordId"));
                    record.setUserId(rs.getString("user_id"));
                    record.setDate(rs.getString("date"));
                    record.setType(rs.getString("type"));
                    record.setAmount(rs.getDouble("amount"));
                    record.setCategory(rs.getString("category"));
                    record.setRemark(rs.getString("remark"));
                    resultList.add(record);
                }
            }
        } catch (SQLException e) {
        }
        return resultList;
    }

    // 按时间区间查询账务记录的方法
    public List<AccountRecord> queryByTimeRange(String startTime, String endTime) {
        List<AccountRecord> resultList = new ArrayList<>();
        String sql = "SELECT record_id, user_id, date, type, amount, category, remark FROM account_record_table WHERE date BETWEEN? AND?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, startTime);
            preparedStatement.setString(2, endTime);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    AccountRecord record = new AccountRecord();
                    record.setRecordId(rs.getInt("record_id"));
                    record.setUserId(rs.getString("user_id"));
                    record.setDate(rs.getString("date"));
                    record.setType(rs.getString("type"));
                    record.setAmount(rs.getDouble("amount"));
                    record.setCategory(rs.getString("category"));
                    record.setRemark(rs.getString("remark"));
                    resultList.add(record);
                }
            }
        } catch (SQLException e) {
        }
        return resultList;
    }

    // 按记录类型查询账务记录的方法（可多选）
    public List<AccountRecord> queryByType(String[] recordTypeArray) {
        List<AccountRecord> resultList = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT record_id, user_id, date, type, amount, category, remark FROM account_record_table WHERE type IN (");
        if (recordTypeArray != null && recordTypeArray.length > 0) {
            for (int i = 0; i < recordTypeArray.length; i++) {
                sql.append("?");
                if (i < recordTypeArray.length - 1) {
                    sql.append(",");
                }
            }
            sql.append(")");
        } else {
            sql.append("''");
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
            if (recordTypeArray != null && recordTypeArray.length > 0) {
                for (int i = 0, len = recordTypeArray.length; i < len; i++) {
                    preparedStatement.setString(i + 1, recordTypeArray[i]);
                }
            }
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    AccountRecord record = new AccountRecord();
                    record.setRecordId(rs.getInt("record_id"));
                    record.setUserId(rs.getString("user_id"));
                    record.setDate(rs.getString("date"));
                    record.setType(rs.getString("type"));
                    record.setAmount(rs.getDouble("amount"));
                    record.setCategory(rs.getString("category"));
                    record.setRemark(rs.getString("remark"));
                    resultList.add(record);
                }
            }
        } catch (SQLException e) {
        }
        return resultList;
    }

    public void addRecord(AccountRecord record) {
        String sql = "INSERT INTO account_record_table (user_id, date, type, amount, category, remark) VALUES (?,?,?,?,?,?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, record.getUserId());
            preparedStatement.setString(2, record.getDate());
            preparedStatement.setString(3, record.getType());
            preparedStatement.setDouble(4, record.getAmount());
            preparedStatement.setString(5, record.getCategory());
            preparedStatement.setString(6, record.getRemark());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
        }
    }

    // 按项目查询账务记录的方法（模糊查询，此处按照前面假设基于category字段模糊查询项目进行修正，确保代码语法正确）
    public List<AccountRecord> queryByProject(String project) {
        List<AccountRecord> resultList = new ArrayList<>();
        String sql = "SELECT record_id, user_id, date, type, amount, category, remark FROM account_record_table WHERE category LIKE?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "%" + project + "%");
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    AccountRecord record = new AccountRecord();
                    record.setRecordId(rs.getInt("record_id"));
                    record.setUserId(rs.getString("user_id"));
                    record.setDate(rs.getString("date"));
                    record.setType(rs.getString("type"));
                    record.setAmount(rs.getDouble("amount"));
                    record.setCategory(rs.getString("category"));
                    record.setRemark(rs.getString("remark"));
                    resultList.add(record);
                }
            }
        } catch (SQLException e) {
        }
        return resultList;
    }

    // 查询所有账务记录的方法
    public List<AccountRecord> queryAll() throws SQLException {
        List<AccountRecord> resultList = new ArrayList<>();
        String sql = "SELECT record_id, user_id, date, type, amount, category, remark FROM account_record_table";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    AccountRecord record = new AccountRecord();
                    record.setRecordId(rs.getInt("record_id"));
                    record.setUserId(rs.getString("user_id"));
                    record.setDate(rs.getString("date"));
                    record.setType(rs.getString("type"));
                    record.setAmount(rs.getDouble("amount"));
                    record.setCategory(rs.getString("category"));
                    record.setRemark(rs.getString("remark"));
                    resultList.add(record);
                }
            }
        } catch (SQLException e) {
            throw e;
        }
        return resultList;
    }

    public boolean deleteRecord(int recordId) throws SQLException {
        String sql = "DELETE FROM account_record_table WHERE record_id =?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, recordId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public AccountRecord getRecordById(int recordId) throws SQLException {
        AccountRecord record = null;
        String sql = "SELECT record_id, user_id, date, type, amount, category, remark FROM account_record_table WHERE record_id =?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, recordId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    record = new AccountRecord();
                    record.setRecordId(rs.getInt("record_id"));
                    record.setUserId(rs.getString("user_id"));
                    record.setDate(rs.getString("date"));
                    record.setType(rs.getString("type"));
                    record.setAmount(rs.getDouble("amount"));
                    record.setCategory(rs.getString("category"));
                    record.setRemark(rs.getString("remark"));
                }
            }
        }
        return record;
    }

    // 自定义查询方法
    public List<AccountRecord> queryByCustom(String sql, List<Object> params) throws SQLException {
        List<AccountRecord> resultList = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            
            // 设置参数
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                
                // 特殊处理LIKE查询
                if (sql.contains(" LIKE ?") && param instanceof String) {
                    preparedStatement.setString(i + 1, "%" + param + "%");
                } 
                // 处理IN查询
                else if (param instanceof String && sql.contains(" IN (")) {
                    preparedStatement.setString(i + 1, (String) param);
                }
                // 处理普通参数
                else if (param instanceof String) {
                    preparedStatement.setString(i + 1, (String) param);
                } else if (param instanceof Double) {
                    preparedStatement.setDouble(i + 1, (Double) param);
                } else {
                    preparedStatement.setObject(i + 1, param);
                }
            }
            
            // 调试输出SQL
            System.out.println("Executing SQL: " + preparedStatement);
            
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    AccountRecord record = new AccountRecord();
                    record.setRecordId(rs.getInt("record_id"));
                    record.setUserId(rs.getString("user_id"));
                    record.setDate(rs.getString("date"));
                    record.setType(rs.getString("type"));
                    record.setAmount(rs.getDouble("amount"));
                    record.setCategory(rs.getString("category"));
                    record.setRemark(rs.getString("remark"));
                    resultList.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing custom query: " + e.getMessage());
            throw e;
        }
        return resultList;
    }
 // 其他方法保持不变...

    /**
     * 使用存储过程查询账务记录
     */
    public List<AccountRecord> queryByCustomWithSP(String conditionType, String startTime, 
            String endTime, String[] recordTypes, 
            String project, Double minAmount, 
            Double maxAmount) throws SQLException {

        List<AccountRecord> resultList = new ArrayList<>();
        Map<String, List<String>> typeMapping = createTypeMapping();
        Map<String, String> dbToDisplayName = new HashMap<>();
        
        // 构建数据库值到显示名称的反向映射
        for (Map.Entry<String, List<String>> entry : typeMapping.entrySet()) {
            String displayName = entry.getKey();
            for (String dbValue : entry.getValue()) {
                dbToDisplayName.put(dbValue, displayName);
            }
        }

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call QueryRecords(?, ?, ?, ?, ?, ?, ?)}")) {

            // 设置参数
            cs.setString(1, conditionType);
            cs.setString(2, startTime);
            cs.setString(3, endTime);

            // 处理记录类型数组
            String recordTypesStr = null;
            if (recordTypes != null && recordTypes.length > 0) {
                recordTypesStr = String.join(",", recordTypes);
            }
            cs.setString(4, recordTypesStr);

            cs.setString(5, project);
            cs.setObject(6, minAmount, Types.DOUBLE);
            cs.setObject(7, maxAmount, Types.DOUBLE);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    AccountRecord record = new AccountRecord();
                    record.setRecordId(rs.getInt("record_id"));
                    record.setUserId(rs.getString("user_id"));
                    record.setDate(rs.getString("date"));
                    
                    // 获取数据库中的type值并转换为显示名称
                    String dbType = rs.getString("type");
                    String displayType = dbToDisplayName.getOrDefault(dbType, dbType);
                    record.setType(displayType);
                    
                    record.setAmount(rs.getDouble("amount"));
                    record.setCategory(rs.getString("category"));
                    record.setRemark(rs.getString("remark"));
                    resultList.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing stored procedure: " + e.getMessage());
            throw e;
        }

        return resultList;
    }

    // 其他方法保持不变...

    private Map<String, List<String>> createTypeMapping() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("支出", Arrays.asList("expense", "hand out"));
        map.put("收入", Collections.singletonList("income"));
        map.put("转账", Collections.singletonList("transfer"));
        map.put("借出", Collections.singletonList("brrowOut"));
        map.put("借入", Collections.singletonList("brrowIn"));
        map.put("还入", Collections.singletonList("repaymentIn"));
        map.put("还出", Collections.singletonList("repaymentOut"));
        return map;
    }

   
}