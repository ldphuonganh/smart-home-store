package vn.edu.crs.orderservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.crs.orderservice.client.ProductClient;
import vn.edu.crs.orderservice.dto.OrderItemRequestDTO;
import vn.edu.crs.orderservice.dto.OrderRequestDTO;
import vn.edu.crs.orderservice.entity.Order;
import vn.edu.crs.orderservice.entity.OrderItem;
import vn.edu.crs.orderservice.entity.OrderStatus;
import vn.edu.crs.orderservice.entity.PaymentStatus;
import vn.edu.crs.orderservice.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test OrderService:
 * giả lập product-service bằng Mockito
 * (không cần DB, không cần service khác).
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        lenient().when(orderRepository.saveAndFlush(any(Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        lenient().when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createOrderUsesRealPriceAndReservesStock() {
        when(productClient.getProduct(1L))
                .thenReturn(product(1L, "Đèn LED", "150000", 10));

        when(productClient.getProduct(2L))
                .thenReturn(product(2L, "Camera", "790000", 5));

        // Client gửi giá 1đ -> phải bị bỏ qua,
        // sử dụng giá thật lấy từ product-service.
        Order order = orderService.createOrder(
                7L,
                request(
                        item(1L, 2, "1"),
                        item(2L, 1, "1"),
                        item(1L, 1, "1")
                )
        );

        assertThat(order.getCustomerId()).isEqualTo(7L);

        // Hai dòng product 1 được gộp thành quantity = 3
        assertThat(order.getItems()).hasSize(2);

        // 3 * 150000 + 1 * 790000
        assertThat(order.getTotalAmount())
                .isEqualByComparingTo("1240000");

        assertThat(order.getPaymentStatus())
                .isEqualTo(PaymentStatus.UNPAID);

        verify(productClient).reserveStock(1L, 3);
        verify(productClient).reserveStock(2L, 1);
    }

    @Test
    void createOrderReleasesReservedStockWhenLaterItemIsOutOfStock() {

        when(productClient.getProduct(1L))
                .thenReturn(product(1L, "Đèn LED", "150000", 10));

        when(productClient.getProduct(2L))
                .thenReturn(product(2L, "Camera", "790000", 0));

        /*
         * Product 1 phải được reserve thành công trước.
         *
         * Đây là dòng bị thiếu trong file cũ.
         * Nếu không mock dòng này, Mockito strict sẽ ném
         * PotentialStubbingProblem trước khi chạy tới product 2.
         */
        doNothing()
                .when(productClient)
                .reserveStock(1L, 2);

        /*
         * Product 2 hết hàng -> giả lập product-service trả lỗi.
         */
        doThrow(new IllegalStateException(
                "San pham id = 2 khong du so luong ton kho"
        ))
                .when(productClient)
                .reserveStock(2L, 1);

        /*
         * Khi product 2 thất bại,
         * OrderService phải hoàn lại stock của product 1.
         */
        when(productClient.releaseStock(1L, 2))
                .thenReturn(true);

        assertThatThrownBy(() ->
                orderService.createOrder(
                        7L,
                        request(
                                item(1L, 2, null),
                                item(2L, 1, null)
                        )
                )
        )
                .isInstanceOf(IllegalStateException.class);

        // Kiểm tra compensation (bù trừ):
        // product 1 đã reserve 2 -> phải release lại 2.
        verify(productClient).releaseStock(1L, 2);

        // Order lỗi thì không được lưu xuống database.
        verify(orderRepository, never())
                .saveAndFlush(any());
    }

    @Test
    void invalidPaymentMethodIsRejected() {

        OrderRequestDTO dto = request(
                item(1L, 1, null)
        );

        dto.setPaymentMethod("MOMO");

        assertThatThrownBy(() ->
                orderService.createOrder(7L, dto)
        )
                .isInstanceOf(IllegalArgumentException.class);

        // Payment method sai -> chưa được gọi product-service.
        verifyNoInteractions(productClient);
    }

    @Test
    void cancelOrderReleasesStock() {

        Order order = existingOrder(OrderStatus.PENDING);

        when(orderRepository.findById(10L))
                .thenReturn(Optional.of(order));

        when(productClient.releaseStock(anyLong(), anyInt()))
                .thenReturn(true);

        Order cancelled = orderService.cancelOrder(
                10L,
                7L,
                false
        );

        assertThat(cancelled.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);

        // Order có 2 sản phẩm -> phải hoàn lại 2.
        verify(productClient)
                .releaseStock(1L, 2);
    }

    @Test
    void customerCannotSeeOtherCustomersOrder() {

        when(orderRepository.findById(10L))
                .thenReturn(
                        Optional.of(
                                existingOrder(OrderStatus.PENDING)
                        )
                );

        assertThatThrownBy(() ->
                orderService.getOrderById(
                        10L,
                        999L,
                        false
                )
        )
                .isInstanceOf(
                        java.util.NoSuchElementException.class
                );
    }

    @Test
    void markPaidConfirmsPendingOrder() {

        when(orderRepository.findById(10L))
                .thenReturn(
                        Optional.of(
                                existingOrder(OrderStatus.PENDING)
                        )
                );

        Order paid = orderService.markPaid(10L);

        assertThat(paid.getPaymentStatus())
                .isEqualTo(PaymentStatus.PAID);

        assertThat(paid.getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void completingCodOrderMarksItPaid() {

        Order order = existingOrder(OrderStatus.SHIPPING);

        when(orderRepository.findById(10L))
                .thenReturn(Optional.of(order));

        Order done = orderService.updateStatus(
                10L,
                "COMPLETED"
        );

        assertThat(done.getPaymentStatus())
                .isEqualTo(PaymentStatus.PAID);
    }

    // =========================================================
    // HÀM PHỤ
    // =========================================================

    private static ProductClient.ProductInfo product(
            Long id,
            String name,
            String price,
            int stock
    ) {
        return new ProductClient.ProductInfo(
                id,
                name,
                new BigDecimal(price),
                stock
        );
    }

    private static OrderItemRequestDTO item(
            Long productId,
            int quantity,
            String clientPrice
    ) {
        OrderItemRequestDTO dto =
                new OrderItemRequestDTO();

        dto.setProductId(productId);
        dto.setQuantity(quantity);

        dto.setPrice(
                clientPrice == null
                        ? null
                        : new BigDecimal(clientPrice)
        );

        return dto;
    }

    private static OrderRequestDTO request(
            OrderItemRequestDTO... items
    ) {
        OrderRequestDTO dto =
                new OrderRequestDTO();

        dto.setShippingAddress("123 Nguyen Trai");
        dto.setPhone("0912345678");
        dto.setPaymentMethod("cod");
        dto.setItems(List.of(items));

        return dto;
    }

    private static Order existingOrder(
            OrderStatus status
    ) {
        Order order = new Order();

        order.setId(10L);
        order.setCustomerId(7L);
        order.setPaymentMethod("COD");
        order.setStatus(status);
        order.setTotalAmount(
                new BigDecimal("300000")
        );

        OrderItem item = new OrderItem();

        item.setProductId(1L);
        item.setProductName("Đèn LED");
        item.setPrice(
                new BigDecimal("150000")
        );
        item.setQuantity(2);
        item.setSubtotal(
                new BigDecimal("300000")
        );

        order.addItem(item);

        return order;
    }
}