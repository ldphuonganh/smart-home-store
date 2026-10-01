package vn.edu.smarthome.productservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smarthome.productservice.dto.CategoryRequest;
import vn.edu.smarthome.productservice.dto.CategoryResponse;
import vn.edu.smarthome.productservice.entity.Category;
import vn.edu.smarthome.productservice.exception.ConflictException;
import vn.edu.smarthome.productservice.exception.ResourceNotFoundException;
import vn.edu.smarthome.productservice.repository.CategoryRepository;
import vn.edu.smarthome.productservice.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Tên danh mục đã tồn tại");
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(request.getDescription());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findEntity(id);
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Tên danh mục đã tồn tại");
        }
        category.setName(name);
        category.setDescription(request.getDescription());
        return toResponse(categoryRepository.save(category));
    }

    /**
     * Không cho xoá danh mục còn sản phẩm (trước đây cascade xoá luôn sản phẩm - rất nguy hiểm).
     * Admin phải chuyển sản phẩm sang danh mục khác hoặc xoá sản phẩm trước.
     */
    @Transactional
    public void delete(Long id) {
        Category category = findEntity(id);
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new ConflictException("Danh mục đang có " + productCount
                    + " sản phẩm, không thể xoá");
        }
        categoryRepository.delete(category);
    }

    /** Dùng chung cho ProductService. */
    @Transactional(readOnly = true)
    public Category findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục id = " + id));
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(productRepository.countByCategoryId(category.getId()))
                .build();
    }
}
