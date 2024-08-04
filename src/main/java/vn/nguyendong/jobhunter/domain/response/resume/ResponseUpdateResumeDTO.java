package vn.nguyendong.jobhunter.domain.response.resume;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseUpdateResumeDTO {
    private Instant updatedAt;
    private String updatedBy;
}
