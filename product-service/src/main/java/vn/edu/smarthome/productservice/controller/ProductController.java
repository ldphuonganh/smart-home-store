package vn.edu.smarthome.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.smarthome.productservice.dto.PageResponse;
import vn.edu.smarthome.productservice.dto.ProductRequest;
import vn.edu.smarthome.productservice.dto.ProductResponse;
import vn.edu.smarthome.productservice.service.FileStorageService;
import vn.edu.smarthome.productservice.service.ProductService;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * API sản phẩm cho Client (qua Gateway: /api/products/**).
 * GET: public. POST/PUT/DELETE: chỉ ADMIN (cấu hình trong SecurityConfig).
 * Controller chỉ nhận request và gọi Service; lỗi do GlobalExceptionHandler xử lý.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;

    /**
     * GET /products?keyword=&categoryId=&minPrice=&maxPrice=&inStock=&page=0&size=12&sort=price,asc
     */
    @GetMapping
    public PageResponse<ProductResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.search(keyword, categoryId, minPrice, maxPrice, inStock, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    /** POST /products/{id}/image (multipart/form-data, field "file") - ADMIN. */
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return productService.updateImage(id, file);
    }

    /** GET /products/images/{fileName} - public, trả về file ảnh đã upload. */
    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        Resource image = fileStorageService.loadImage(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileStorageService.probeContentType(fileName)))
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .body(image);
    }
}
