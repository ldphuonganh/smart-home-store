package vn.edu.smarthome.productservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smarthome.productservice.dto.ProductDTO;
import vn.edu.smarthome.productservice.service.ProductService;

@RestController
@RequestMapping("/public/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;

    /**
     * GET /public/products
     * Dành cho Partner - xác thực qua X-API-KEY tại Gateway
     */
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getPublicProducts(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(keyword, pageable));
    }

    /**
     * GET /public/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getPublicProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}