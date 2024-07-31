package vn.nguyendong.jobhunter.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtil {
    private final JwtEncoder jwtEncoder;

    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    // Read values in application.properties
    @Value("${nguyendong.jwt.base64-secret}")
    private String jwtKey;

    @Value("${nguyendong.jwt.token-validity-in-seconds}")
    private long jwtKeyExpiration;

    public String createToken(Authentication authentication) {
        // thời gian hiện tại
        Instant now = Instant.now();
        // thời gian hết hạn của token = thời gian hiện tại + thời gian hết hạn
        Instant validity = now.plus(this.jwtKeyExpiration, ChronoUnit.SECONDS);

        // tạo header cho token
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        /*
         * Trong JSON Web Token (JWT), "claims" là các thông tin được mã hóa bên trong
         * token. Các claims này chứa các thông tin về người dùng và các quyền hạn của
         * họ.
         * 
         * Phần này là body của token
         */
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now) // Thời gian phát hành token
                .expiresAt(validity) // Thời gian hết hạn của token
                .subject(authentication.getName()) // Chủ thể của token (tên người dùng)
                .claim("nguyendong", authentication) // Một claim tùy ý chứa thông tin xác thực
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

    }
}
