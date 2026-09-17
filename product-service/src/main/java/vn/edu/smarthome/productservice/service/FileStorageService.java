package vn.edu.smarthome.productservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // Thư mục gốc để lưu file (cùng cấp với project khi chạy)
    private static final String UPLOAD_DIR = "uploads";

    /**
     * Lưu file và trả về đường dẫn tương đối (vd: "uploads/uuid_filename.png")
     */
    public String saveFile(MultipartFile file) throws IOException {
        // Kiểm tra file rỗng
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file duy nhất để tránh trùng
        String uniqueName = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
        Path target = uploadPath.resolve(uniqueName);

        // Ghi file
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn lưu trong DB
        return UPLOAD_DIR + "/" + uniqueName;
    }

    /**
     * Loại bỏ ký tự có thể gây lỗi đường dẫn
     */
    private String sanitize(String original) {
        if (original == null) return "file";
        return original.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
