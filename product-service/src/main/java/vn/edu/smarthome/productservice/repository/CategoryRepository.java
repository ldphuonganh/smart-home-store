package vn.edu.smarthome.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.smarthome.productservice.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    /** Dùng khi cập nhật: trùng tên với danh mục KHÁC thì mới báo lỗi. */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
