package vn.edu.smarthome.productservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Gọi API nội bộ của order-service để biết khách đã mua & nhận hàng thành công
 * sản phẩm hay chưa (điều kiện được đánh giá - giống SmartHome Store).
 * order-service tắt -> coi như chưa mua (không cho đánh giá), không làm hỏng trang sản phẩm.
 */
@Slf4j
@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(@Value("${services.order-service.url:http://localhost:8083}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public boolean hasPurchased(Long userId, Long productId) {
        try {
            Map<?, ?> body = restClient.get()
                    .uri("/internal/orders/purchased?userId={u}&productId={p}", userId, productId)
                    .retrieve()
                    .body(Map.class);
            return body != null && Boolean.TRUE.equals(body.get("purchased"));
        } catch (Exception e) {
            log.warn("Không kiểm tra được lịch sử mua hàng (order-service): {}", e.getMessage());
            return false;
        }
    }
}
