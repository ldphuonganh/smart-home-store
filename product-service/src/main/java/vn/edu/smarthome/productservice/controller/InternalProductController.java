package vn.edu.smarthome.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.edu.smarthome.productservice.dto.ProductResponse;
import vn.edu.smarthome.productservice.dto.StockRequest;
import vn.edu.smarthome.productservice.service.ProductService;

/**
 * API NỘI BỘ - chỉ dành cho service khác (order-service, cart-service) gọi trực tiếp
 * vào cổng 8082. Gateway KHÔNG định tuyến /internal/** ra ngoài.
 * (Cùng mô hình reserve-seat/release-seat của Buổi 3.)
 */
@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    /** order-service/cart-service lấy tên, giá, tồn kho hiện tại. */
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    /** Trừ tồn kho khi tạo đơn. Không đủ hàng -> 409. */
    @PatchMapping("/{id}/reserve-stock")
    public ProductResponse reserveStock(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return productService.reserveStock(id, request.getQuantity());
    }

    /** Hoàn tồn kho khi huỷ đơn hoặc tạo đơn thất bại. */
    @PatchMapping("/{id}/release-stock")
    public ProductResponse releaseStock(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return productService.releaseStock(id, request.getQuantity());
    }
}
