package vn.edu.smarthome.paymentservice.exception;

/** Sai email/mật khẩu -> HTTP 401. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
