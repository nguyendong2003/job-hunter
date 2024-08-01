package vn.nguyendong.jobhunter.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Meta {
    private int page; // trang hiện tại
    private int pageSize; // số lượng phần tử trên mỗi trang
    private int pages; // tổng số trang chia từ trong database
    private long total; // tổng số phần tử trong database
}
