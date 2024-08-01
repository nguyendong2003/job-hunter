package vn.nguyendong.jobhunter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.nguyendong.jobhunter.domain.User;
import vn.nguyendong.jobhunter.domain.dto.LoginDTO;
import vn.nguyendong.jobhunter.domain.dto.ResponseLoginDTO;
import vn.nguyendong.jobhunter.domain.dto.ResponseLoginDTO.UserLogin;
import vn.nguyendong.jobhunter.service.UserService;
import vn.nguyendong.jobhunter.util.SecurityUtil;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;

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
    @PostMapping("/login")
    public ResponseEntity<ResponseLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        // Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        // xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // create a token
        String accessToken = this.securityUtil.createToken(authentication);

        // Save data to Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // set data to response
        ResponseLoginDTO res = new ResponseLoginDTO();
        res.setAccessToken(accessToken);

        User currentUserDB = this.userService.handleGetUserByUsername(loginDTO.getUsername());
        if (currentUserDB != null) {
            ResponseLoginDTO.UserLogin user = new ResponseLoginDTO.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getName());
            res.setUser(user);
        }

        //
        return ResponseEntity.ok().body(res);
    }
}
