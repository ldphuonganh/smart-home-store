package vn.edu.smarthome.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.smarthome.paymentservice.entity.Payment;
import vn.edu.smarthome.paymentservice.entity.PaymentStatus;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserIdOrderByIdDesc(Long userId);

    List<Payment> findAllByOrderByIdDesc();

    List<Payment> findByOrderIdOrderByIdDesc(Long orderId);

    Optional<Payment> findFirstByOrderIdAndStatusIn(Long orderId, List<PaymentStatus> statuses);
}
