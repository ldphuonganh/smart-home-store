package vn.edu.smarthome.productservice.repository;

import org.springframework.data.jpa.domain.Specification;
import vn.edu.smarthome.productservice.entity.Product;

import java.math.BigDecimal;

/**
 * Các điều kiện lọc sản phẩm, ghép động tuỳ theo tham số client gửi lên.
 * Tham số nào null thì bỏ qua điều kiện đó.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

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
}
