package vn.edu.smarthome.productservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Danh mục sản phẩm (bảng categories) - giống bảng categories của SmartHome Store (Laravel):
 * name, slug, description, image.
 *
 * Chỉ giữ quan hệ 1 chiều Product -> Category, không cascade xoá
 * (xoá danh mục còn sản phẩm bị chặn ở CategoryService - 409).
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

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(length = 1000)
    private String description;

    /** Ảnh đại diện danh mục, vd "/images/categories/kitchen.jpg". */
    @Column(length = 500)
    private String image;

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
