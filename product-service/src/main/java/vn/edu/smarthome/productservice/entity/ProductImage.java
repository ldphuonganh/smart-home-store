package vn.edu.smarthome.productservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Ảnh sản phẩm (bảng product_images) - 1 sản phẩm có nhiều ảnh, 1 ảnh đại diện. */
@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public ProductImage(String imageUrl, boolean primary, int sortOrder) {
        this.imageUrl = imageUrl;
        this.primary = primary;
        this.sortOrder = sortOrder;
    }
}
