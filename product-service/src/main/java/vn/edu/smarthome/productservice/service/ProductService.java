package vn.edu.smarthome.productservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smarthome.productservice.dto.PageResponse;
import vn.edu.smarthome.productservice.dto.ProductRequest;
import vn.edu.smarthome.productservice.dto.ProductResponse;
import vn.edu.smarthome.productservice.entity.Category;
import vn.edu.smarthome.productservice.entity.Product;
import vn.edu.smarthome.productservice.exception.BadRequestException;
import vn.edu.smarthome.productservice.exception.ConflictException;
import vn.edu.smarthome.productservice.exception.ResourceNotFoundException;
import vn.edu.smarthome.productservice.repository.ProductRepository;

import java.math.BigDecimal;

import static vn.edu.smarthome.productservice.repository.ProductSpecifications.*;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    // ==================== ĐỌC DỮ LIỆU ====================

    /**
     * Tìm kiếm + lọc + phân trang. Mọi tham số lọc đều không bắt buộc.
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, Long categoryId,
                                                BigDecimal minPrice, BigDecimal maxPrice,
                                                Boolean inStock, Pageable pageable) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Giá tối thiểu không được lớn hơn giá tối đa");
        }
        Specification<Product> spec = Specification.where(nameContains(keyword))
                .and(inCategory(categoryId))
                .and(priceFrom(minPrice))
                .and(priceTo(maxPrice))
                .and(inStockOnly(inStock));
        Page<ProductResponse> page = productRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    // ==================== ADMIN: THÊM / SỬA / XOÁ ====================

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String name = request.getName().trim();
        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Tên sản phẩm đã tồn tại");
        }
        Product product = new Product();
        applyRequest(product, request, name);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findEntity(id);
        String name = request.getName().trim();
        if (productRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Tên sản phẩm đã tồn tại");
        }
        applyRequest(product, request, name);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = findEntity(id);
        productRepository.delete(product);
        // Đơn hàng cũ không bị ảnh hưởng vì order-service lưu snapshot tên + giá.
        fileStorageService.deleteIfLocal(product.getImageUrl());
    }

    @Transactional
    public ProductResponse updateImage(Long id, org.springframework.web.multipart.MultipartFile file) {
        Product product = findEntity(id);
        String oldImage = product.getImageUrl();
        product.setImageUrl(fileStorageService.saveImage(file));
        ProductResponse response = toResponse(productRepository.save(product));
        fileStorageService.deleteIfLocal(oldImage);
        return response;
    }

    // ==================== API NỘI BỘ: TỒN KHO ====================

    /**
     * Trừ tồn kho khi order-service tạo đơn.
     * Dùng câu UPDATE có điều kiện "stock >= quantity" nên không bao giờ bị âm kho,
     * kể cả khi nhiều đơn đặt cùng lúc. Hết hàng -> 409.
     */
    @Transactional
    public ProductResponse reserveStock(Long id, int quantity) {
        Product product = findEntity(id);
        int updated = productRepository.decreaseStock(id, quantity);
        if (updated == 0) {
            throw new ConflictException("Sản phẩm '" + product.getName() + "' không đủ hàng (còn "
                    + product.getStock() + ", cần " + quantity + ")");
        }
        return toResponse(findEntity(id));
    }

    /** Hoàn tồn kho khi đơn bị huỷ hoặc tạo đơn thất bại giữa chừng. */
    @Transactional
    public ProductResponse releaseStock(Long id, int quantity) {
        findEntity(id);
        productRepository.increaseStock(id, quantity);
        return toResponse(findEntity(id));
    }

    // ==================== HÀM PHỤ ====================

    private Product findEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm id = " + id));
    }

    private void applyRequest(Product product, ProductRequest request, String trimmedName) {
        Category category = categoryService.findEntity(request.getCategoryId());
        product.setName(trimmedName);
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setStock(request.getStock());
        product.setCategory(category);
        // Chỉ ghi đè ảnh khi client gửi imageUrl (ảnh upload qua API riêng /products/{id}/image)
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            product.setImageUrl(request.getImageUrl().trim());
        }
    }

    private ProductResponse toResponse(Product product) {
        Category category = product.getCategory();
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .stock(product.getStock())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
