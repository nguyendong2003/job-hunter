package vn.nguyendong.jobhunter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * Cấu hình Interceptor
 */
@Configuration
public class PermissionInterceptorConfiguration implements WebMvcConfigurer {
    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /*
         * Các URL không cần kiểm tra quyền truy cập (không cần gọi Interceptor, mà chạy
         * bình thường)
         */
        String[] whiteList = {
                "/", "/api/v1/auth/**", "/storage/**",
                "/api/v1/companies/**", "/api/v1/jobs/**", "/api/v1/skills/**", "/api/v1/files",
                "/api/v1/resumes/**",
                "/api/v1/subscribers/**"
        };

        registry.addInterceptor(getPermissionInterceptor())
                .excludePathPatterns(whiteList);
    }
}
