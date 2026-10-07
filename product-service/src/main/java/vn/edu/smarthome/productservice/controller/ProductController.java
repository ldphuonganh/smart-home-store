package vn.edu.smarthome.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.smarthome.productservice.dto.*;
import vn.edu.smarthome.productservice.security.AuthUser;
import vn.edu.smarthome.productservice.service.FileStorageService;
import vn.edu.smarthome.productservice.service.ProductService;
import vn.edu.smarthome.productservice.service.ReviewService;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * API sản phẩm cho Client (qua Gateway: /api/products/**).
 * GET: public. Thêm/sửa/xoá/ảnh: ADMIN. Viết đánh giá: CUSTOMER (cấu hình trong SecurityConfig).
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;
    private final FileStorageService fileStorageService;

    /**
     * GET /products?keyword=&categoryId=&brand=&minPrice=&maxPrice=&inStock=&sort=newest&page=0&size=12
     * sort: newest | price_asc | price_desc | name_asc | name_desc | best_selling
     */
    @GetMapping
    public PageResponse<ProductResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return productService.search(keyword, categoryId, brand, minPrice, maxPrice, inStock, sort, page, size);
    }

    /** GET /products/admin?keyword=&categoryId=&active=&page=0&size=10 - ADMIN (gồm cả sản phẩm ngừng bán). */
    @GetMapping("/admin")
    public PageResponse<ProductResponse> adminSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.adminSearch(keyword, categoryId, active, page, size);
    }

    @GetMapping("/brands")
    public List<String> brands() {
        return productService.brands();
    }

    @GetMapping("/best-sellers")
    public List<ProductResponse> bestSellers(@RequestParam(defaultValue = "8") int limit) {
        return productService.bestSellers(limit);
    }

    @GetMapping("/latest")
    public List<ProductResponse> latest(@RequestParam(defaultValue = "8") int limit) {
        return productService.latest(limit);
    }

    @GetMapping("/slug/{slug}")
    public ProductResponse getBySlug(@PathVariable String slug, @AuthenticationPrincipal AuthUser user) {
        return productService.getBySlug(slug, user != null && user.isAdmin());
    }

    @GetMapping("/{id:\\d+}")
    public ProductResponse getById(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return productService.getVisibleById(id, user != null && user.isAdmin());
    }

    @GetMapping("/{id:\\d+}/related")
    public List<ProductResponse> related(@PathVariable Long id, @RequestParam(defaultValue = "4") int limit) {
        return productService.related(id, limit);
    }

    // ==================== ĐÁNH GIÁ ====================

    @GetMapping("/{id:\\d+}/reviews")
    public List<ReviewResponse> reviews(@PathVariable Long id) {
        return reviewService.approvedForProduct(id);
    }

    @GetMapping("/{id:\\d+}/reviews/eligibility")
    public ReviewEligibility reviewEligibility(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return reviewService.eligibility(id, user);
    }

    @PostMapping("/{id:\\d+}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(@PathVariable Long id, @AuthenticationPrincipal AuthUser user,
                                       @Valid @RequestBody ReviewRequest request) {
        return reviewService.create(id, user, request);
    }

    // ==================== ADMIN: SẢN PHẨM ====================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id:\\d+}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    /** Xoá; sản phẩm đã có đơn hàng -> chuyển sang ngừng bán (deactivated = true). */
    @DeleteMapping("/{id:\\d+}")
    public DeleteResult delete(@PathVariable Long id) {
        return productService.delete(id);
    }

    @PatchMapping("/{id:\\d+}/toggle-active")
    public ProductResponse toggleActive(@PathVariable Long id) {
        return productService.toggleActive(id);
    }

    /** POST /products/{id}/images (multipart, field "files" - nhiều ảnh). */
    @PostMapping(value = "/{id:\\d+}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse addImages(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files) {
        return productService.addImages(id, files);
    }

    /** POST /products/{id}/image (multipart, field "file") - thêm 1 ảnh làm ảnh đại diện. */
    @PostMapping(value = "/{id:\\d+}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return productService.updateImage(id, file);
    }

    @PutMapping("/{id:\\d+}/images/{imageId:\\d+}/primary")
    public ProductResponse setPrimaryImage(@PathVariable Long id, @PathVariable Long imageId) {
        return productService.setPrimaryImage(id, imageId);
    }

    @DeleteMapping("/{id:\\d+}/images/{imageId:\\d+}")
    public ProductResponse deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        return productService.deleteImage(id, imageId);
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

    // ==================== ADMIN: ĐÁNH GIÁ ====================

    /** GET /products/reviews?search=&rating=&approved=&page=0&size=10 - ADMIN. */
    @GetMapping("/reviews")
    public AdminReviewPage adminReviews(@RequestParam(required = false) String search,
                                        @RequestParam(required = false) Integer rating,
                                        @RequestParam(required = false) Boolean approved,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return reviewService.adminList(search, rating, approved, page, size);
    }

    /** GET /products/reviews/mine - id các sản phẩm tôi đã đánh giá. */
    @GetMapping("/reviews/mine")
    public List<Long> myReviewedProductIds(@AuthenticationPrincipal AuthUser user) {
        return reviewService.reviewedProductIds(user);
    }

    @PatchMapping("/reviews/{reviewId:\\d+}/toggle")
    public ReviewResponse toggleReview(@PathVariable Long reviewId) {
        return reviewService.toggle(reviewId);
    }

    @DeleteMapping("/reviews/{reviewId:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Long reviewId) {
        reviewService.delete(reviewId);
    }
}
