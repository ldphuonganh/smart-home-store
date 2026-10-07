package vn.edu.smarthome.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.smarthome.productservice.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    /** Lấy kèm category trong 1 câu truy vấn (tránh lỗi N+1 khi hiển thị categoryName). */
    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    long countByCategoryId(Long categoryId);

    long countByActiveTrue();

    /** Danh sách thương hiệu (bộ lọc trang sản phẩm). */
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.active = true AND p.brand IS NOT NULL "
            + "AND p.brand <> '' ORDER BY p.brand")
    List<String> findActiveBrands();

    /** Sản phẩm liên quan: cùng danh mục, đang bán, trừ chính nó. */
    @EntityGraph(attributePaths = "category")
    List<Product> findByCategoryIdAndIdNotAndActiveTrue(Long categoryId, Long id, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    List<Product> findByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    List<Product> findByIdIn(List<Long> ids);

    /**
     * Trừ tồn kho NGUYÊN TỬ (atomic) bằng 1 câu UPDATE có điều kiện.
     * Chỉ trừ khi còn đủ hàng và đang bán; trả về số dòng bị ảnh hưởng (0 = không đủ hàng).
     * Đồng thời đánh dấu ordered = true (đã có đơn -> không xoá cứng được nữa).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity, p.ordered = true " +
            "WHERE p.id = :id AND p.stock >= :quantity")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    /** Hoàn lại tồn kho khi huỷ đơn / đặt hàng thất bại. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    /** Cộng số lượng đã bán khi đơn giao thành công. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.soldCount = p.soldCount + :quantity WHERE p.id = :id")
    int increaseSold(@Param("id") Long id, @Param("quantity") int quantity);
}
