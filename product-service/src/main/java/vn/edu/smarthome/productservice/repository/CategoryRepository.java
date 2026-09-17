package vn.edu.smarthome.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.smarthome.productservice.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Kiểm tra trùng tên danh mục
    boolean existsByNameIgnoreCase(String name);
}