package vn.edu.smarthome.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.edu.smarthome.productservice.dto.ProductResponse;
import vn.edu.smarthome.productservice.dto.SoldRequest;
import vn.edu.smarthome.productservice.dto.StockRequest;
import vn.edu.smarthome.productservice.service.ProductService;

import java.util.List;

/**
 * API NỘI BỘ - chỉ dành cho service khác (order-service, cart-service) gọi trực tiếp
 * vào cổng 8082. Gateway KHÔNG định tuyến /internal/** ra ngoài.
 */
@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    /** order-service/cart-service lấy tên, giá, tồn kho, trạng thái bán hiện tại. */
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    /** GET /internal/products?ids=1,2,3 - lấy nhiều sản phẩm 1 lần. */
    @GetMapping
    public List<ProductResponse> getByIds(@RequestParam List<Long> ids) {
        return productService.getByIds(ids);
    }

    /** Trừ tồn kho khi tạo đơn. Không đủ hàng / ngừng bán -> 409. */
    @PatchMapping("/{id}/reserve-stock")
    public ProductResponse reserveStock(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return productService.reserveStock(id, request.getQuantity());
    }

    /** Hoàn tồn kho khi huỷ đơn hoặc tạo đơn thất bại. */
    @PatchMapping("/{id}/release-stock")
    public ProductResponse releaseStock(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return productService.releaseStock(id, request.getQuantity());
    }

    /** Cộng số lượng đã bán khi đơn giao thành công. */
    @PatchMapping("/{id}/sold")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sold(@PathVariable Long id, @Valid @RequestBody SoldRequest request) {
        productService.increaseSold(id, request.getQuantity());
    }
}
