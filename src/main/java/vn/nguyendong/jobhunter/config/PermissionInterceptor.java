package vn.nguyendong.jobhunter.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.nguyendong.jobhunter.domain.Permission;
import vn.nguyendong.jobhunter.domain.Role;
import vn.nguyendong.jobhunter.domain.User;
import vn.nguyendong.jobhunter.service.UserService;
import vn.nguyendong.jobhunter.util.SecurityUtil;
import vn.nguyendong.jobhunter.util.error.PermissionException;

/*
 * Mô hình: Request => Spring Security => Interceptor => Controller => Service
 * 
 * Interceptor: Kiểm tra quyền truy cập (permission) của người dùng
 * 
 * - Interceptor được gọi sau Spring Security và trước Controller
 * 
 * prehandle() – called before the execution of the actual handler
 */
public class PermissionInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    /*
     * preHandle(): return true thì nó sẽ đi tiếp đến Controller
     * 
     * Lỗi ở dòng List<Permission> permissions = role.getPermissions(); nếu không
     * dùng @Transactional vì permissions trong Role.java là FetchType.LAZY, nên ở
     * dòng Role role = user.getRole(); chưa lấy được permissions, bắt buộc phải
     * query xuống database để lấy permissions
     * 
     * => Dùng @Transactional để giữ nguyên session đăng nhập và query xuống
     * database lấy lên được permissions
     * 
     */
    @Override
    @Transactional
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response, Object handler)
            throws Exception {
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);

        // check permission
        String email = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        // người dùng đã đăng nhập
        if (email != null && !email.isEmpty()) {
            User user = this.userService.handleGetUserByUsername(email);
            if (user != null) {
                Role role = user.getRole();
                if (role != null) {
                    List<Permission> permissions = role.getPermissions();
                    boolean isAllowed = permissions.stream().anyMatch(permission -> {
                        return permission.getApiPath().equals(path) && permission.getMethod().equals(httpMethod);
                    });

                    if (!isAllowed) {
                        throw new PermissionException("Bạn không có quyền truy cập endpoint này.");
                    }
                } else {
                    throw new PermissionException("Bạn không có quyền truy cập endpoint này.");
                }
            }
        }

        return true;
    }
}
