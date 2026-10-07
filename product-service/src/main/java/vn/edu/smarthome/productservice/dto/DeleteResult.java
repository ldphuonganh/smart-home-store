package vn.edu.smarthome.productservice.dto;

/**
 * Kết quả xoá sản phẩm: sản phẩm đã có trong đơn hàng thì không xoá cứng
 * mà chuyển sang ngừng bán (deactivated = true) để giữ lịch sử đơn.
 */
public record DeleteResult(boolean deleted, boolean deactivated, String message) {
}
