package vn.edu.crs.orderservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Quan hệ nhiều OrderItem thuộc về một Order
     *
     * @JsonIgnore:
     * Không cho Jackson trả ngược Order bên trong OrderItem
     * để tránh JSON bị lặp:
     *
     * Order
     *  -> items
     *      -> order
     *          -> items
     *              -> order
     *                  -> ...
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * ID sản phẩm thuộc Product Service
     *
     * Không tạo quan hệ JPA với Product vì Product nằm
     * ở database của Product Service.
     */
    @Column(nullable = false)
    private Long productId;

    /**
     * Lưu tên sản phẩm tại thời điểm đặt hàng.
     * Đây là snapshot (ảnh chụp dữ liệu) của sản phẩm.
     */
    @Column(nullable = false)
    private String productName;

    /**
     * Giá sản phẩm tại thời điểm đặt hàng.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    /**
     * Số lượng sản phẩm.
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Thành tiền = price * quantity.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;
}