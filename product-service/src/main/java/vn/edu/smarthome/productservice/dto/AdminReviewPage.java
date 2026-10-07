package vn.edu.smarthome.productservice.dto;

import java.util.Map;

/** Danh sách đánh giá cho trang quản trị + thống kê theo số sao. */
public record AdminReviewPage(PageResponse<ReviewResponse> reviews, Map<String, Long> stats) {
}
