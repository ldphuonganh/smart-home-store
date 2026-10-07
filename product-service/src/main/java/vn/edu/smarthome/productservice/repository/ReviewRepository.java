package vn.edu.smarthome.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.smarthome.productservice.entity.Review;

import java.util.Collection;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    @Override
    @EntityGraph(attributePaths = "product")
    Page<Review> findAll(Specification<Review> spec, Pageable pageable);

    List<Review> findByProductIdAndApprovedTrueOrderByCreatedAtDesc(Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    long countByRating(int rating);

    @Query("SELECT r.product.id FROM Review r WHERE r.userId = :userId")
    List<Long> findProductIdsByUserId(@Param("userId") Long userId);

    void deleteByProductId(Long productId);

    /** [productId, avg(rating), count] của các đánh giá đang hiển thị. */
    @Query("SELECT r.product.id, AVG(r.rating), COUNT(r) FROM Review r "
            + "WHERE r.approved = true AND r.product.id IN :ids GROUP BY r.product.id")
    List<Object[]> ratingStats(@Param("ids") Collection<Long> ids);
}
