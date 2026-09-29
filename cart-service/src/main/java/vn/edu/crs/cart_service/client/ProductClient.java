package vn.edu.crs.cart_service.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Gọi API nội bộ của product-service: GET /internal/products/{id}.
 * cart-service KHÔNG truy cập product_db (Database Isolation).
 */
@Component
public class ProductClient {

    private final RestTemplate restTemplate;

    @Value("${product-service.base-url}")
    private String productServiceBaseUrl;

    public ProductClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** Dùng khi THÊM vào giỏ: sản phẩm phải tồn tại, product-service phải trả lời được. */
    public ProductInfo getRequired(Long productId) {
        try {
            ProductInfo product = restTemplate.getForObject(url(productId), ProductInfo.class);
            if (product == null) {
                throw new NoSuchElementException("Khong tim thay san pham id = " + productId);
            }
            return product;
        } catch (HttpClientErrorException.NotFound e) {
            throw new NoSuchElementException("Khong tim thay san pham id = " + productId);
        } catch (RestClientException e) {
            throw new IllegalStateException("Khong the ket noi toi product-service, vui long thu lai sau");
        }
    }

    /**
     * Dùng khi XEM giỏ: lỗi thì trả Optional.empty() để giỏ vẫn hiển thị được
     * (sản phẩm đã bị xoá hoặc product-service đang tắt).
     */
    public Optional<ProductInfo> find(Long productId) {
        try {
            return Optional.ofNullable(restTemplate.getForObject(url(productId), ProductInfo.class));
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }

    private String url(Long productId) {
        return productServiceBaseUrl + "/internal/products/" + productId;
    }

    @Data
    @NoArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String name;
        private BigDecimal price;
        private Integer stock;
        private String imageUrl;
    }
}
