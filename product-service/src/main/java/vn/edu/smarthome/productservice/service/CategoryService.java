package vn.edu.smarthome.productservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.smarthome.productservice.dto.CategoryDTO;
import vn.edu.smarthome.productservice.entity.Category;
import vn.edu.smarthome.productservice.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id).map(this::toDTO);
    }

    public CategoryDTO createCategory(CategoryDTO dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }
        Category category = new Category();
        category.setName(dto.getName());
        return toDTO(categoryRepository.save(category));
    }

    public Optional<CategoryDTO> updateCategory(Long id, CategoryDTO dto) {
        return categoryRepository.findById(id).map(category -> {
            category.setName(dto.getName());
            return toDTO(categoryRepository.save(category));
        });
    }

    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}