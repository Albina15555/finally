package entity;

public class AccountRecord {
    private int recordId;
    private String userId;
    private String date;
    private String type;
    private double amount;
    private String category;
    private String remark;

    // 生成Getter和Setter方法
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
    public AccountRecord() {}
    // 构造函数示例，方便创建对象时初始化属性
    public AccountRecord(int recordId, String userId, String date, String type, double amount, String category, String remark) {
        this.recordId = recordId;
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.remark = remark;
    }
}