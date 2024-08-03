package vn.nguyendong.jobhunter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.nguyendong.jobhunter.util.SecurityUtil;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Table(name = "companies")
@Entity
@Getter
@Setter
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "name không được để trống")
    private String name;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    private String address;
    private String logo;

    /*
     * DateTimeFormatConfiguration đã xác định Instant dùng tiêu chuẩn ISO
     * => Trong database lưu ở GMT+0
     * => khi trả về cho client cần chuyển về GMT+7 (Việt Nam)
     * 
     * 
     * @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7") => format
     * bên backend
     * 
     * => Không có nó thì bên frontend phải tự format
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;

    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore // Anotation này giúp tránh lỗi vòng lặp vô hạn khi get companies (khi query
                // Company thì không query users)
    private List<User> users;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.updatedAt = Instant.now();
    }

}
