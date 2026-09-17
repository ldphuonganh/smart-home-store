package vn.edu.smarthome.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smarthome.productservice.dto.CategoryDTO;
import vn.edu.smarthome.productservice.dto.ProductDTO;
import vn.edu.smarthome.productservice.service.CategoryService;
import vn.edu.smarthome.productservice.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CategoryDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(categoryService.createCategory(dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO dto) {
        try {
            return categoryService.updateCategory(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return categoryService.deleteCategory(id)
                ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long id) {
        if (categoryService.getCategoryById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(productService.getProductsByCategory(id));
    }
}