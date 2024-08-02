package vn.nguyendong.jobhunter.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.nguyendong.jobhunter.domain.User;
import vn.nguyendong.jobhunter.domain.response.ResponseCreateUserDTO;
import vn.nguyendong.jobhunter.domain.response.ResponseUpdateUserDTO;
import vn.nguyendong.jobhunter.domain.response.ResponseUserDTO;
import vn.nguyendong.jobhunter.domain.response.ResultPaginationDTO;
import vn.nguyendong.jobhunter.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResultPaginationDTO fetchUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(pageUser.getContent());

        // remove sensitive data
        // data trả về không chứa thông tin mật khẩu, refresh token của người dùng)
        List<ResponseUserDTO> listUser = pageUser.getContent()
                .stream().map(item -> new ResponseUserDTO(
                        item.getId(),
                        item.getEmail(),
                        item.getName(),
                        item.getGender(),
                        item.getAddress(),
                        item.getAge(),
                        item.getUpdatedAt(),
                        item.getCreatedAt()))
                .collect(Collectors.toList());

        rs.setResult(listUser);

        return rs;
    }

    public User fetchUserById(long id) {
        return this.userRepository.findById(id).orElse(null);
        // Optional<User> userOptional = this.userRepository.findById(id);
        // if (userOptional.isPresent()) {
        // return userOptional.get();
        // }
        // return null;
    }

    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public User handleCreateUser(User user) {
        return this.userRepository.save(user);
    }

    public User handleUpdateUser(User user) {
        User currentUser = this.fetchUserById(user.getId());
        if (currentUser != null) {
            currentUser.setAddress(user.getAddress());
            currentUser.setGender(user.getGender());
            currentUser.setAge(user.getAge());
            currentUser.setName(user.getName());

            currentUser = this.userRepository.save(currentUser);
        }
        return currentUser;
    }

    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public boolean isEmailExists(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public ResponseCreateUserDTO convertToResponseCreateUserDTO(User user) {
        ResponseCreateUserDTO res = new ResponseCreateUserDTO();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setAge(user.getAge());
        res.setCreatedAt(user.getCreatedAt());
        return res;
    }

    public ResponseUpdateUserDTO convertToResponseUpdateUserDTO(User user) {
        ResponseUpdateUserDTO res = new ResponseUpdateUserDTO();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setAge(user.getAge());
        res.setUpdatedAt(user.getUpdatedAt());
        return res;

    }

    public ResponseUserDTO convertToResponseUserDTO(User user) {
        ResponseUserDTO res = new ResponseUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setAge(user.getAge());
        res.setCreatedAt(user.getCreatedAt());
        res.setUpdatedAt(user.getUpdatedAt());
        return res;
    }

    /*
     * function to update refresh token of user
     */

    public void updateUserRefreshToken(String token, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }
}
