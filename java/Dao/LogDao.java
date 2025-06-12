package Dao;

import entity.OperationLog;
import utils.DBConnection; // 使用统一的数据库连接工具类
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogDao {
    // 添加操作日志
    public boolean addOperationLog(OperationLog log) {
        String sql = "INSERT INTO operation_log " +
                     "(user_id, username, operation_time, operation_type, operation_desc, ip_address, status, execution_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection(); // 使用MySQL连接
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, log.getUserId());
            stmt.setString(2, log.getUsername());
            stmt.setTimestamp(3, log.getOperationTime());
            stmt.setString(4, log.getOperationType());
            stmt.setString(5, log.getOperationDesc());
            stmt.setString(6, log.getIpAddress());
            stmt.setString(7, log.getStatus());
            stmt.setLong(8, log.getExecutionTime());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // 获取生成的日志ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        log.setLogId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 查询操作日志（按时间倒序）
    public List<OperationLog> queryOperationLogs(String type, String startTime, String endTime) {
        List<OperationLog> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM operation_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        // 添加查询条件
        if (type != null && !type.isEmpty()) {
            sql.append(" AND operation_type = ?");
            params.add(type);
        }
        
        if (startTime != null && !startTime.isEmpty()) {
            sql.append(" AND operation_time >= ?");
            params.add(Timestamp.valueOf(startTime + " 00:00:00"));
        }
        
        if (endTime != null && !endTime.isEmpty()) {
            sql.append(" AND operation_time <= ?");
            params.add(Timestamp.valueOf(endTime + " 23:59:59"));
        }
        
        // 按操作时间降序排列（最新的在前）
        sql.append(" ORDER BY operation_time DESC, log_id DESC");
        
        try (Connection conn = DBConnection.getConnection(); // 使用MySQL连接
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // 设置查询参数
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            // 执行查询
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OperationLog log = new OperationLog();
                    log.setLogId(rs.getInt("log_id"));
                    log.setUserId(rs.getInt("user_id"));
                    log.setUsername(rs.getString("username"));
                    log.setOperationTime(rs.getTimestamp("operation_time"));
                    log.setOperationType(rs.getString("operation_type"));
                    log.setOperationDesc(rs.getString("operation_desc"));
                    log.setIpAddress(rs.getString("ip_address"));
                    log.setStatus(rs.getString("status"));
                    log.setExecutionTime(rs.getLong("execution_time"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
}