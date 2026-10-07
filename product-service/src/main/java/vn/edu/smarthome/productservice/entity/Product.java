package vn.edu.smarthome.productservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sản phẩm (bảng products) - đủ các cột của SmartHome Store (Laravel):
 * slug, brand, material, weight, power, origin, is_active + nhiều ảnh (product_images).
 *
 * - price dùng BigDecimal, lưu số nguyên VNĐ (scale = 0).
 * - stock chỉ được trừ/hoàn qua API nội bộ reserve-stock / release-stock (order-service gọi).
 * - imageUrl luôn là ảnh đại diện (đồng bộ với ảnh isPrimary trong images) để các trang
 *   danh sách / giỏ hàng / đơn hàng chỉ cần đọc 1 cột.
 * - soldCount: số lượng đã bán (chỉ tính đơn giao thành công - order-service báo sang).
 * - ordered: đã từng nằm trong đơn hàng -> không xoá cứng, chỉ chuyển sang ngừng bán.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String material;

    @Column(length = 50)
    private String weight;

    @Column(length = 50)
    private String power;

    @Column(length = 100)
    private String origin;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "sold_count", nullable = false)
    private Integer soldCount = 0;

    @Column(nullable = false)
    private boolean ordered = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProductImage> images = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (stock == null) {
            stock = 0;
        }
        if (soldCount == null) {
            soldCount = 0;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addImage(ProductImage image) {
        image.setProduct(this);
        images.add(image);
    }

    /** Giữ imageUrl khớp ảnh đại diện (giống syncPrimaryImage bên Laravel). */
    public void syncPrimaryImage() {
        ProductImage primary = images.stream().filter(ProductImage::isPrimary).findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
        images.forEach(img -> img.setPrimary(img == primary));
        imageUrl = primary != null ? primary.getImageUrl() : null;
    }
}
