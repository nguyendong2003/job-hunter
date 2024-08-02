package vn.nguyendong.jobhunter.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ResponseLoginDTO {
    @JsonProperty("access_token")
    private String accessToken;

    private UserLogin user;

    // inner class (nested class) (java core)
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserLogin {
        private long id;
        private String email;
        private String name;
    }

    // class này để khi http://localhost:8080/api/v1/auth/account thì có key "user"
    // bọc bên ngoài id, email, name ở phần data response
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserGetAccount {
        private UserLogin user;
    }

}
