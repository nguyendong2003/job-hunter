package vn.nguyendong.jobhunter.domain.response.resume;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseCreateResumeDTO {
    private long id;
    private Instant createdAt;
    private String createdBy;
}
