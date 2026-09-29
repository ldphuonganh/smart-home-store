package vn.edu.smarthome.authservice.exception;

/** Sai email/mật khẩu -> HTTP 401. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
