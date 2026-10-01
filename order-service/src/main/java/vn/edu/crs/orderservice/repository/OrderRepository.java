package vn.edu.crs.orderservice.repository;

import vn.edu.crs.orderservice.entity.Order;
import vn.edu.crs.orderservice.entity.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByCustomerIdAndStatus(
            Long customerId,
            OrderStatus status
    );
}