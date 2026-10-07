package vn.edu.smarthome.productservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import vn.edu.smarthome.productservice.dto.PageResponse;
import vn.edu.smarthome.productservice.dto.ProductResponse;
import vn.edu.smarthome.productservice.service.ProductService;

import java.math.BigDecimal;

/**
 * Public Product API dành cho Partner (chỉ đọc).
 * Client gọi: GET /api/public/products  (Header X-API-KEY, scope products:read)
 * Gateway kiểm tra API Key + scope rồi rewrite thành /public/products.
 */
@RestController
@RequestMapping("/public/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;

    @GetMapping
    public PageResponse<ProductResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return productService.searchPublic(keyword, categoryId, minPrice, maxPrice, inStock, pageable);
    }

    @GetMapping("/{id:\\d+}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getVisibleById(id, false);
    }
}
