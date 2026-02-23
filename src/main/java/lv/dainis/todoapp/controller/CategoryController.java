package lv.dainis.todoapp.controller;

import jakarta.validation.Valid;
import lv.dainis.todoapp.entity.Category;
import lv.dainis.todoapp.requestmodel.CategoryRequestDTO;
import lv.dainis.todoapp.responsemodel.CategoryResponseDTO;
import lv.dainis.todoapp.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getUserCategories(Principal principal) {
        List<CategoryResponseDTO> categoryList = categoryService.getAllCategoriesByUsername(principal.getName());

        return ResponseEntity.ok(categoryList);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryRequestDTO categoryRequest,
            Principal principal) {

        CategoryResponseDTO createdCategoryResponse = categoryService.createCategory(
                categoryRequest, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategoryResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO categoryRequest,
            Principal principal) {

        CategoryResponseDTO updatedCategoryResponse = categoryService.updateCategory(
                id, categoryRequest, principal.getName());

        return ResponseEntity.ok(updatedCategoryResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, Principal principal) {
        categoryService.deleteCategory(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
