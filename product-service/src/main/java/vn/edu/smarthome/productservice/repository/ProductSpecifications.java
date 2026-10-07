package vn.edu.smarthome.productservice.repository;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import vn.edu.smarthome.productservice.entity.Product;
import vn.edu.smarthome.productservice.entity.Review;

import java.math.BigDecimal;

/**
 * Các điều kiện lọc, ghép động tuỳ theo tham số client gửi lên.
 * Tham số nào null/rỗng thì bỏ qua điều kiện đó.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    /** Tìm theo tên, thương hiệu hoặc mô tả (giống trang sản phẩm SmartHome Store). */
    public static Specification<Product> keywordMatches(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String like = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("brand"), "")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("description"), "")), like));
        };
    }

    /** Chỉ tìm theo tên (trang quản trị). */
    public static Specification<Product> nameContains(String keyword) {
        return (root, query, cb) -> (keyword == null || keyword.isBlank())
                ? null
                : cb.like(cb.lower(root.get("name")), "%" + keyword.trim().toLowerCase() + "%");
    }

    public static Specification<Product> inCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null
                ? null
                : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasBrand(String brand) {
        return (root, query, cb) -> (brand == null || brand.isBlank())
                ? null
                : cb.equal(root.get("brand"), brand.trim());
    }

    public static Specification<Product> priceFrom(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceTo(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null
                ? null
                : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> inStockOnly(Boolean inStock) {
        return (root, query, cb) -> Boolean.TRUE.equals(inStock)
                ? cb.greaterThan(root.get("stock"), 0)
                : null;
    }

    /** active = null: lấy tất cả (admin); true/false: lọc theo trạng thái bán. */
    public static Specification<Product> activeIs(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    // ===== Đánh giá (trang quản trị) =====

    public static Specification<Review> reviewSearch(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String like = "%" + keyword.trim().toLowerCase() + "%";
            var product = root.join("product", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(cb.coalesce(root.get("comment"), "")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("userName"), "")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("userEmail"), "")), like),
                    cb.like(cb.lower(product.get("name")), like));
        };
    }

    public static Specification<Review> reviewRating(Integer rating) {
        return (root, query, cb) -> rating == null ? null : cb.equal(root.get("rating"), rating);
    }

    public static Specification<Review> reviewApproved(Boolean approved) {
        return (root, query, cb) -> approved == null ? null : cb.equal(root.get("approved"), approved);
    }
}
