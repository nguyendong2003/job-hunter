package vn.nguyendong.jobhunter.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.nguyendong.jobhunter.domain.Company;
import vn.nguyendong.jobhunter.domain.Role;
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
    private final CompanyService companyService;
    private final RoleService roleService;

    public UserService(UserRepository userRepository, CompanyService companyService, RoleService roleService) {
        this.userRepository = userRepository;
        this.companyService = companyService;
        this.roleService = roleService;
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

        // remove sensitive data
        // data trả về không chứa thông tin mật khẩu, refresh token của người dùng)
        List<ResponseUserDTO> listUser = pageUser.getContent()
                .stream().map(item -> this.convertToResponseUserDTO(item))
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

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public User handleCreateUser(User user) {

        /*
         * check company (nếu có truyền id company thì lấy ra company đó, nếu tồn tại
         * company thì set company cho user, nếu không truyền hoặc truyền sai thì set
         * company = null)
         */
        if (user.getCompany() != null) {
            Optional<Company> companyOptional = this.companyService.findById(user.getCompany().getId());
            user.setCompany(companyOptional.isPresent() ? companyOptional.get() : null);
        }

        // check role (kiểm tra role có tồn tại không, nếu có thì set role cho user, nếu
        // không thì set role = null)
        if (user.getRole() != null) {
            Role r = this.roleService.fetchById(user.getRole().getId());
            user.setRole(r != null ? r : null);
        }

        return this.userRepository.save(user);

    }

    public User handleUpdateUser(User user) {
        User currentUser = this.fetchUserById(user.getId());
        if (currentUser != null) {
            currentUser.setAddress(user.getAddress());
            currentUser.setGender(user.getGender());
            currentUser.setAge(user.getAge());
            currentUser.setName(user.getName());

            // check company (tương tự như create user)
            if (user.getCompany() != null) {
                Optional<Company> companyOptional = this.companyService.findById(user.getCompany().getId());
                currentUser.setCompany(companyOptional.isPresent() ? companyOptional.get() : null);
            }

            // check role
            if (user.getRole() != null) {
                Role r = this.roleService.fetchById(user.getRole().getId());
                currentUser.setRole(r != null ? r : null);
            }

            // update
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
        ResponseCreateUserDTO.CompanyUser company = new ResponseCreateUserDTO.CompanyUser();

        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setAge(user.getAge());
        res.setCreatedAt(user.getCreatedAt());

        if (user.getCompany() != null) {
            company.setId(user.getCompany().getId());
            company.setName(user.getCompany().getName());
            res.setCompany(company);
        }

        return res;
    }

    public ResponseUpdateUserDTO convertToResponseUpdateUserDTO(User user) {
        ResponseUpdateUserDTO res = new ResponseUpdateUserDTO();
        ResponseUpdateUserDTO.CompanyUser company = new ResponseUpdateUserDTO.CompanyUser();

        res.setId(user.getId());
        res.setName(user.getName());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setAge(user.getAge());
        res.setUpdatedAt(user.getUpdatedAt());

        if (user.getCompany() != null) {
            company.setId(user.getCompany().getId());
            company.setName(user.getCompany().getName());
            res.setCompany(company);
        }

        return res;

    }

    public ResponseUserDTO convertToResponseUserDTO(User user) {
        ResponseUserDTO res = new ResponseUserDTO();
        ResponseUserDTO.CompanyUser company = new ResponseUserDTO.CompanyUser();
        ResponseUserDTO.RoleUser roleUser = new ResponseUserDTO.RoleUser();

        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setAge(user.getAge());
        res.setCreatedAt(user.getCreatedAt());
        res.setUpdatedAt(user.getUpdatedAt());

        if (user.getCompany() != null) {
            company.setId(user.getCompany().getId());
            company.setName(user.getCompany().getName());
            res.setCompany(company);
        }

        if (user.getRole() != null) {
            roleUser.setId(user.getRole().getId());
            roleUser.setName(user.getRole().getName());
            res.setRole(roleUser);
        }

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
