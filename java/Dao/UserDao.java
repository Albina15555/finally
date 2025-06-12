package Dao;

import utils.DBConnection;
import entity.User;
import filter.PermissionChecker;
import jakarta.servlet.http.HttpSession;
import java.sql.*;
import java.util.*;

public class UserDao {
    // 验证用户登录
    public static boolean validateUser(String username, String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_table WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // 根据用户名和密码获取用户ID
    public static String getUserIdByUsernameAndPassword(String username, String password) throws SQLException {
        String sql = "SELECT user_id FROM user_table WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("user_id") : null;
            }
        }
    }

    // 用户注册
//    public static boolean registerUser(String username, String password, String email) throws SQLException {
//        String sql = "INSERT INTO user_table (username, password, email) VALUES (?, ?, ?)";
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, username);
//            pstmt.setString(2, password);
//            pstmt.setString(3, email);
//            return pstmt.executeUpdate() > 0;
//        }
//    }
    public static boolean registerUser(String username, String password, String email, String phone) throws SQLException {
        String sql = "INSERT INTO user_table (username, password, email, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 检查用户名是否已存在
    public static boolean checkUsernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_table WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // 获取所有用户角色
    public Map<Integer, Map<String, String>> getAllUserRoles() {
        Map<Integer, Map<String, String>> result = new HashMap<>();
        String sql = "SELECT u.user_id, u.username, r.role_name " +
                "FROM user_table u " +
                "JOIN user_role ur ON u.user_id = ur.user_id " +
                "JOIN role r ON ur.role_id = r.role_id";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String userName = rs.getString("username");
                String roleName = rs.getString("role_name");
                
                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("userName", userName);
                userInfo.put("roleName", roleName);
                
                result.put(userId, userInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("查询用户角色失败: " + e.getMessage());
        }
        return result;
    }
    
    // 获取所有用户（只包含user_id和username）
    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>();
        String sql = "SELECT user_id, username FROM user_table";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                allUsers.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allUsers;
    }
    
    // 获取所有用户的完整信息（新增方法）
    public List<User> getAllUsersWithFullInfo() {
        List<User> allUsers = new ArrayList<>();
        String sql = "SELECT user_id, username, email, phone FROM user_table";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                allUsers.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allUsers;
    }
    
    // 根据ID获取用户
    public User getUserById(int userId) {
        User user = null;
        String sql = "SELECT user_id, username, email, phone FROM user_table WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
    
    // 根据用户名搜索用户
    public List<User> searchUsersByUsername(String username) {
        List<User> result = new ArrayList<>();
        String sql = "SELECT user_id, username, email, phone FROM user_table WHERE username LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + username + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    result.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    public static boolean canModifyUser(HttpSession session, int targetUserId) {
        try {
            int currentUserId = (int) session.getAttribute("userId");
            return currentUserId == targetUserId;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 专用更新方法（接受HttpSession）
     */
    public static void updateUserInfo(HttpSession session, int targetUserId, 
                                     String newUsername, String newPassword, 
                                     String newEmail, String newPhone) throws SQLException {
        int currentUserId = (int) session.getAttribute("userId");
        updateUserInfo(currentUserId, targetUserId, newUsername, newPassword, newEmail, newPhone);
    }
    
    /**
     * 原有更新方法保持不变
     */
   
    public static void updateUserInfo(int currentUserId, int targetUserId, 
            String newUsername, String newPassword, 
            String newEmail, String newPhone) throws SQLException {
try (Connection conn = DBConnection.getConnection();
CallableStatement cstmt = conn.prepareCall("{call UpdateUserInfo(?, ?, ?, ?, ?)}")) {

// 使用正确的参数顺序，只传递需要更新的字段
cstmt.setInt(1, targetUserId);
cstmt.setString(2, newUsername);

// 处理密码 - 留空不更新
if (newPassword != null && !newPassword.isEmpty()) {
cstmt.setString(3, newPassword);
} else {
cstmt.setNull(3, Types.VARCHAR);
}

cstmt.setString(4, newEmail);
cstmt.setString(5, newPhone);

cstmt.execute();
} catch (SQLException e) {
throw new SQLException("更新用户信息失败: " + e.getMessage());
}
}
    public boolean isUsernameExists(String username, int excludeUserId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM user_table WHERE username = ? AND user_id != ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setInt(2, excludeUserId);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            // 关闭资源
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        }
    }
    }
