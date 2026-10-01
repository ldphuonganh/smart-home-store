package vn.edu.smarthome.paymentservice.exception;

/** Tài khoản bị khoá / thao tác không được phép -> HTTP 403. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
