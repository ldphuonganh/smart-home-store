package vn.edu.crs.orderservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class ProductClient {

    private final RestTemplate restTemplate;

    @Value("${product-service.base-url}")
    private String productServiceBaseUrl;

    @Value("${product-service.validation-enabled:false}")
    private boolean validationEnabled;

    public ProductClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProductInfo getProduct(Long productId) {

        if (!validationEnabled) {
            return null;
        }

        String url =
                productServiceBaseUrl
                        + "/internal/products/"
                        + productId;

        try {

            ProductInfo product =
                    restTemplate.getForObject(
                            url,
                            ProductInfo.class
                    );

            if (product == null) {
                throw new IllegalStateException(
                        "Khong tim thay san pham id = " + productId
                );
            }

            return product;

        } catch (IllegalStateException e) {
            throw e;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Khong the ket noi toi product-service"
            );
        }
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