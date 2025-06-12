package filter;

import entity.Permission;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PermissionChecker {
    private static final Map<String, String> FUNCTION_PERMISSION_MAP = new HashMap<>();
    static {
        FUNCTION_PERMISSION_MAP.put("function1", "add_record");
        FUNCTION_PERMISSION_MAP.put("function2", "view_record");
        FUNCTION_PERMISSION_MAP.put("function3", "view_all_records");
        FUNCTION_PERMISSION_MAP.put("function4", "edit_record");
        FUNCTION_PERMISSION_MAP.put("function5", "delete_record");
        FUNCTION_PERMISSION_MAP.put("function6", "analyze_statistics");
        FUNCTION_PERMISSION_MAP.put("function7", "save_record");
        FUNCTION_PERMISSION_MAP.put("function8", "view_log");
        FUNCTION_PERMISSION_MAP.put("function9", "sort_record");
        FUNCTION_PERMISSION_MAP.put("function10", "manage_permission");
    }

    public static boolean hasPermission(HttpSession session, String functionKey) {
        try {
            List<Permission> userPermissions = (List<Permission>) session.getAttribute("userPermissions");
            if (userPermissions == null || userPermissions.isEmpty()) {
                System.out.println("权限检查失败: 用户权限列表为空");
                return false;
            }
            
            String requiredPermission = FUNCTION_PERMISSION_MAP.get(functionKey);
            if (requiredPermission == null) {
                System.out.println("权限检查失败: 功能键 '" + functionKey + "' 没有对应的权限映射");
                return false;
            }
            
            return userPermissions.stream()
                .map(Permission::getPermissionName)
                .anyMatch(requiredPermission::equals);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("权限检查异常: " + e.getMessage());
            return false;
        }
    }
 // 在PermissionChecker中添加方法
    public static boolean canManageRoles(HttpSession session) {
        return hasPermission(session, "manage_permission");
    }

    public static boolean canAssignRoles(HttpSession session) {
        return hasPermission(session, "assign_roles");
    }
}