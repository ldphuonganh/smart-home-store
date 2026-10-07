package vn.edu.smarthome.productservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Đánh giá sản phẩm (bảng reviews).
 * - Mỗi khách chỉ đánh giá 1 lần / sản phẩm (unique user_id + product_id).
 * - Chỉ khách đã mua và nhận hàng thành công mới được đánh giá (hỏi order-service).
 * - userName/userEmail lưu snapshot từ JWT vì user nằm ở auth-service (DB khác).
 * - approved: admin ẩn/hiện đánh giá; mặc định hiển thị.
 */
@Entity
@Table(name = "reviews", uniqueConstraints =
        @UniqueConstraint(name = "uk_review_user_product", columnNames = {"user_id", "product_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", length = 150)
    private String userName;

    @Column(name = "user_email", length = 150)
    private String userEmail;

    @Column(nullable = false)
    private int rating;

    @Column(length = 1000)
    private String comment;

    @Column(name = "is_approved", nullable = false)
    private boolean approved = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
