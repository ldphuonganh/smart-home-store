package vn.edu.smarthome.paymentservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import vn.edu.smarthome.paymentservice.exception.ConflictException;
import vn.edu.smarthome.paymentservice.exception.ResourceNotFoundException;

import java.math.BigDecimal;

/**
 * Gọi API NỘI BỘ của order-service (không qua Gateway):
 *   GET /internal/orders/{id}       -> tổng tiền, chủ đơn, trạng thái
 *   PUT /internal/orders/{id}/paid  -> báo đã thanh toán (đơn chuyển PAID + CONFIRMED)
 */
@Component
public class OrderClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public OrderClient(@Value("${order-service.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
        this.baseUrl = baseUrl;
    }

    public OrderInfo getOrder(Long orderId) {
        try {
            OrderInfo order = restTemplate.getForObject(baseUrl + "/internal/orders/" + orderId, OrderInfo.class);
            if (order == null) {
                throw new ResourceNotFoundException("Không tìm thấy đơn hàng id = " + orderId);
            }
            return order;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Không tìm thấy đơn hàng id = " + orderId);
        } catch (RestClientException e) {
            throw new ConflictException("Không kết nối được order-service, vui lòng thử lại sau");
        }
    }

    public void markPaid(Long orderId) {
        try {
            restTemplate.put(baseUrl + "/internal/orders/" + orderId + "/paid", null);
        } catch (HttpClientErrorException.Conflict e) {
            throw new ConflictException("Đơn hàng không còn thanh toán được (có thể đã bị huỷ)");
        } catch (RestClientException e) {
            throw new ConflictException("Không cập nhật được đơn hàng, vui lòng thử lại sau");
        }
    }

    /** Khớp InternalOrderController.OrderPaymentInfo của order-service. */
    public record OrderInfo(Long id, Long customerId, BigDecimal totalAmount,
                            String paymentMethod, String status, String paymentStatus) {
    }
}
