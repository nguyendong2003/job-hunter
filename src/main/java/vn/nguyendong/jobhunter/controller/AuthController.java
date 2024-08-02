package vn.nguyendong.jobhunter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

@RestController
@RequestMapping("/api/v1")
public class AuthController {

        private final AuthenticationManagerBuilder authenticationManagerBuilder;
        private final SecurityUtil securityUtil;
        private final UserService userService;

        @Value("${nguyendong.jwt.refresh-token-validity-in-seconds}")
        private long refreshTokenExpiration;

        public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
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

                // Save data to Security Context
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
                String accessToken = this.securityUtil.createAccessToken(authentication, res.getUser());
                res.setAccessToken(accessToken);

                // create refresh token
                String refreshToken = this.securityUtil.createRefreshToken(loginDTO.getUsername(), res);

                // update refresh token of user
                this.userService.updateUserRefreshToken(refreshToken, loginDTO.getUsername());

                // set cookies to client
                ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken)
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
}
