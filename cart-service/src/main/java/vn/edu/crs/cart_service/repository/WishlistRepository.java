package vn.edu.crs.cart_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.crs.cart_service.entity.Wishlist;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByUserId(Long userId);
}