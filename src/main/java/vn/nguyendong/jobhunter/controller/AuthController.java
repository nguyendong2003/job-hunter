package vn.nguyendong.jobhunter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.nguyendong.jobhunter.domain.User;
import vn.nguyendong.jobhunter.domain.dto.LoginDTO;
import vn.nguyendong.jobhunter.domain.dto.ResponseLoginDTO;
import vn.nguyendong.jobhunter.service.UserService;
import vn.nguyendong.jobhunter.util.SecurityUtil;
import vn.nguyendong.jobhunter.util.annotation.ApiMessage;
import vn.nguyendong.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

        private final AuthenticationManagerBuilder authenticationManagerBuilder;
        private final SecurityUtil securityUtil;
        private final UserService userService;

        @Value("${nguyendong.jwt.refresh-token-validity-in-seconds}")
        private long refreshTokenExpiration;

        public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,
                        SecurityUtil securityUtil,
                        UserService userService) {
                this.authenticationManagerBuilder = authenticationManagerBuilder;
                this.securityUtil = securityUtil;
                this.userService = userService;
        }

        /*
         * Sử dụng @Valid thì mỗi lần không hợp lệ thì throw
         * MethodArgumentNotValidException
         * 
         * => Viết @ExceptionHandler(MethodArgumentNotValidException.class) trong
         * GlobalException.java để giải quyết nó
         */
        @PostMapping("/auth/login")
        public ResponseEntity<ResponseLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
                // Nạp input gồm username/password vào Security
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginDTO.getUsername(), loginDTO.getPassword());

                // xác thực người dùng => cần viết hàm loadUserByUsername
                Authentication authentication = authenticationManagerBuilder.getObject()
                                .authenticate(authenticationToken);

                // set thông tin người dùng đăng nhập vào Context (có thể sử dụng sau này)
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // set data to response
                ResponseLoginDTO res = new ResponseLoginDTO();

                User currentUserDB = this.userService.handleGetUserByUsername(loginDTO.getUsername());
                if (currentUserDB != null) {
                        ResponseLoginDTO.UserLogin user = new ResponseLoginDTO.UserLogin(
                                        currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName());
                        res.setUser(user);
                }
                // create a token
                String accessToken = this.securityUtil.createAccessToken(authentication.getName(), res.getUser());
                res.setAccessToken(accessToken);

                // create refresh token
                String refreshToken = this.securityUtil.createRefreshToken(loginDTO.getUsername(), res);

                // update refresh token of user
                this.userService.updateUserRefreshToken(refreshToken, loginDTO.getUsername());

                // set cookies to client
                ResponseCookie responseCookie = ResponseCookie
                                .from("refresh_token", refreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(refreshTokenExpiration)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                                .body(res);
        }

        @GetMapping("/auth/account")
        @ApiMessage("fetch account")
        public ResponseEntity<ResponseLoginDTO.UserLogin> getAccount() {
                String email = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";

                User currentUserDB = this.userService.handleGetUserByUsername(email);
                ResponseLoginDTO.UserLogin userLogin = new ResponseLoginDTO.UserLogin();
                if (currentUserDB != null) {
                        userLogin.setId(currentUserDB.getId());
                        userLogin.setEmail(currentUserDB.getEmail());
                        userLogin.setName(currentUserDB.getName());
                }
                return ResponseEntity.ok().body(userLogin);
        }

        @GetMapping("/auth/refresh")
        @ApiMessage("get user by refresh token")
        public ResponseEntity<ResponseLoginDTO> refreshToken(
                        // get refresh token from cookies
                        @CookieValue(name = "refresh_token", defaultValue = "abc") String refreshToken)
                        throws IdInvalidException {
                if (refreshToken.equals("abc")) {
                        throw new IdInvalidException("Bạn không có refresh token ở cookie");
                }
                // check valid refresh token (trả về một đối tượng Jwt => refresh token hợp lệ)
                Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
                String email = decodedToken.getSubject();

                // check user by refresh token + email
                User currentUser = this.userService.getUserByRefreshTokenAndEmail(refreshToken, email);
                if (currentUser == null) {
                        throw new IdInvalidException("Refresh token không hợp lệ");
                }

                // issue new access token/set refresh token to cookies
                ResponseLoginDTO res = new ResponseLoginDTO();

                User currentUserDB = this.userService.handleGetUserByUsername(email);
                if (currentUserDB != null) {
                        ResponseLoginDTO.UserLogin user = new ResponseLoginDTO.UserLogin(
                                        currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName());
                        res.setUser(user);
                }
                // create a token
                String newAccessToken = this.securityUtil.createAccessToken(email, res.getUser());
                res.setAccessToken(newAccessToken);

                // create new refresh token
                String newRefreshToken = this.securityUtil.createRefreshToken(email, res);

                // update refresh token of user
                this.userService.updateUserRefreshToken(newRefreshToken, email);

                // set cookies to client
                ResponseCookie responseCookie = ResponseCookie
                                .from("refresh_token", newRefreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(refreshTokenExpiration)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                                .body(res);
        }

        @PostMapping("/auth/logout")
        @ApiMessage("logout user")
        public ResponseEntity<Void> logout() throws IdInvalidException {
                String email = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                if (email.equals("")) {
                        throw new IdInvalidException("Bạn chưa đăng nhập/access token không hợp lệ");
                }

                // update refresh token of user = null
                this.userService.updateUserRefreshToken(null, email);

                // remove refresh token from cookies
                ResponseCookie responseCookie = ResponseCookie
                                .from("refresh_token", null)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(0)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                                .body(null);
        }
}
