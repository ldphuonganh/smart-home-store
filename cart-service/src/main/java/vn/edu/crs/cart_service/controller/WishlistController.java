package vn.edu.crs.cart_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.cart_service.dto.WishlistDTO;
import vn.edu.crs.cart_service.service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<WishlistDTO> getWishlist(
            Authentication authentication
    ) {

        Long userId =
                getUserId(authentication);

        return ResponseEntity.ok(
                wishlistService.getWishlist(userId)
        );
    }

    @PostMapping("/items")
    public ResponseEntity<WishlistDTO> addItem(
            @RequestParam Long productId,
            Authentication authentication
    ) {

        Long userId =
                getUserId(authentication);

        return ResponseEntity.ok(
                wishlistService.addItem(
                        userId,
                        productId
                )
        );
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Long userId =
                getUserId(authentication);

        wishlistService.removeItem(
                userId,
                id
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearWishlist(
            Authentication authentication
    ) {

        Long userId =
                getUserId(authentication);

        wishlistService.clearWishlist(
                userId
        );

        return ResponseEntity.noContent().build();
    }

    private Long getUserId(
            Authentication authentication
    ) {

        Object credentials =
                authentication.getCredentials();

        if (credentials instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalStateException(
                "Khong lay duoc userId tu JWT"
        );
    }
}