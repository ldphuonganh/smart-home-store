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
import vn.edu.smarthome.productservice.util.SlugUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /** Tất cả danh mục; keyword != null -> tìm theo tên (trang quản trị). */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll(String keyword) {
        List<Category> categories = (keyword == null || keyword.isBlank())
                ? categoryRepository.findAllByOrderByIdAsc()
                : categoryRepository.findByNameContainingIgnoreCaseOrderByNameAsc(keyword.trim());
        return categories.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        return toResponse(categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục " + slug)));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Tên danh mục đã tồn tại.");
        }
        Category category = new Category();
        apply(category, request, name);
        category.setSlug(uniqueSlug(name, null));
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findEntity(id);
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Tên danh mục đã tồn tại.");
        }
        apply(category, request, name);
        category.setSlug(uniqueSlug(name, id));
        return toResponse(categoryRepository.save(category));
    }

    /** Không cho xoá danh mục còn sản phẩm. */
    @Transactional
    public void delete(Long id) {
        Category category = findEntity(id);
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new ConflictException("Không thể xóa danh mục này vì có " + productCount
                    + " sản phẩm đang thuộc danh mục!");
        }
        categoryRepository.delete(category);
    }

    /** Dùng chung cho ProductService. */
    @Transactional(readOnly = true)
    public Category findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục id = " + id));
    }

    private void apply(Category category, CategoryRequest request, String name) {
        category.setName(name);
        category.setDescription(request.getDescription() == null || request.getDescription().isBlank()
                ? null : request.getDescription().trim());
        if (request.getImage() != null) {
            category.setImage(request.getImage().isBlank() ? null : request.getImage().trim());
        }
    }

    private String uniqueSlug(String name, Long excludeId) {
        String base = SlugUtil.slugify(name);
        String slug = base;
        int i = 2;
        while (excludeId == null ? categoryRepository.existsBySlug(slug)
                : categoryRepository.existsBySlugAndIdNot(slug, excludeId)) {
            slug = base + "-" + i++;
        }
        return slug;
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .image(category.getImage())
                .productCount(productRepository.countByCategoryId(category.getId()))
                .build();
    }
}
