package vn.edu.crs.cart_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.crs.cart_service.client.ProductClient;
import vn.edu.crs.cart_service.dto.CartDTO;
import vn.edu.crs.cart_service.dto.CartItemDTO;
import vn.edu.crs.cart_service.entity.Cart;
import vn.edu.crs.cart_service.entity.CartItem;
import vn.edu.crs.cart_service.repository.CartRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final int MAX_QUANTITY_PER_ITEM = 99;

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    @Transactional
    public CartDTO getCart(Long userId) {
        return toDTO(getOrCreateCart(userId));
    }

    /**
     * Thêm vào giỏ: kiểm tra sản phẩm tồn tại và đủ hàng qua product-service.
     * Sản phẩm đã có trong giỏ thì cộng dồn số lượng.
     */
    @Transactional
    public CartDTO addItem(Long userId, Long productId, int quantity) {
        ProductClient.ProductInfo product = productClient.getRequired(productId);
        Cart cart = getOrCreateCart(userId);

        CartItem item = findItemByProduct(cart, productId).orElse(null);
        int newQuantity = (item == null ? 0 : item.getQuantity()) + quantity;
        checkStock(product, newQuantity);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProductId(productId);
            cart.getItems().add(item); // phải thêm vào list thì DTO trả về mới có dòng mới
        }
        item.setQuantity(newQuantity);
        touch(cart);
        // saveAndFlush để dòng mới có id ngay (IDENTITY) trước khi trả về client
        return toDTO(cartRepository.saveAndFlush(cart));
    }

    /** Cập nhật số lượng. {itemId} là id của dòng trong giỏ (không phải productId). */
    @Transactional
    public CartDTO updateItem(Long userId, Long itemId, int quantity) {
        Cart cart = findCart(userId);
        CartItem item = findItem(cart, itemId);
        checkStock(productClient.getRequired(item.getProductId()), quantity);
        item.setQuantity(quantity);
        touch(cart);
        return toDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO removeItem(Long userId, Long itemId) {
        Cart cart = findCart(userId);
        cart.getItems().remove(findItem(cart, itemId)); // orphanRemoval xoá dòng trong DB
        touch(cart);
        return toDTO(cartRepository.save(cart));
    }

    /** Xoá toàn bộ giỏ (Frontend gọi sau khi đặt hàng thành công). */
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            touch(cart);
            cartRepository.save(cart);
        });
    }

    // ==================== HÀM PHỤ ====================

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            return cartRepository.save(cart);
        });
    }

    private Cart findCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Gio hang trong"));
    }

    /** Chỉ tìm trong giỏ của chính user -> không sửa được giỏ người khác (chống IDOR). */
    private CartItem findItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay san pham trong gio hang"));
    }

    private Optional<CartItem> findItemByProduct(Cart cart, Long productId) {
        return cart.getItems().stream().filter(i -> i.getProductId().equals(productId)).findFirst();
    }

    private void checkStock(ProductClient.ProductInfo product, int quantity) {
        if (quantity > MAX_QUANTITY_PER_ITEM) {
            throw new IllegalArgumentException("So luong toi da moi san pham la " + MAX_QUANTITY_PER_ITEM);
        }
        int stock = product.getStock() == null ? 0 : product.getStock();
        if (stock <= 0) {
            throw new IllegalStateException("San pham '" + product.getName() + "' da het hang");
        }
        if (quantity > stock) {
            throw new IllegalStateException("San pham '" + product.getName() + "' chi con " + stock + " san pham");
        }
    }

    /** Đảm bảo @PreUpdate chạy (updatedAt) kể cả khi chỉ đổi dòng con. */
    private void touch(Cart cart) {
        cart.setUpdatedAt(java.time.LocalDateTime.now());
    }

    private CartDTO toDTO(Cart cart) {
        List<CartItemDTO> items = cart.getItems().stream().map(this::toItemDTO).toList();
        BigDecimal total = items.stream()
                .filter(CartItemDTO::isAvailable)
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalQuantity = items.stream().mapToInt(CartItemDTO::getQuantity).sum();
        return new CartDTO(cart.getId(), cart.getUserId(), items, totalQuantity, total,
                cart.getCreatedAt(), cart.getUpdatedAt());
    }

    private CartItemDTO toItemDTO(CartItem item) {
        Optional<ProductClient.ProductInfo> product = productClient.find(item.getProductId());
        CartItemDTO.CartItemDTOBuilder dto = CartItemDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity());
        if (product.isEmpty()) {
            return dto.productName("Sản phẩm #" + item.getProductId() + " (không còn bán)")
                    .price(BigDecimal.ZERO).subtotal(BigDecimal.ZERO).stock(0).available(false).build();
        }
        ProductClient.ProductInfo p = product.get();
        return dto.productName(p.getName())
                .price(p.getPrice())
                .imageUrl(p.getImageUrl())
                .stock(p.getStock())
                .subtotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .available(true)
                .build();
    }
}
