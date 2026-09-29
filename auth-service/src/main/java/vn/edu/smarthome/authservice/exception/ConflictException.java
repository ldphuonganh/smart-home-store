package vn.edu.smarthome.authservice.exception;

/** Xung đột dữ liệu (trùng tên, hết hàng, danh mục còn sản phẩm...) -> HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
