package vn.edu.crs.orderservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Gọi API NỘI BỘ của product-service (không qua Gateway):
 *   GET   /internal/products/{id}
 *   PATCH /internal/products/{id}/reserve-stock   { "quantity": n }
 *   PATCH /internal/products/{id}/release-stock   { "quantity": n }
 * order-service KHÔNG truy cập product_db.
 */
@Component
public class ProductClient {

    private final RestTemplate restTemplate;

    @Value("${product-service.base-url}")
    private String productServiceBaseUrl;

    @Value("${product-service.validation-enabled:true}")
    private boolean validationEnabled;

    public ProductClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isEnabled() {
        return validationEnabled;
    }

    /** Lấy tên, giá, tồn kho THẬT từ product-service. */
    public ProductInfo getProduct(Long productId) {
        if (!validationEnabled) {
            return null;
        }
        try {
            ProductInfo product = restTemplate.getForObject(
                    productServiceBaseUrl + "/internal/products/" + productId, ProductInfo.class);
            if (product == null) {
                throw new NoSuchElementException("Khong tim thay san pham id = " + productId);
            }
            return product;
        } catch (HttpClientErrorException.NotFound e) {
            throw new NoSuchElementException("Khong tim thay san pham id = " + productId);
        } catch (ResourceAccessException e) {
            throw new IllegalStateException("Khong the ket noi toi product-service, vui long thu lai sau");
        }
    }

    /** Trừ tồn kho. Không đủ hàng -> IllegalStateException (409). */
    public void reserveStock(Long productId, int quantity) {
        if (!validationEnabled) {
            return;
        }
        try {
            patch(productId, "reserve-stock", quantity);
        } catch (HttpClientErrorException.Conflict e) {
            throw new IllegalStateException("San pham id = " + productId + " khong du so luong ton kho");
        } catch (HttpClientErrorException.NotFound e) {
            throw new NoSuchElementException("Khong tim thay san pham id = " + productId);
        } catch (ResourceAccessException e) {
            throw new IllegalStateException("Khong the ket noi toi product-service, vui long thu lai sau");
        }
    }

    /**
     * Hoàn tồn kho (bù trừ khi đặt hàng lỗi giữa chừng hoặc khi huỷ đơn).
     * Trả về false nếu không hoàn được để nơi gọi ghi log, KHÔNG ném lỗi
     * (tránh che mất lỗi gốc).
     */
    public boolean releaseStock(Long productId, int quantity) {
        if (!validationEnabled) {
            return true;
        }
        try {
            patch(productId, "release-stock", quantity);
            return true;
        } catch (RestClientException e) {
            return false;
        }
    }

    private void patch(Long productId, String action, int quantity) {
        restTemplate.exchange(
                productServiceBaseUrl + "/internal/products/" + productId + "/" + action,
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("quantity", quantity)),
                Void.class);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String name;
        private BigDecimal price;
        private Integer stock;
    }
}
