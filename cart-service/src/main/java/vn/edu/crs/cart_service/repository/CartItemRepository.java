package vn.edu.crs.cart_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.crs.cart_service.entity.CartItem;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}