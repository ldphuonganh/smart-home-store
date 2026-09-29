package vn.edu.smarthome.productservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vn.edu.smarthome.productservice.dto.ErrorResponse;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bắt lỗi tập trung, trả về JSON theo format chung của nhóm:
 * { timestamp, status, error, message, path } (+ errors khi lỗi validation).
 * Controller KHÔNG tự try/catch nữa, cứ ném exception để lớp này xử lý.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===== 404 =====
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest request) {
        return ErrorResponses.entity(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex,
                                                          HttpServletRequest request) {
        return ErrorResponses.entity(HttpStatus.NOT_FOUND, "Không tìm thấy đường dẫn", request);
    }

    // ===== 409 =====
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex,
                                                        HttpServletRequest request) {
        return ErrorResponses.entity(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                             HttpServletRequest request) {
        return ErrorResponses.entity(HttpStatus.CONFLICT,
                "Dữ liệu bị trùng hoặc đang được sử dụng", request);
    }

    // ===== 400 =====
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex,
                                                          HttpServletRequest request) {
        return ErrorResponses.entity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        String message = fieldErrors.values().stream().findFirst().orElse("Dữ liệu không hợp lệ");
        return ResponseEntity.badRequest().body(
                ErrorResponses.build(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex,
                                                          HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .findFirst().map(v -> v.getMessage()).orElse("Dữ liệu không hợp lệ");
        return ErrorResponses.entity(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class
    })
    public ResponseEntity<ErrorResponse> handleMalformed(Exception ex, HttpServletRequest request) {
        return ErrorResponses.entity(HttpStatus.BAD_REQUEST,
                "Request không hợp lệ: thiếu hoặc sai kiểu dữ liệu", request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handleBadSort(PropertyReferenceException ex,
                                                       HttpServletRequest request) {
        return ErrorResponses.entity(HttpStatus.BAD_REQUEST,
                "Không thể sắp xếp theo trường '" + ex.getPropertyName() + "'", request);
    }

    /** Tham số sort/filter không hợp lệ bị Spring Data bọc lại. */
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidQuery(InvalidDataAccessApiUsageException ex,
                                                            HttpServletRequest request) {
        return ErrorResponses.entity(HttpStatus.BAD_REQUEST, "Tham số truy vấn không hợp lệ", request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException ex,
                                                         HttpServletRequest request) {
        return ErrorResponses.entity(HttpStatus.BAD_REQUEST, "File ảnh vượt quá dung lượng cho phép (5MB)", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethod(HttpRequestMethodNotSupportedException ex,
                                                      HttpServletRequest request) {
        return ErrorResponses.entity(HttpStatus.METHOD_NOT_ALLOWED,
                "Phương thức " + ex.getMethod() + " không được hỗ trợ", request);
    }

    // ===== 403 (phòng trường hợp dùng @PreAuthorize) =====
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                            HttpServletRequest request) {
        return ErrorResponses.entity(HttpStatus.FORBIDDEN,
                "Bạn không có quyền thực hiện thao tác này", request);
    }

    // ===== 500 =====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex, HttpServletRequest request) {
        log.error("Lỗi không xác định tại {}", request.getRequestURI(), ex);
        return ErrorResponses.entity(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi máy chủ", request);
    }
}
