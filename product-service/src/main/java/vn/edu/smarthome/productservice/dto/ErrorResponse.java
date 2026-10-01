package vn.edu.smarthome.productservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Cấu trúc lỗi thống nhất của cả nhóm (Tài liệu thống nhất - mục 27):
 * { timestamp, status, error, message, path }
 * Riêng lỗi validation có thêm "errors": { tenField: thongBao } để Frontend
 * hiển thị lỗi ngay dưới từng ô nhập.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> errors;
}
