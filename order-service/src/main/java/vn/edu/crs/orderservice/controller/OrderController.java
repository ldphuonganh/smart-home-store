package vn.edu.crs.orderservice.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import vn.edu.crs.orderservice.dto.OrderRequestDTO;
import vn.edu.crs.orderservice.dto.UpdateOrderStatusDTO;
import vn.edu.crs.orderservice.entity.Order;
import vn.edu.crs.orderservice.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Customer checkout / tạo đơn hàng
     *
     * POST /orders
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order createOrder(
            Authentication authentication,
            @Valid @RequestBody OrderRequestDTO dto
    ) {

        Long customerId =
                getUserId(authentication);

        return orderService.createOrder(
                customerId,
                dto
        );
    }

    /**
     * Customer xem đơn hàng của mình
     *
     * GET /orders/my
     */
    @GetMapping("/my")
    public List<Order> getMyOrders(
            Authentication authentication
    ) {

        Long customerId =
                getUserId(authentication);

        return orderService.getMyOrders(
                customerId
        );
    }

    /**
     * Admin xem tất cả đơn hàng
     *
     * GET /orders
     */
    @GetMapping
    public List<Order> getAllOrders(
            Authentication authentication
    ) {

        requireAdmin(authentication);

        return orderService.getAllOrders();
    }

    /**
     * Xem chi tiết đơn hàng
     *
     * GET /orders/{id}
     */
    @GetMapping("/{id}")
    public Order getOrderById(
            Authentication authentication,
            @PathVariable Long id
    ) {

        Long customerId =
                getUserId(authentication);

        boolean isAdmin =
                isAdmin(authentication);

        return orderService.getOrderById(
                id,
                customerId,
                isAdmin
        );
    }

    /**
     * Customer hủy đơn
     *
     * PUT /orders/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public Order cancelOrder(
            Authentication authentication,
            @PathVariable Long id
    ) {

        Long customerId =
                getUserId(authentication);

        boolean isAdmin =
                isAdmin(authentication);

        return orderService.cancelOrder(
                id,
                customerId,
                isAdmin
        );
    }

    /**
     * Admin cập nhật trạng thái
     *
     * PUT /orders/{id}/status
     */
    @PutMapping("/{id}/status")
    public Order updateStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDTO dto
    ) {

        requireAdmin(authentication);

        return orderService.updateStatus(
                id,
                dto.getStatus()
        );
    }

    /**
     * Lấy userId từ JWT
     */
    private Long getUserId(
            Authentication authentication
    ) {

        if (authentication == null) {

            throw new IllegalStateException(
                    "Chua dang nhap"
            );
        }

        Object credentials =
                authentication.getCredentials();

        if (!(credentials instanceof Long)) {

            throw new IllegalStateException(
                    "Khong xac dinh duoc userId"
            );
        }

        return (Long) credentials;
    }

    /**
     * Kiểm tra Admin
     */
    private void requireAdmin(
            Authentication authentication
    ) {

        if (!isAdmin(authentication)) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "Chi ADMIN moi co quyen thuc hien thao tac nay"
            );
        }
    }

    private boolean isAdmin(
            Authentication authentication
    ) {

        return authentication != null
                && authentication.getAuthorities()
                .stream()
                .anyMatch(
                        authority ->
                                authority
                                        .getAuthority()
                                        .equals("ROLE_ADMIN")
                );
    }
}