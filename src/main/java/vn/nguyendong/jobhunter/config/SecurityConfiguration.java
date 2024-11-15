package vn.nguyendong.jobhunter.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;

import vn.nguyendong.jobhunter.util.SecurityUtil;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {
    // Read values in application.properties (JWT)
    @Value("${nguyendong.jwt.base64-secret}")
    private String jwtKey;

    // mã hoá mật khẩu người dùng bằng thuật toán BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // key được sinh ra dưới định dạng Base64 => cần giải mã key để lấy ra SecretKey
    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length,
                SecurityUtil.JWT_ALGORITHM.getName());
    }

    // khai báo mã hoá JWT như thế nào
    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    // giải mã JWT(add .oauth2ResourceServer trong filterChain thì phải add hàm này)
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();

        /*
         * Đoạn code dưới đây là việc ghi đè lại hàm: String decode(String) trong
         * FunctionalInterface JwtDecoder
         */
        return token -> {
            try {
                return jwtDecoder.decode(token);
            } catch (Exception e) {
                System.out.println(">>> JWT error: " + e.getMessage());
                throw e;
            }
        };
    }

    /*
     * - Lấy thông tin từ token và nạp vào phần authorities (quyền hạn)
     * => phần authorities này sẽ được sử dụng trong hàm filterChain()
     * 
     * 
     * Hàm createAccessToken() trong SecurityUtil.java có key "permission"
     * => cần phải truyền đúng chính xác tên key "permisssion" vào hàm này để nó có
     * thể đọc value của key đó và nạp vào authorities
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("permission");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {

        String[] whiteList = {
                "/",
                "/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/register",
                "/storage/**",
                "/api/v1/email/**",
                // config access swagger
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
                //
        };

        http
                .csrf(c -> c.disable())
                .cors(Customizer.withDefaults()) // cấu hình cors mặc định
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers(whiteList).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/jobs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/skills/**").permitAll()

                        .anyRequest().authenticated())

                /*
                 * kích hoạt filter BearerTokenAuthenticationFilter
                 * => Filter này sẽ “tự động tách” Bear Token
                 * -> sau đó đưa nó vào JwtDecoder để giải mã token -> ghi đè jwtDecoder()
                 */
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults())
                        // custom 'response body' when token is invalid or not found or expired
                        .authenticationEntryPoint(customAuthenticationEntryPoint))

                // // default exception
                // .exceptionHandling(
                // exceptions -> exceptions
                // .authenticationEntryPoint(customAuthenticationEntryPoint) // 401
                // .accessDeniedHandler(new BearerTokenAccessDeniedHandler())) // 403

                .formLogin((form) -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

}
