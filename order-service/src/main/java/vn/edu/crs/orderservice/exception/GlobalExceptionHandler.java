package vn.edu.crs.orderservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * ================================
     * 404 - NOT FOUND
     * ================================
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            NoSuchElementException ex,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }

    /*
     * ================================
     * 403 - FORBIDDEN
     * ================================
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Bạn không có quyền thực hiện thao tác này",
                request
        );
    }

    /*
     * ================================
     * 409 - CONFLICT
     * ================================
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(
            IllegalStateException ex,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
    }

    /*
     * ================================
     * 400 - BAD REQUEST
     * ================================
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    /*
     * ================================
     * 400 - VALIDATION ERROR
     * ================================
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .findFirst()
                        .map(error -> error.getDefaultMessage())
                        .orElse("Dữ liệu không hợp lệ");

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    /*
     * ================================
     * 400 - CONSTRAINT VIOLATION
     * ================================
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {

        String message =
                ex.getConstraintViolations()
                        .stream()
                        .findFirst()
                        .map(violation ->
                                violation.getMessage()
                        )
                        .orElse("Dữ liệu không hợp lệ");

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    /*
     * ================================
     * 500 - INTERNAL SERVER ERROR
     * ================================
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception ex,
            HttpServletRequest request
    ) {

        /*
         * In lỗi ra console để dễ debug trong quá trình phát triển.
         */
        ex.printStackTrace();

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Lỗi máy chủ",
                request
        );
    }

    /*
     * ================================
     * HÀM TẠO ERROR RESPONSE
     * ================================
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {

        Map<String, Object> errorResponse =
                new LinkedHashMap<>();

        errorResponse.put(
                "timestamp",
                LocalDateTime.now()
        );

        errorResponse.put(
                "status",
                status.value()
        );

        errorResponse.put(
                "error",
                status.getReasonPhrase()
        );

        errorResponse.put(
                "message",
                message != null
                        ? message
                        : status.getReasonPhrase()
        );

        errorResponse.put(
                "path",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }
}