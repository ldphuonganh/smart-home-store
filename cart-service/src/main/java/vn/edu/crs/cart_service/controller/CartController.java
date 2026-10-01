package vn.edu.crs.cart_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.cart_service.dto.CartDTO;
import vn.edu.crs.cart_service.dto.CartItemRequest;
import vn.edu.crs.cart_service.dto.UpdateQuantityRequest;
import vn.edu.crs.cart_service.service.CartService;

/**
 * Client gọi qua Gateway: /api/cart/**  ->  cart-service /cart/**
 * userId luôn lấy từ JWT, không nhận từ client.
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartDTO getCart(Authentication authentication) {
        return cartService.getCart(CurrentUser.id(authentication));
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartDTO addItem(@Valid @RequestBody CartItemRequest request, Authentication authentication) {
        return cartService.addItem(CurrentUser.id(authentication), request.getProductId(), request.getQuantity());
    }

    @PutMapping("/items/{id}")
    public CartDTO updateItem(@PathVariable Long id, @Valid @RequestBody UpdateQuantityRequest request,
                              Authentication authentication) {
        return cartService.updateItem(CurrentUser.id(authentication), id, request.getQuantity());
    }

    @DeleteMapping("/items/{id}")
    public CartDTO removeItem(@PathVariable Long id, Authentication authentication) {
        return cartService.removeItem(CurrentUser.id(authentication), id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(Authentication authentication) {
        cartService.clearCart(CurrentUser.id(authentication));
    }
}
