package vn.edu.smarthome.productservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.smarthome.productservice.dto.ProductDTO;
import vn.edu.smarthome.productservice.entity.Category;
import vn.edu.smarthome.productservice.entity.Product;
import vn.edu.smarthome.productservice.repository.CategoryRepository;
import vn.edu.smarthome.productservice.repository.ProductRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id).map(this::toDTO);
    }

    public ProductDTO createProduct(ProductDTO dto) {
        if (productRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Tên sản phẩm đã tồn tại");
        }

        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));
            product.setCategory(category);
        }

        return toDTO(productRepository.save(product));
    }

    public Optional<ProductDTO> updateProduct(Long id, ProductDTO dto) {
        return productRepository.findById(id).map(product -> {
            product.setName(dto.getName());
            product.setPrice(dto.getPrice());
            product.setDescription(dto.getDescription());
            product.setImageUrl(dto.getImageUrl());

            if (dto.getCategoryId() != null) {
                Category category = categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));
                product.setCategory(category);
            }

            return toDTO(productRepository.save(product));
        });
    }

    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Page<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        Page<Product> page = (keyword == null || keyword.isBlank())
                ? productRepository.findAll(pageable)
                : productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return page.map(this::toDTO);
    }

    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<ProductDTO> createProductInCategory(Long categoryId, ProductDTO dto) {
        return categoryRepository.findById(categoryId).map(category -> {
            Product product = new Product();
            product.setName(dto.getName());
            product.setPrice(dto.getPrice());
            product.setDescription(dto.getDescription());
            product.setImageUrl(dto.getImageUrl());
            product.setCategory(category);
            return toDTO(productRepository.save(product));
        });
    }

    public Optional<ProductDTO> updateProductImage(Long id, String imageUrl) {
        return productRepository.findById(id).map(product -> {
            product.setImageUrl(imageUrl);
            return toDTO(productRepository.save(product));
        });
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
        }
        return dto;
    }
}