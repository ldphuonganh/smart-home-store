package vn.edu.crs.cart_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.crs.cart_service.client.ProductClient;
import vn.edu.crs.cart_service.dto.WishlistDTO;
import vn.edu.crs.cart_service.dto.WishlistItemDTO;
import vn.edu.crs.cart_service.entity.Wishlist;
import vn.edu.crs.cart_service.entity.WishlistItem;
import vn.edu.crs.cart_service.repository.WishlistRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductClient productClient;

    @Transactional
    public WishlistDTO getWishlist(Long userId) {
        return toDTO(getOrCreate(userId));
    }

    /** Thêm sản phẩm yêu thích; đã có thì bỏ qua (không báo lỗi). */
    @Transactional
    public WishlistDTO addItem(Long userId, Long productId) {
        productClient.getRequired(productId); // sản phẩm phải tồn tại
        Wishlist wishlist = getOrCreate(userId);
        boolean exists = wishlist.getItems().stream().anyMatch(i -> i.getProductId().equals(productId));
        if (!exists) {
            WishlistItem item = new WishlistItem();
            item.setWishlist(wishlist);
            item.setProductId(productId);
            wishlist.getItems().add(item);
            wishlist.setUpdatedAt(java.time.LocalDateTime.now());
        }
        return toDTO(wishlistRepository.saveAndFlush(wishlist));
    }

    @Transactional
    public WishlistDTO removeItem(Long userId, Long itemId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Danh sach yeu thich trong"));
        WishlistItem item = wishlist.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay san pham trong danh sach yeu thich"));
        wishlist.getItems().remove(item);
        wishlist.setUpdatedAt(java.time.LocalDateTime.now());
        return toDTO(wishlistRepository.save(wishlist));
    }

    @Transactional
    public void clearWishlist(Long userId) {
        wishlistRepository.findByUserId(userId).ifPresent(w -> {
            w.getItems().clear();
            wishlistRepository.save(w);
        });
    }

    private Wishlist getOrCreate(Long userId) {
        return wishlistRepository.findByUserId(userId).orElseGet(() -> {
            Wishlist wishlist = new Wishlist();
            wishlist.setUserId(userId);
            return wishlistRepository.save(wishlist);
        });
    }

    private WishlistDTO toDTO(Wishlist wishlist) {
        return new WishlistDTO(
                wishlist.getId(),
                wishlist.getUserId(),
                wishlist.getItems().stream().map(this::toItemDTO).toList(),
                wishlist.getCreatedAt(),
                wishlist.getUpdatedAt());
    }

    private WishlistItemDTO toItemDTO(WishlistItem item) {
        Optional<ProductClient.ProductInfo> product = productClient.find(item.getProductId());
        WishlistItemDTO.WishlistItemDTOBuilder dto = WishlistItemDTO.builder()
                .id(item.getId())
                .productId(item.getProductId());
        return product.map(p -> dto.productName(p.getName()).price(p.getPrice())
                        .imageUrl(p.getImageUrl()).stock(p.getStock()).available(true).build())
                .orElseGet(() -> dto.productName("Sản phẩm #" + item.getProductId() + " (không còn bán)")
                        .available(false).build());
    }
}
