package vn.edu.smarthome.paymentservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import vn.edu.smarthome.paymentservice.dto.ErrorResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Tạo ErrorResponse theo đúng format chung, dùng cho cả
 * GlobalExceptionHandler (lỗi trong Controller) và SecurityConfig (lỗi 401/403
 * xảy ra trước khi request vào tới Controller).
 */
public final class ErrorResponses {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private ErrorResponses() {
    }

    public static ErrorResponse build(HttpStatus status, String message, String path,
                                      Map<String, String> fieldErrors) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message != null ? message : status.getReasonPhrase(),
                path,
                fieldErrors
        );
    }

    public static ResponseEntity<ErrorResponse> entity(HttpStatus status, String message,
                                                       HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(build(status, message, request.getRequestURI(), null));
    }

    /** Ghi thẳng JSON lỗi ra response (dùng trong Spring Security filter chain). */
    public static void write(HttpServletResponse response, HttpStatus status, String message,
                             String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        MAPPER.writeValue(response.getWriter(), build(status, message, path, null));
    }
}
