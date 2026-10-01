package vn.edu.smarthome.paymentservice.exception;

/** Không tìm thấy dữ liệu -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
