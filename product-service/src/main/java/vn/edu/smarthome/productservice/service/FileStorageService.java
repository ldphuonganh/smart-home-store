package vn.edu.smarthome.productservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.smarthome.productservice.exception.BadRequestException;
import vn.edu.smarthome.productservice.exception.ResourceNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Lưu ảnh sản phẩm vào thư mục uploads/ của product-service.
 *
 * Ảnh được phục vụ qua GET /products/images/{fileName}, nên qua Gateway sẽ là
 * GET /api/products/images/{fileName} (dùng lại route /api/products/** sẵn có,
 * không cần thêm route mới ở Gateway).
 */
@Slf4j
@Service
public class FileStorageService {

    /** Đường dẫn công khai (qua Gateway) được lưu vào DB. */
    public static final String PUBLIC_PREFIX = "/api/products/images/";

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File ảnh không được để trống");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Chỉ chấp nhận file ảnh (jpg, png, webp, gif)");
        }
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Chỉ chấp nhận file ảnh (jpg, png, webp, gif)");
        }

        String fileName = UUID.randomUUID() + "." + extension;
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(uploadDir);
            Files.copy(in, uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Không thể lưu file ảnh", e);
        }
        return PUBLIC_PREFIX + fileName;
    }

    public Resource loadImage(String fileName) {
        Path file = uploadDir.resolve(fileName).normalize();
        // Chặn path traversal kiểu "../../application.properties"
        if (!file.startsWith(uploadDir) || !Files.isRegularFile(file)) {
            throw new ResourceNotFoundException("Không tìm thấy ảnh " + fileName);
        }
        try {
            return new UrlResource(file.toUri());
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Không tìm thấy ảnh " + fileName);
        }
    }

    public String probeContentType(String fileName) {
        try {
            String type = Files.probeContentType(uploadDir.resolve(fileName));
            return type != null ? type : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    /** Xoá file ảnh cũ nếu ảnh đó do service này lưu (bỏ qua ảnh là link ngoài). */
    public void deleteIfLocal(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(PUBLIC_PREFIX)) {
            return;
        }
        Path file = uploadDir.resolve(imageUrl.substring(PUBLIC_PREFIX.length())).normalize();
        if (!file.startsWith(uploadDir)) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Không xoá được ảnh cũ {}", file, e);
        }
    }

    private String getExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
