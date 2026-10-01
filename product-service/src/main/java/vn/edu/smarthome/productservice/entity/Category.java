package vn.edu.smarthome.productservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Danh mục sản phẩm (bảng categories).
 *
 * Chỉ giữ quan hệ 1 chiều Product -> Category:
 * - Không có List<Product> ở đây để tránh vòng lặp vô hạn (toString/hashCode/JSON).
 * - Không cascade xoá: xoá danh mục còn sản phẩm sẽ bị chặn ở CategoryService (409).
 *
 * Dùng @Getter/@Setter thay vì @Data vì @Data sinh equals/hashCode trên mọi field,
 * không phù hợp với JPA Entity.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
