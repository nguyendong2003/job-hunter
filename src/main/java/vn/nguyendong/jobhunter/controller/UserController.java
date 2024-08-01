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

import vn.nguyendong.jobhunter.domain.User;
import vn.nguyendong.jobhunter.domain.dto.ResultPaginationDTO;
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

        // cách 2
        // return ResponseEntity.ok(this.userService.fetchAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> fetchUserById(@PathVariable("id") long id) {
        // cách 1
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.fetchUserById(id));

        // cách 2
        // return ResponseEntity.ok(this.userService.fetchUserById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<User> createNewUser(@RequestBody User user) {
        String hashPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        User createdUser = this.userService.handleCreateUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        // cách 1
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleUpdateUser(user));

        // cách 2
        // return ResponseEntity.ok(this.userService.handleUpdateUser(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") long id) throws IdInvalidException {
        this.userService.handleDeleteUser(id);

        if (id >= 1500) {
            throw new IdInvalidException("Id khong duoc nho hon 1500");
        }

        // cách 1
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");

        // cách 2
        // return ResponseEntity.ok("User deleted successfully");
    }
}
