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

public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    /** Lấy kèm category trong 1 câu truy vấn (tránh lỗi N+1 khi hiển thị categoryName). */
    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    long countByCategoryId(Long categoryId);

    /**
     * Trừ tồn kho NGUYÊN TỬ (atomic) bằng 1 câu UPDATE có điều kiện.
     * Chỉ trừ khi còn đủ hàng; trả về số dòng bị ảnh hưởng (0 = không đủ hàng / không tồn tại).
     * Cách này an toàn khi nhiều đơn hàng đặt cùng lúc, không cần đọc rồi mới ghi.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity " +
            "WHERE p.id = :id AND p.stock >= :quantity")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    /** Hoàn lại tồn kho khi huỷ đơn / đặt hàng thất bại. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("quantity") int quantity);
}
