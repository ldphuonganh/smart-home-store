package vn.edu.crs.cart_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.cart_service.dto.WishlistDTO;
import vn.edu.crs.cart_service.dto.WishlistItemRequest;
import vn.edu.crs.cart_service.service.WishlistService;

/** Qua Gateway: /api/wishlist/** -> cart-service /wishlist/** */
@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public WishlistDTO getWishlist(Authentication authentication) {
        return wishlistService.getWishlist(CurrentUser.id(authentication));
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistDTO addItem(@Valid @RequestBody WishlistItemRequest request, Authentication authentication) {
        return wishlistService.addItem(CurrentUser.id(authentication), request.getProductId());
    }

    @DeleteMapping("/items/{id}")
    public WishlistDTO removeItem(@PathVariable Long id, Authentication authentication) {
        return wishlistService.removeItem(CurrentUser.id(authentication), id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearWishlist(Authentication authentication) {
        wishlistService.clearWishlist(CurrentUser.id(authentication));
    }
}
