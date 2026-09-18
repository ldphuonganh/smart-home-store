package vn.edu.crs.cart_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.crs.cart_service.dto.WishlistDTO;
import vn.edu.crs.cart_service.dto.WishlistItemDTO;
import vn.edu.crs.cart_service.entity.Wishlist;
import vn.edu.crs.cart_service.entity.WishlistItem;
import vn.edu.crs.cart_service.repository.WishlistItemRepository;
import vn.edu.crs.cart_service.repository.WishlistRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;

    @Transactional
    public WishlistDTO getWishlist(Long userId) {

        Wishlist wishlist =
                wishlistRepository
                        .findByUserId(userId)
                        .orElseGet(() ->
                                createWishlist(userId)
                        );

        return toDTO(wishlist);
    }

    @Transactional
    public WishlistDTO addItem(
            Long userId,
            Long productId
    ) {

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException(
                    "Product ID phai lon hon 0"
            );
        }

        Wishlist wishlist =
                wishlistRepository
                        .findByUserId(userId)
                        .orElseGet(() ->
                                createWishlist(userId)
                        );

        WishlistItem item =
                wishlistItemRepository
                        .findByWishlistIdAndProductId(
                                wishlist.getId(),
                                productId
                        )
                        .orElse(null);

        if (item == null) {

            item = new WishlistItem();

            item.setWishlist(wishlist);
            item.setProductId(productId);

            wishlist.getItems().add(item);

            wishlistItemRepository.save(item);
        }

        return toDTO(wishlist);
    }

    @Transactional
    public void removeItem(
            Long userId,
            Long itemId
    ) {

        Wishlist wishlist =
                wishlistRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Khong tim thay wishlist"
                                )
                        );

        WishlistItem item =
                wishlistItemRepository
                        .findByIdAndWishlistId(
                                itemId,
                                wishlist.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Khong tim thay san pham trong wishlist"
                                )
                        );

        wishlist.getItems().remove(item);

        wishlistItemRepository.delete(item);
    }

    @Transactional
    public void clearWishlist(Long userId) {

        Wishlist wishlist =
                wishlistRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Khong tim thay wishlist"
                                )
                        );

        wishlist.getItems().clear();

        wishlistRepository.save(wishlist);
    }

    private Wishlist createWishlist(
            Long userId
    ) {

        Wishlist wishlist =
                new Wishlist();

        wishlist.setUserId(userId);

        return wishlistRepository.save(wishlist);
    }

    private WishlistDTO toDTO(
            Wishlist wishlist
    ) {

        List<WishlistItemDTO> items =
                wishlist.getItems()
                        .stream()
                        .map(this::toItemDTO)
                        .toList();

        return new WishlistDTO(
                wishlist.getId(),
                wishlist.getUserId(),
                items,
                wishlist.getCreatedAt(),
                wishlist.getUpdatedAt()
        );
    }

    private WishlistItemDTO toItemDTO(
            WishlistItem item
    ) {

        return new WishlistItemDTO(
                item.getId(),
                item.getProductId()
        );
    }
}