package vn.edu.crs.cart_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.crs.cart_service.entity.WishlistItem;

import java.util.Optional;

public interface WishlistItemRepository
        extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByIdAndWishlistId(
            Long id,
            Long wishlistId
    );

    Optional<WishlistItem> findByWishlistIdAndProductId(
            Long wishlistId,
            Long productId
    );
}