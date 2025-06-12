package Dao;

import entity.Permission;
import entity.Role;
import entity.User;
import utils.DBConnection;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class PermissionDao {
    // 获取数据库连接
    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }
 // 修改 getAllRolesWithPermissions 方法，确保包含所有角色
    public Map<Role, List<Permission>> getAllRolesWithPermissions() {
        Map<Role, List<Permission>> result = new LinkedHashMap<>(); // 保持顺序
        
        // 先获取所有角色
        List<Role> allRoles = getAllRoles();
        
        for (Role role : allRoles) {
            // 获取角色对应的权限
            List<Integer> permIds = getPermissionsByRole(role.getRoleId());
            List<Permission> permissions = permIds.stream()
                .map(this::getPermissionById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
            result.put(role, permissions);
        }
        return result;
    }

    /**
     * 获取指定用户的权限（非管理员场景）
     * @param userId 用户ID
     * @return 权限列表
     */
    public List<Permission> getUserRolePermissions(int userId) {
        List<Permission> result = new ArrayList<>();
        // SQL查询语句，通过多表连接获取用户的权限信息
        String sql = "SELECT " +
                "p.permission_id, " +
                "p.permission_name, " +
                "p.description, " +
                "r.role_id, " +
                "r.role_name, " +
                "ut.user_id, " +
                "ut.username " +
                "FROM user_table ut " +
                "JOIN user_role ur ON ut.user_id = ur.user_id " +
                "JOIN role r ON ur.role_id = r.role_id " +
                "JOIN role_permission rp ON r.role_id = rp.role_id " +
                "JOIN permission p ON rp.permission_id = p.permission_id " +
                "WHERE ut.user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // 设置查询参数
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                // 遍历结果集，将结果映射为Permission对象
                while (rs.next()) {
                    result.add(mapPermissionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            // 打印异常信息
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取所有用户的权限（管理员场景）
     * @return 权限列表
     */
    public List<Permission> getAllUserPermissions() {
        List<Permission> result = new ArrayList<>();
        // SQL查询语句，通过多表连接获取所有用户的权限信息，并按用户ID排序
        String sql = "SELECT " +
                "p.permission_id, " +
                "p.permission_name, " +
                "p.description, " +
                "r.role_id, " +
                "r.role_name, " +
                "ut.user_id, " +
                "ut.username " +
                "FROM user_table ut " +
                "JOIN user_role ur ON ut.user_id = ur.user_id " +
                "JOIN role r ON ur.role_id = r.role_id " +
                "JOIN role_permission rp ON r.role_id = rp.role_id " +
                "JOIN permission p ON rp.permission_id = p.permission_id " +
                "ORDER BY ut.user_id";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            // 遍历结果集，将结果映射为Permission对象
            while (rs.next()) {
                result.add(mapPermissionFromResultSet(rs));
            }
        } catch (SQLException e) {
            // 打印异常信息
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取所有权限
     * @return 权限列表
     */
    public List<Permission> getAllPermissions() {
        List<Permission> result = new ArrayList<>();
        // SQL查询语句，获取所有权限信息
        String sql = "SELECT * FROM permission";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // 遍历结果集，将结果映射为Permission对象
            while (rs.next()) {
                Permission perm = new Permission();
                perm.setPermissionId(rs.getInt("permission_id"));
                perm.setPermissionName(rs.getString("permission_name"));
                perm.setDescription(rs.getString("description"));
                result.add(perm);
            }
        } catch (SQLException e) {
            // 打印异常信息
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取所有角色
     * @return 角色列表
     */
    public List<Role> getAllRoles() {
        List<Role> result = new ArrayList<>();
        // SQL查询语句，获取所有角色信息
        String sql = "SELECT * FROM role";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // 遍历结果集，将结果映射为Role对象
            while (rs.next()) {
                Role role = new Role();
                role.setRoleId(rs.getInt("role_id"));
                role.setRoleName(rs.getString("role_name"));
                role.setDescription(rs.getString("description"));
                result.add(role);
            }
        } catch (SQLException e) {
            // 打印异常信息
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取角色的权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    public List<Integer> getPermissionsByRole(int roleId) {
        List<Integer> permissions = new ArrayList<>();
        // SQL查询语句，获取指定角色的权限ID
        String sql = "SELECT permission_id FROM role_permission WHERE role_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 设置查询参数
            pstmt.setInt(1, roleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                // 遍历结果集，将权限ID添加到列表中
                while (rs.next()) {
                    permissions.add(rs.getInt("permission_id"));
                }
            }
        } catch (SQLException e) {
            // 打印异常信息
            e.printStackTrace();
        }
        return permissions;
    }

    /**
     * 根据ID获取权限
     * @param permissionId 权限ID
     * @return 权限对象
     */
    public Permission getPermissionById(int permissionId) {
        Permission permission = null;
        // SQL查询语句，根据权限ID获取权限信息
        String sql = "SELECT * FROM permission WHERE permission_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 设置查询参数
            pstmt.setInt(1, permissionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    permission = new Permission();
                    permission.setPermissionId(rs.getInt("permission_id"));
                    permission.setPermissionName(rs.getString("permission_name"));
                    permission.setDescription(rs.getString("description"));
                }
            }
        } catch (SQLException e) {
            // 打印异常信息
            e.printStackTrace();
        }
        return permission;
    }

    /**
     * 更新权限信息
     * @param permission 权限对象
     * @return 更新是否成功
     */
    public boolean updatePermission(Permission permission) {
        // SQL更新语句，更新权限名称和描述
        String sql = "UPDATE permission SET permission_name = ?, description = ? WHERE permission_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 设置更新参数
            pstmt.setString(1, permission.getPermissionName());
            pstmt.setString(2, permission.getDescription());
            pstmt.setInt(3, permission.getPermissionId());
            // 执行更新操作，判断是否成功
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // 打印异常信息
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除权限（带事务处理）
     * @param permissionId 权限ID
     * @return 删除是否成功
     */
    public boolean deletePermission(int permissionId) {
        try (Connection conn = getConnection()) {
            // 开启事务
            conn.setAutoCommit(false);

            // 删除角色-权限关联
            try (PreparedStatement pstmt1 = conn.prepareStatement("DELETE FROM role_permission WHERE permission_id=?")) {
                pstmt1.setInt(1, permissionId);
                pstmt1.executeUpdate();
            }

            // 删除权限

        } catch (SQLException e) {
            // 打印异常信息
            e.printStackTrace();
            return false;
        }
		return false;
    }

    /**
     * 分配权限（带事务处理）
     * @param userId 用户ID
     * @param roleName 角色名称
     * @param permissionValue 权限值
     * @return 分配是否成功
     */
    public boolean assignPermission(int userId, String roleName, String permissionValue) {
        try (Connection conn = getConnection()) {
            // 开启事务
            conn.setAutoCommit(false);

            // 获取角色ID
            int roleId = getRoleId(roleName, conn);
            if (roleId == -1) {
                throw new SQLException("角色不存在: " + roleName);
            }

            // 获取权限ID
            int permissionId = getPermissionId(permissionValue, conn);
            if (permissionId == -1) {
                throw new SQLException("权限不存在: " + permissionValue);
            }

            // 检查用户是否已有该角色
            boolean hasRole = checkUserHasRole(userId, roleId, conn);
            if (!hasRole) {
                addUserRole(userId, roleId, conn);
            }

            // 检查角色是否已有该权限
            boolean hasPermission = checkRoleHasPermission(roleId, permissionId, conn);
            if (!hasPermission) {
                addRolePermission(roleId, permissionId, conn);
            }

            // 提交事务
            conn.commit();
            return true;
        } catch (SQLException e) {
            // 打印异常信息
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 为角色添加权限
     * @param roleName 角色名称
     * @param description 角色描述
     * @return 添加是否成功
     */
//    public boolean addRole(String roleName, String description) {
//        // SQL插入语句，添加角色信息
//        String sql = "INSERT INTO role (role_name, description) VALUES (?, ?)";
//
//        try (Connection conn = getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            // 设置插入参数
//            pstmt.setString(1, roleName);
//            pstmt.setString(2, description);
//            // 执行插入操作，判断是否成功
//            return pstmt.executeUpdate() > 0;
//        } catch (SQLException e) {
//            // 打印异常信息
//            e.printStackTrace();
//            return false;
//        }
//    }
    public boolean addRole(String roleName, String description) {
        String sql = "CALL AddRole(?, ?)";
        try (Connection conn = getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, roleName);
            cstmt.setString(2, description);
            cstmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 删除角色
     * @param roleId 角色ID
     * @return 删除是否成功
     */
//    public boolean deleteRole(int roleId) {
//        try (Connection conn = getConnection()) {
//            // 开启事务
//            conn.setAutoCommit(false);
//
//            // 删除用户-角色关联
//            try (PreparedStatement pstmt1 = conn.prepareStatement("DELETE FROM user_role WHERE role_id=?")) {
//                pstmt1.setInt(1, roleId);
//                pstmt1.executeUpdate();
//            }
//
//            // 删除角色-权限关联
//            try (PreparedStatement pstmt2 = conn.prepareStatement("DELETE FROM role_permission WHERE role_id=?")) {
//                pstmt2.setInt(1, roleId);
//                pstmt2.executeUpdate();
//            }
//
//            // 删除角色
//            try (PreparedStatement pstmt3 = conn.prepareStatement("DELETE FROM role WHERE role_id=?")) {
//                pstmt3.setInt(1, roleId);
//                int rows = pstmt3.executeUpdate();
//                // 提交事务
//                conn.commit();
//                return rows > 0;
//            }
//        } catch (SQLException e) {
//            // 打印异常信息
//            e.printStackTrace();
//            return false;
//        }
//    }
    public boolean deleteRole(int roleId) {
        String sql = "CALL DeleteRole(?)";
        try (Connection conn = getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, roleId);
            cstmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新角色权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 更新是否成功
     */
    public boolean updateRolePermissions(int roleId, List<Integer> permissionIds) {
        try (Connection conn = getConnection()) {
            // 开启事务
            conn.setAutoCommit(false);

            // 删除现有权限
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM role_permission WHERE role_id = ?")) {
                pstmt.setInt(1, roleId);
                pstmt.executeUpdate();
            }

            // 添加新权限
            if (!permissionIds.isEmpty()) {
                String insertSql = "INSERT INTO role_permission (role_id, permission_id) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    for (int permId : permissionIds) {
                        pstmt.setInt(1, roleId);
                        pstmt.setInt(2, permId);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }

            // 提交事务
            conn.commit();
            return true;
        } catch (SQLException e) {
            // 打印异常信息
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 辅助方法：获取角色ID
     * @param roleName 角色名称
     * @param conn 数据库连接
     * @return 角色ID，如果不存在则返回-1
     * @throws SQLException SQL异常
     */
    private int getRoleId(String roleName, Connection conn) throws SQLException {
        // SQL查询语句，根据角色名称获取角色ID
        String sql = "SELECT role_id FROM role WHERE role_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 设置查询参数
            pstmt.setString(1, roleName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("role_id");
                }
            }
        }
        return -1;
    }

    /**
     * 辅助方法：获取权限ID
     * @param permissionValue 权限值
     * @param conn 数据库连接
     * @return 权限ID，如果不存在则返回-1
     * @throws SQLException SQL异常
     */
    private int getPermissionId(String permissionValue, Connection conn) throws SQLException {
        // SQL查询语句，根据权限名称获取权限ID
        String sql = "SELECT permission_id FROM permission WHERE permission_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 设置查询参数
            pstmt.setString(1, permissionValue);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("permission_id");
                }
            }
        }
        return -1;
    }

    /**
     * 辅助方法：检查用户是否已有该角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @param conn 数据库连接
     * @return 是否已有该角色
     * @throws SQLException SQL异常
     */
    private boolean checkUserHasRole(int userId, int roleId, Connection conn) throws SQLException {
        // SQL查询语句，统计用户拥有该角色的记录数
        String sql = "SELECT COUNT(*) FROM user_role WHERE user_id = ? AND role_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 设置查询参数
            pstmt.setInt(1, userId);
            pstmt.setInt(2, roleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * 辅助方法：添加用户-角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     * @param conn 数据库连接
     * @throws SQLException SQL异常
     */
    private void addUserRole(int userId, int roleId, Connection conn) throws SQLException {
        // SQL插入语句，添加用户-角色关联
        String sql = "INSERT INTO user_role (user_id, role_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 设置插入参数
            pstmt.setInt(1, userId);
            pstmt.setInt(2, roleId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 辅助方法：检查角色是否已有该权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @param conn 数据库连接
     * @return 是否已有该权限
     * @throws SQLException SQL异常
     */
    private boolean checkRoleHasPermission(int roleId, int permissionId, Connection conn) throws SQLException {
        // SQL查询语句，统计角色拥有该权限的记录数
        String sql = "SELECT COUNT(*) FROM role_permission WHERE role_id = ? AND permission_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 设置查询参数
            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * 辅助方法：添加角色-权限关联
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @param conn 数据库连接
     * @throws SQLException SQL异常
     */
    private void addRolePermission(int roleId, int permissionId, Connection conn) throws SQLException {
        // SQL插入语句，添加角色-权限关联
        String sql = "INSERT INTO role_permission (role_id, permission_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 设置插入参数
            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 从结果集映射Permission对象（用于多表查询）
     * @param rs 结果集
     * @return Permission对象
     * @throws SQLException SQL异常
     */
    private Permission mapPermissionFromResultSet(ResultSet rs) throws SQLException {
        Permission perm = new Permission();
        perm.setPermissionId(rs.getInt("permission_id"));
        perm.setPermissionName(rs.getString("permission_name"));
        perm.setDescription(rs.getString("description"));
        perm.setRoleId(rs.getInt("role_id"));
        perm.setRoleName(rs.getString("role_name"));
        perm.setUserId(rs.getInt("user_id"));
        perm.setUserName(rs.getString("username"));
        return perm;
    }

    /**
     * 为角色添加权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 添加是否成功
     */
//    public boolean addPermissionToRole(int roleId, int permissionId) {
//        // SQL插入语句，为角色添加权限
//        String sql = "INSERT INTO role_permission (role_id, permission_id) VALUES (?, ?)";
//
//        try (Connection conn = getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            // 设置插入参数
//            pstmt.setInt(1, roleId);
//            pstmt.setInt(2, permissionId);
//            // 执行插入操作，判断是否成功
//            return pstmt.executeUpdate() > 0;
//        } catch (SQLException e) {
//            // 打印异常信息
//            System.err.println("为角色添加权限失败，角色ID: " + roleId + ", 权限ID: " + permissionId);
//            e.printStackTrace();
//            return false;
//        }
//    }
    
    public boolean addPermissionToRole(int roleId, int permissionId) {
        String sql = "CALL AddPermissionToRole(?, ?)";
        try (Connection conn = getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, roleId);
            cstmt.setInt(2, permissionId);
            cstmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean removeAllPermissionsFromRole(int roleId) {
        String sql = "DELETE FROM role_permission WHERE role_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 从角色中移除权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 移除是否成功
     */
//    public boolean removePermissionFromRole(int roleId, int permissionId) {
//        // SQL删除语句，从角色中移除权限
//        String sql = "DELETE FROM role_permission WHERE role_id = ? AND permission_id = ?";
//
//        try (Connection conn = getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            // 设置删除参数
//            pstmt.setInt(1, roleId);
//            pstmt.setInt(2, permissionId);
//            // 执行删除操作，判断是否成功
//            return pstmt.executeUpdate() > 0;
//        } catch (SQLException e) {
//            // 打印异常信息
//            System.err.println("从角色移除权限失败，角色ID: " + roleId + ", 权限ID: " + permissionId);
//            e.printStackTrace();
//            return false;
//        }
//    }
    
    public boolean removePermissionFromRole(int roleId, int permissionId) {
        String sql = "CALL RemovePermissionFromRole(?, ?)";
        try (Connection conn = getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, roleId);
            cstmt.setInt(2, permissionId);
            cstmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean addPermissionsToRole(int roleId, int[] permissionIds) {
        String sql = "INSERT INTO role_permission (role_id, permission_id) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int permissionId : permissionIds) {
                pstmt.setInt(1, roleId);
                pstmt.setInt(2, permissionId);
                pstmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 为用户分配角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 分配是否成功
     */
//    public boolean assignRoleToUser(int userId, int roleId) {
//        try (Connection conn = getConnection()) {
//            // 开启事务
//            conn.setAutoCommit(false);
//
//            // 删除用户原有的角色记录
//            try (PreparedStatement pstmt1 = conn.prepareStatement("DELETE FROM user_role WHERE user_id = ?")) {
//                pstmt1.setInt(1, userId);
//                pstmt1.executeUpdate();
//            }
//
//            // 为用户分配新角色
//            String sql = "INSERT INTO user_role (user_id, role_id) VALUES (?, ?)";
//            try (PreparedStatement pstmt2 = conn.prepareStatement(sql)) {
//                pstmt2.setInt(1, userId);
//                pstmt2.setInt(2, roleId);
//                int rows = pstmt2.executeUpdate();
//
//                // 提交事务
//                conn.commit();
//                return rows > 0;
//            }
//        } catch (SQLException e) {
//            // 打印异常信息
//            System.err.println("为用户分配角色失败，用户ID: " + userId + ", 角色ID: " + roleId);
//            e.printStackTrace();
//            return false;
//        }}
    public boolean assignRoleToUser(int userId, int roleId) {
        String sql = "CALL AssignRoleToUser(?, ?)";
        try (Connection conn = getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, userId);
            cstmt.setInt(2, roleId);
            cstmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    }