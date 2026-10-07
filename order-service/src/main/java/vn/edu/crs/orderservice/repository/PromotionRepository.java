package vn.edu.crs.orderservice.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.crs.orderservice.entity.Promotion;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<Promotion> findAllByOrderByCreatedAtDesc();

    /** Khoá dòng mã giảm giá khi đặt hàng để 2 đơn cùng lúc không vượt giới hạn lượt dùng. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Promotion p WHERE p.id = :id")
    Optional<Promotion> findByIdForUpdate(@Param("id") Long id);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE Promotion p SET p.usedCount = p.usedCount - 1 WHERE p.id = :id AND p.usedCount > 0")
    int releaseUsage(@Param("id") Long id);
}
