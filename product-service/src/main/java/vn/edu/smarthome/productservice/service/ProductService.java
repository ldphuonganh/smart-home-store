package vn.edu.smarthome.productservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.smarthome.productservice.dto.DeleteResult;
import vn.edu.smarthome.productservice.dto.PageResponse;
import vn.edu.smarthome.productservice.dto.ProductRequest;
import vn.edu.smarthome.productservice.dto.ProductResponse;
import vn.edu.smarthome.productservice.entity.Category;
import vn.edu.smarthome.productservice.entity.Product;
import vn.edu.smarthome.productservice.entity.ProductImage;
import vn.edu.smarthome.productservice.exception.BadRequestException;
import vn.edu.smarthome.productservice.exception.ConflictException;
import vn.edu.smarthome.productservice.exception.ResourceNotFoundException;
import vn.edu.smarthome.productservice.repository.ProductImageRepository;
import vn.edu.smarthome.productservice.repository.ProductRepository;
import vn.edu.smarthome.productservice.repository.ReviewRepository;
import vn.edu.smarthome.productservice.util.SlugUtil;

import java.math.BigDecimal;
import java.util.*;

import static vn.edu.smarthome.productservice.repository.ProductSpecifications.*;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ReviewRepository reviewRepository;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    // ==================== ĐỌC DỮ LIỆU (CỬA HÀNG) ====================

    /**
     * Trang sản phẩm: tìm theo tên/thương hiệu/mô tả, lọc danh mục, thương hiệu, khoảng giá,
     * sắp xếp (newest | price_asc | price_desc | name_asc | name_desc). Chỉ sản phẩm đang bán.
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, Long categoryId, String brand,
                                                BigDecimal minPrice, BigDecimal maxPrice,
                                                Boolean inStock, String sort, int page, int size) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Giá tối thiểu không được lớn hơn giá tối đa");
        }
        Specification<Product> spec = Specification.where(keywordMatches(keyword))
                .and(inCategory(categoryId))
                .and(hasBrand(brand))
                .and(priceFrom(minPrice))
                .and(priceTo(maxPrice))
                .and(inStockOnly(inStock))
                .and(activeIs(true));
        return toPage(productRepository.findAll(spec, pageRequest(page, size, sort)));
    }

    /** Giữ lại chữ ký cũ cho Public API (Partner) - dùng Pageable của Spring. */
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchPublic(String keyword, Long categoryId,
                                                      BigDecimal minPrice, BigDecimal maxPrice,
                                                      Boolean inStock, Pageable pageable) {
        Specification<Product> spec = Specification.where(keywordMatches(keyword))
                .and(inCategory(categoryId))
                .and(priceFrom(minPrice))
                .and(priceTo(maxPrice))
                .and(inStockOnly(inStock))
                .and(activeIs(true));
        return toPage(productRepository.findAll(spec, pageable));
    }

    /** Trang quản trị: tìm theo tên, lọc danh mục, trạng thái (active = null: tất cả). */
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> adminSearch(String keyword, Long categoryId, Boolean active,
                                                     int page, int size) {
        Specification<Product> spec = Specification.where(nameContains(keyword))
                .and(inCategory(categoryId))
                .and(activeIs(active));
        return toPage(productRepository.findAll(spec, pageRequest(page, size, "newest")));
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return toResponse(findEntity(id), true);
    }

    /** Trang chi tiết theo slug; sản phẩm ngừng bán không truy cập được (trừ admin). */
    @Transactional(readOnly = true)
    public ProductResponse getBySlug(String slug, boolean isAdmin) {
        Product product = productRepository.findBySlug(slug)
                .filter(p -> p.isActive() || isAdmin)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        return toResponse(product, true);
    }

    /** Chi tiết theo id cho cửa hàng: ẩn sản phẩm ngừng bán với khách. */
    @Transactional(readOnly = true)
    public ProductResponse getVisibleById(Long id, boolean isAdmin) {
        Product product = findEntity(id);
        if (!product.isActive() && !isAdmin) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm id = " + id);
        }
        return toResponse(product, true);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> related(Long id, int limit) {
        Product product = findEntity(id);
        if (product.getCategory() == null) {
            return List.of();
        }
        return toList(productRepository.findByCategoryIdAndIdNotAndActiveTrue(
                product.getCategory().getId(), id, PageRequest.of(0, clamp(limit, 1, 20))));
    }

    /** Sản phẩm bán chạy (trang chủ). */
    @Transactional(readOnly = true)
    public List<ProductResponse> bestSellers(int limit) {
        return toList(productRepository.findByActiveTrue(PageRequest.of(0, clamp(limit, 1, 20),
                Sort.by(Sort.Order.desc("soldCount"), Sort.Order.desc("id")))));
    }

    /** Sản phẩm mới nhất (trang chủ). */
    @Transactional(readOnly = true)
    public List<ProductResponse> latest(int limit) {
        return toList(productRepository.findByActiveTrue(PageRequest.of(0, clamp(limit, 1, 20),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")))));
    }

    @Transactional(readOnly = true)
    public List<String> brands() {
        return productRepository.findActiveBrands();
    }

    /** Nhiều sản phẩm theo danh sách id (cart-service / wishlist dùng). */
    @Transactional(readOnly = true)
    public List<ProductResponse> getByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return toList(productRepository.findByIdIn(ids));
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
        product.setSlug(uniqueSlug(name, null));
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            product.addImage(new ProductImage(request.getImageUrl().trim(), true, 0));
        }
        product.syncPrimaryImage();
        return toResponse(productRepository.save(product), true);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findEntity(id);
        String name = request.getName().trim();
        if (productRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Tên sản phẩm đã tồn tại");
        }
        applyRequest(product, request, name);
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank() && product.getImages().isEmpty()) {
            product.addImage(new ProductImage(request.getImageUrl().trim(), true, 0));
            product.syncPrimaryImage();
        }
        return toResponse(productRepository.save(product), true);
    }

    /**
     * Sản phẩm đã có trong đơn hàng thì KHÔNG xoá cứng (mất lịch sử đơn, sai doanh thu)
     * mà chuyển sang ngừng bán - giống SmartHome Store.
     */
    @Transactional
    public DeleteResult delete(Long id) {
        Product product = findEntity(id);
        if (product.isOrdered()) {
            product.setActive(false);
            productRepository.save(product);
            return new DeleteResult(false, true,
                    "Sản phẩm đã có đơn hàng nên được chuyển sang trạng thái ngừng bán thay vì xóa.");
        }
        List<String> files = product.getImages().stream().map(ProductImage::getImageUrl).toList();
        reviewRepository.deleteByProductId(id);
        productRepository.delete(product);
        files.forEach(fileStorageService::deleteIfLocal);
        return new DeleteResult(true, false, "Xóa sản phẩm thành công!");
    }

    /** Bật/tắt trạng thái bán. */
    @Transactional
    public ProductResponse toggleActive(Long id) {
        Product product = findEntity(id);
        product.setActive(!product.isActive());
        return toResponse(productRepository.save(product), true);
    }

    // ==================== ADMIN: ẢNH SẢN PHẨM ====================

    /** Thêm nhiều ảnh (không xoá ảnh cũ); sản phẩm chưa có ảnh thì ảnh đầu làm ảnh đại diện. */
    @Transactional
    public ProductResponse addImages(Long id, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn ít nhất 1 ảnh");
        }
        Product product = findEntity(id);
        int order = product.getImages().stream().mapToInt(ProductImage::getSortOrder).max().orElse(-1) + 1;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            product.addImage(new ProductImage(fileStorageService.saveImage(file), false, order++));
        }
        product.syncPrimaryImage();
        return toResponse(productRepository.save(product), true);
    }

    /** API cũ: upload 1 ảnh và đặt làm ảnh đại diện. */
    @Transactional
    public ProductResponse updateImage(Long id, MultipartFile file) {
        Product product = findEntity(id);
        ProductImage image = new ProductImage(fileStorageService.saveImage(file), false, 0);
        product.getImages().forEach(img -> img.setSortOrder(img.getSortOrder() + 1));
        product.addImage(image);
        product.getImages().forEach(img -> img.setPrimary(img == image));
        product.syncPrimaryImage();
        return toResponse(productRepository.save(product), true);
    }

    @Transactional
    public ProductResponse setPrimaryImage(Long productId, Long imageId) {
        Product product = findEntity(productId);
        ProductImage target = findImage(productId, imageId);
        product.getImages().forEach(img -> img.setPrimary(img.getId().equals(target.getId())));
        product.syncPrimaryImage();
        return toResponse(productRepository.save(product), true);
    }

    @Transactional
    public ProductResponse deleteImage(Long productId, Long imageId) {
        Product product = findEntity(productId);
        ProductImage target = findImage(productId, imageId);
        product.getImages().removeIf(img -> img.getId().equals(target.getId()));
        product.syncPrimaryImage();
        Product saved = productRepository.save(product);
        fileStorageService.deleteIfLocal(target.getImageUrl());
        return toResponse(saved, true);
    }

    // ==================== API NỘI BỘ: TỒN KHO / ĐÃ BÁN ====================

    /**
     * Trừ tồn kho khi order-service tạo đơn. Câu UPDATE có điều kiện "stock >= quantity"
     * nên không bao giờ âm kho kể cả khi nhiều đơn đặt cùng lúc. Hết hàng / ngừng bán -> 409.
     */
    @Transactional
    public ProductResponse reserveStock(Long id, int quantity) {
        Product product = findEntity(id);
        if (!product.isActive()) {
            throw new ConflictException("Sản phẩm '" + product.getName() + "' đã ngừng kinh doanh");
        }
        int updated = productRepository.decreaseStock(id, quantity);
        if (updated == 0) {
            throw new ConflictException("Sản phẩm '" + product.getName() + "' không đủ hàng (còn "
                    + product.getStock() + ", cần " + quantity + ")");
        }
        return toResponse(findEntity(id), false);
    }

    /** Hoàn tồn kho khi đơn bị huỷ hoặc tạo đơn thất bại giữa chừng. */
    @Transactional
    public ProductResponse releaseStock(Long id, int quantity) {
        findEntity(id);
        productRepository.increaseStock(id, quantity);
        return toResponse(findEntity(id), false);
    }

    @Transactional
    public void increaseSold(Long id, int quantity) {
        findEntity(id);
        productRepository.increaseSold(id, quantity);
    }

    // ==================== HÀM PHỤ ====================

    @Transactional(readOnly = true)
    public Product findEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm id = " + id));
    }

    private ProductImage findImage(Long productId, Long imageId) {
        return productImageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ảnh id = " + imageId));
    }

    private void applyRequest(Product product, ProductRequest request, String trimmedName) {
        Category category = categoryService.findEntity(request.getCategoryId());
        product.setName(trimmedName);
        product.setPrice(request.getPrice());
        product.setDescription(blankToNull(request.getDescription()));
        product.setStock(request.getStock());
        product.setCategory(category);
        product.setBrand(blankToNull(request.getBrand()));
        product.setMaterial(blankToNull(request.getMaterial()));
        product.setWeight(blankToNull(request.getWeight()));
        product.setPower(blankToNull(request.getPower()));
        product.setOrigin(blankToNull(request.getOrigin()));
        product.setActive(request.getActive() == null || request.getActive());
    }

    /** Slug duy nhất: trùng thì thêm hậu tố -2, -3... */
    public String uniqueSlug(String name, Long excludeId) {
        String base = SlugUtil.slugify(name);
        String slug = base;
        int i = 2;
        while (excludeId == null ? productRepository.existsBySlug(slug)
                : productRepository.existsBySlugAndIdNot(slug, excludeId)) {
            slug = base + "-" + i++;
        }
        return slug;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static Pageable pageRequest(int page, int size, String sort) {
        Sort s = switch (sort == null ? "" : sort) {
            case "price_asc" -> Sort.by(Sort.Order.asc("price"), Sort.Order.desc("id"));
            case "price_desc" -> Sort.by(Sort.Order.desc("price"), Sort.Order.desc("id"));
            case "name_asc" -> Sort.by(Sort.Order.asc("name"));
            case "name_desc" -> Sort.by(Sort.Order.desc("name"));
            case "best_selling" -> Sort.by(Sort.Order.desc("soldCount"), Sort.Order.desc("id"));
            default -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        };
        return PageRequest.of(Math.max(page, 0), clamp(size, 1, 100), s);
    }

    private PageResponse<ProductResponse> toPage(Page<Product> page) {
        Map<Long, double[]> stats = ratingStats(page.getContent());
        return PageResponse.from(page.map(p -> toResponse(p, false, stats)));
    }

    private List<ProductResponse> toList(List<Product> products) {
        Map<Long, double[]> stats = ratingStats(products);
        return products.stream().map(p -> toResponse(p, false, stats)).toList();
    }

    private Map<Long, double[]> ratingStats(Collection<Product> products) {
        if (products.isEmpty()) {
            return Map.of();
        }
        Map<Long, double[]> map = new HashMap<>();
        for (Object[] row : reviewRepository.ratingStats(products.stream().map(Product::getId).toList())) {
            map.put((Long) row[0], new double[]{((Number) row[1]).doubleValue(), ((Number) row[2]).doubleValue()});
        }
        return map;
    }

    private ProductResponse toResponse(Product product, boolean withImages) {
        return toResponse(product, withImages, ratingStats(List.of(product)));
    }

    private ProductResponse toResponse(Product product, boolean withImages, Map<Long, double[]> stats) {
        Category category = product.getCategory();
        double[] stat = stats.getOrDefault(product.getId(), new double[]{0, 0});
        int stock = product.getStock() == null ? 0 : product.getStock();
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .brand(product.getBrand())
                .material(product.getMaterial())
                .weight(product.getWeight())
                .power(product.getPower())
                .origin(product.getOrigin())
                .price(product.getPrice())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .images(withImages ? product.getImages().stream()
                        .map(img -> new ProductResponse.ImageResponse(img.getId(), img.getImageUrl(), img.isPrimary()))
                        .toList() : null)
                .stock(stock)
                .stockStatus(stock > 10 ? "Còn hàng" : stock > 0 ? "Sắp hết hàng" : "Hết hàng")
                .active(product.isActive())
                .soldCount(product.getSoldCount())
                .averageRating(Math.round(stat[0] * 10) / 10.0)
                .reviewCount((long) stat[1])
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .categorySlug(category != null ? category.getSlug() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
