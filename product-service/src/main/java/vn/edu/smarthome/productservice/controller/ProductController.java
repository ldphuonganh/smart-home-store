package vn.edu.smarthome.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smarthome.productservice.dto.ProductDTO;
import vn.edu.smarthome.productservice.service.ProductService;
import vn.edu.smarthome.productservice.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;   // ← thêm dòng này

    // ==================== GET: Lấy danh sách + Tìm kiếm + Phân trang ====================

    /**
     * GET /products?keyword=...&page=0&size=10&sort=price,desc
     * Public - ai cũng xem được
     */
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getProducts(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(keyword, pageable));
    }

    /**
     * GET /products/{id}
     * Public - ai cũng xem được
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ==================== POST: Tạo sản phẩm - chỉ ADMIN ====================

    /**
     * POST /products?role=ADMIN
     * Body: { "name": "...", "price": ..., "categoryId": ... }
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO dto) {
        try {
            ProductDTO created = productService.createProduct(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ==================== PUT: Cập nhật sản phẩm - chỉ ADMIN ====================

    /**
     * PUT /products/{id}?role=ADMIN
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO dto) {
        try {
            return productService.updateProduct(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ==================== DELETE: Xóa sản phẩm - chỉ ADMIN ====================

    /**
     * DELETE /products/{id}?role=ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted
                ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // ==================== ENDPOINTS THEO CATEGORY ====================

    /**
     * GET /products/category/{categoryId}
     * Public - lấy tất cả sản phẩm của 1 danh mục
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    /**
     * POST /products/category/{categoryId}?role=ADMIN
     * Tạo sản phẩm trực tiếp trong 1 category
     */
    @PostMapping("/category/{categoryId}")
    public ResponseEntity<?> createInCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody ProductDTO dto) {
        try {
            return productService.createProductInCategory(categoryId, dto)
                    .map(p -> ResponseEntity.status(HttpStatus.CREATED).body(p))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ==================== UPLOAD ẢNH ====================

    /**
     * POST /products/{id}/upload-image?role=ADMIN
     * Form-data: file=<ảnh>
     */
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileStorageService.saveFile(file);
            return productService.updateProductImage(id, imageUrl)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi lưu file: " + e.getMessage());
        }
    }
}