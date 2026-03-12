package lv.dainis.todoapp.controller;

import jakarta.validation.Valid;
import lv.dainis.todoapp.entity.UserPrincipal;
import lv.dainis.todoapp.requestmodel.CategoryRequestDTO;
import lv.dainis.todoapp.responsemodel.CategoryResponseDTO;
import lv.dainis.todoapp.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getUserCategories(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<CategoryResponseDTO> categoryList = categoryService.getAllCategoriesByUserId(principal.getId());

        return ResponseEntity.ok(categoryList);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryRequestDTO categoryRequest,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        CategoryResponseDTO createdCategoryResponse = categoryService.createCategory(
                categoryRequest, principal.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategoryResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO categoryRequest,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        CategoryResponseDTO updatedCategoryResponse = categoryService.updateCategory(
                id, categoryRequest, principal.getId());

        return ResponseEntity.ok(updatedCategoryResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        categoryService.deleteCategory(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
