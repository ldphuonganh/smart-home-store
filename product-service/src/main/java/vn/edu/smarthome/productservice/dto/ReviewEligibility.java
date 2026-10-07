package vn.edu.smarthome.productservice.dto;

/**
 * Khách hiện tại có được đánh giá sản phẩm không.
 * canReview = đã mua & nhận hàng thành công && chưa đánh giá.
 */
public record ReviewEligibility(boolean canReview, boolean purchased, boolean reviewed) {
}
