package vn.nguyendong.jobhunter.domain.dto;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.nguyendong.jobhunter.util.constant.GenderEnum;

@Getter
@Setter
public class ResponseUpdateUserDTO {
    private long id;
    private String name;
    private GenderEnum gender;
    private String address;
    private int age;
    private Instant updatedAt;
}
