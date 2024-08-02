package vn.nguyendong.jobhunter.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.nguyendong.jobhunter.domain.User;
import vn.nguyendong.jobhunter.domain.response.ResponseCreateUserDTO;
import vn.nguyendong.jobhunter.domain.response.ResponseUpdateUserDTO;
import vn.nguyendong.jobhunter.domain.response.ResponseUserDTO;
import vn.nguyendong.jobhunter.domain.response.ResultPaginationDTO;
import vn.nguyendong.jobhunter.service.UserService;
import vn.nguyendong.jobhunter.util.annotation.ApiMessage;
import vn.nguyendong.jobhunter.util.error.IdInvalidException;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /*
     * https://docs.spring.io/spring-data/rest/reference/paging-and-sorting.html
     * 
     * Trong API phải truyền đúng tên params: page, size, sort thì Pageable mới tự
     * hiểu được
     * 
     * 
     * Truyền thêm param filter để sử dụng thư viện
     * https://github.com/turkraft/springfilter
     */
    @GetMapping("/users")
    @ApiMessage("fetch users")
    public ResponseEntity<ResultPaginationDTO> fetchUsers(
            @Filter Specification<User> spec,
            Pageable pageable) {
        // cách 1
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.fetchUsers(spec, pageable));
    }

    @GetMapping("/users/{id}")
    @ApiMessage("fetch user by id")
    public ResponseEntity<ResponseUserDTO> fetchUserById(@PathVariable("id") long id) throws IdInvalidException {
        User user = this.userService.fetchUserById(id);
        if (user == null) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại.");
        }

        // cách 1
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertToResponseUserDTO(user));
    }

    @PostMapping("/users")
    @ApiMessage("create a new user")
    public ResponseEntity<ResponseCreateUserDTO> createNewUser(@Valid @RequestBody User user)
            throws IdInvalidException {
        boolean isEmailExist = this.userService.isEmailExists(user.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException(
                    "Email " + user.getEmail() + "đã tồn tại, vui lòng sử dụng email khác.");
        }

        String hashPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        User createdUser = this.userService.handleCreateUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.userService.convertToResponseCreateUserDTO(createdUser));
    }

    @PutMapping("/users")
    @ApiMessage("update a user")
    public ResponseEntity<ResponseUpdateUserDTO> updateUser(@RequestBody User user) throws IdInvalidException {
        User updateUser = this.userService.handleUpdateUser(user);
        if (updateUser == null) {
            throw new IdInvalidException("User với id = " + user.getId() + " không tồn tại.");
        }
        // cách 1
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertToResponseUpdateUserDTO(updateUser));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) throws IdInvalidException {
        User user = this.userService.fetchUserById(id);
        if (user == null) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại.");
        }

        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }
}
