package vn.edu.crs.cart_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.cart_service.dto.CartDTO;
import vn.edu.crs.cart_service.service.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getCart(
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                cartService.getCart(userId)
        );
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItem(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                cartService.addItem(
                        userId,
                        productId,
                        quantity
                )
        );
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartDTO> updateItem(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                cartService.updateItem(
                        userId,
                        id,
                        quantity
                )
        );
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        cartService.removeItem(userId, id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            Authentication authentication) {

        Long userId = getUserId(authentication);

        cartService.clearCart(userId);

        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Authentication authentication) {

        Object credentials = authentication.getCredentials();

        if (credentials instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalStateException(
                "Khong lay duoc userId tu JWT"
        );
    }
}