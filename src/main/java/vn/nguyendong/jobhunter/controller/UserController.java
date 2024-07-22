package vn.nguyendong.jobhunter.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import vn.nguyendong.jobhunter.domain.User;
import vn.nguyendong.jobhunter.service.UserService;

import java.util.List;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public List<User> fetchAllUsers() {
        return this.userService.fetchAllUsers();
    }

    @GetMapping("/user/{id}")
    public User fetchUserById(@PathVariable("id") long id) {
        return this.userService.fetchUserById(id);
    }

    @PostMapping("/user")
    public User createNewUser(@RequestBody User user) {

        User user2 = this.userService.handleCreateUser(user);

        return user2;
    }

    @PutMapping("/user")
    public User updateUser(@RequestBody User user) {
        return this.userService.handleUpdateUser(user);
    }

    @DeleteMapping("/user/{id}")
    public void deleteUser(@PathVariable("id") long id) {
        this.userService.handleDeleteUser(id);
    }
}
