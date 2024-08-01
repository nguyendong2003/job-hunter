package vn.nguyendong.jobhunter.domain.dto;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.nguyendong.jobhunter.util.constant.GenderEnum;

@Getter
@Setter
public class ResponseCreateUserDTO {
    private long id;
    private String name;
    private String email;
    private GenderEnum gender;
    private String address;
    private int age;
    private Instant createdAt;
}
