package vn.edu.smarthome.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.smarthome.productservice.entity.ProductImage;

import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    Optional<ProductImage> findByIdAndProductId(Long id, Long productId);
}
