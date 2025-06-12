package entity; // 注意：包名可能需要根据项目结构调整
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * 操作日志实体类
 * 记录系统中所有用户操作行为
 */
public class OperationLog {
    private Integer logId;
    private Integer userId;
    private String username;
    private Timestamp operationTime;
    private String operationType;
    private String operationDesc;
    private String ipAddress;
    private String status;
    private long executionTime; // 操作执行时间(毫秒)
    
    // 时间格式化器（线程安全版本）
    private static final ThreadLocal<SimpleDateFormat> sdf = ThreadLocal.withInitial(
        () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    );

    // Getters and Setters
    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Timestamp operationTime) {
        this.operationTime = operationTime;
    }
    
    // 提供格式化时间字符串
    public String getOperationTimeStr() {
        if (operationTime != null) {
            return sdf.get().format(operationTime);
        }
        return "";
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationDesc() {
        return operationDesc;
    }

    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    @Override
    public String toString() {
        return "OperationLog{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", operationTime=" + (operationTime != null ? sdf.get().format(operationTime) : "null") +
                ", operationType='" + operationType + '\'' +
                ", operationDesc='" + operationDesc + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", status='" + status + '\'' +
                ", executionTime=" + executionTime + "ms" +
                '}';
    }
}