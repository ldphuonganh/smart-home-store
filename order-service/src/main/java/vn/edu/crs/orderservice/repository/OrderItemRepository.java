package vn.edu.crs.orderservice.repository;

import vn.edu.crs.orderservice.entity.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);
}