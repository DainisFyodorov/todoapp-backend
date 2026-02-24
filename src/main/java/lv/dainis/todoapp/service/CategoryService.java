package lv.dainis.todoapp.service;

import lv.dainis.todoapp.dao.CategoryRepository;
import lv.dainis.todoapp.entity.Category;
import lv.dainis.todoapp.entity.User;
import lv.dainis.todoapp.requestmodel.CategoryRequestDTO;
import lv.dainis.todoapp.responsemodel.CategoryResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final UserService userService;

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(UserService userService, CategoryRepository categoryRepository) {
        this.userService = userService;
        this.categoryRepository = categoryRepository;
    }

    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public List<CategoryResponseDTO> getAllCategoriesByUsername(String username) {
        User user = userService.findByUsername(username);
        return categoryRepository.findAllByUser(user).stream().map(category -> {
            CategoryResponseDTO dto = new CategoryResponseDTO();
            dto.setId(category.getId());
            dto.setName(category.getName());
            return dto;
        }).collect(Collectors.toList());
    }

    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO, String username) {
        User user = userService.findByUsername(username);

        Category savedCategory = new Category();
        savedCategory.setName(categoryRequestDTO.getName());
        savedCategory.setUser(user);

        savedCategory = categoryRepository.save(savedCategory);

        CategoryResponseDTO categoryResponse = new CategoryResponseDTO();
        categoryResponse.setId(savedCategory.getId());
        categoryResponse.setName(savedCategory.getName());

        return categoryResponse;
    }

    public CategoryResponseDTO updateCategory(Long categoryId, CategoryRequestDTO categoryRequestDTO, String username) {
        User user = userService.findByUsername(username);

        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new RuntimeException("Category not found"));

        if(!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can update only your own categories");
        }

        category.setName(categoryRequestDTO.getName());
        category = categoryRepository.save(category);

        CategoryResponseDTO categoryResponseDTO = new CategoryResponseDTO();
        categoryResponseDTO.setId(category.getId());
        categoryResponseDTO.setName(category.getName());

        return categoryResponseDTO;
    }

    public void deleteCategory(Long categoryId, String username) {
        User user = userService.findByUsername(username);

        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new RuntimeException("Category not found"));

        if(!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can delete only your own categories");
        }

        categoryRepository.delete(category);
    }
}
