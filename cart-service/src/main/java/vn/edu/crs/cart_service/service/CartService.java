package vn.edu.crs.cart_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.crs.cart_service.dto.CartDTO;
import vn.edu.crs.cart_service.dto.CartItemDTO;
import vn.edu.crs.cart_service.entity.Cart;
import vn.edu.crs.cart_service.entity.CartItem;
import vn.edu.crs.cart_service.repository.CartItemRepository;
import vn.edu.crs.cart_service.repository.CartRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // Lấy giỏ hàng của user
    public CartDTO getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));

        return toDTO(cart);
    }

    // Thêm sản phẩm vào giỏ hàng
    @Transactional
    public CartDTO addItem(Long userId, Long productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("So luong phai lon hon 0");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            item = new CartItem();
            item.setCart(cart);
            item.setProductId(productId);
            item.setQuantity(quantity);
        }

        cartItemRepository.save(item);

        return toDTO(cart);
    }

    // Cập nhật số lượng sản phẩm
    @Transactional
    public CartDTO updateItem(Long userId, Long itemId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("So luong phai lon hon 0");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Khong tim thay gio hang"));

        CartItem item = cartItemRepository
                .findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Khong tim thay san pham trong gio hang"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return toDTO(cart);
    }

    // Xóa một sản phẩm khỏi giỏ hàng
    @Transactional
    public void removeItem(Long userId, Long itemId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Khong tim thay gio hang"));

        CartItem item = cartItemRepository
                .findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Khong tim thay san pham trong gio hang"));

        cartItemRepository.delete(item);
    }

    // Xóa toàn bộ giỏ hàng
    @Transactional
    public void clearCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Khong tim thay gio hang"));

        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // Tạo giỏ hàng mới
    private Cart createCart(Long userId) {

        Cart cart = new Cart();
        cart.setUserId(userId);

        return cartRepository.save(cart);
    }

    // Chuyển Cart Entity -> CartDTO
    private CartDTO toDTO(Cart cart) {

        List<CartItemDTO> items = cart.getItems()
                .stream()
                .map(this::toItemDTO)
                .toList();

        return new CartDTO(
                cart.getId(),
                cart.getUserId(),
                items,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    // Chuyển CartItem Entity -> CartItemDTO
    private CartItemDTO toItemDTO(CartItem item) {

        return new CartItemDTO(
                item.getId(),
                item.getProductId(),
                item.getQuantity()
        );
    }
}