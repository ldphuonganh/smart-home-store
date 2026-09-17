package vn.edu.smarthome.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.smarthome.productservice.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Lấy danh sách sản phẩm theo categoryId
    List<Product> findByCategoryId(Long categoryId);

    // Tìm kiếm theo tên (không phân biệt hoa thường) + phân trang
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // Kiểm tra trùng tên (dùng để validate khi tạo mới)
    boolean existsByNameIgnoreCase(String name);
}