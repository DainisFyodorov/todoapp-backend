package lv.dainis.todoapp.service;

import lv.dainis.todoapp.dao.CategoryRepository;
import lv.dainis.todoapp.entity.Category;
import lv.dainis.todoapp.entity.User;
import lv.dainis.todoapp.requestmodel.CategoryRequestDTO;
import lv.dainis.todoapp.responsemodel.CategoryResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CategoryService categoryService;

    //region getAllCategoriesByUsername
    @Test
    @DisplayName("Get all user's categories (success)")
    void getUserCategoriesTest() {
        String username = "Dainis";

        User user = new User();
        user.setUsername(username);

        Category categoryOne = new Category();
        Category categoryTwo = new Category();

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryRepository.findAllByUser(user)).thenReturn(List.of(categoryOne, categoryTwo));

        List<CategoryResponseDTO> categories = categoryService.getAllCategoriesByUsername(username);
        assertEquals(2, categories.size());
    }

    @Test
    @DisplayName("Get all user's categories (user not found)")
    void getUserCategoriesUserNotFoundTest() {
        String username = "Dainis";

        when(userService.findByUsername(username)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> categoryService.getAllCategoriesByUsername(username));
    }
    //endregion

    //region createCategory
    @Test
    @DisplayName("Create category (success)")
    void createCategorySuccessTest() {
        String username = "Dainis";

        User user = new User();
        user.setUsername(username);

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();
        categoryRequestDTO.setName("Test category");

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryResponseDTO categoryResponseDTO = categoryService.createCategory(categoryRequestDTO, username);

        assertNotNull(categoryResponseDTO);
        assertNotEquals(0, categoryResponseDTO.getId());
        assertEquals(categoryRequestDTO.getName(), categoryResponseDTO.getName());

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Create category (user not found)")
    void createCategoryUserNotFoundTest() {
        String username = "Dainis";

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();

        when(userService.findByUsername(username)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> categoryService.createCategory(categoryRequestDTO, username));

        verify(categoryRepository, never()).save(any());
    }
    //endregion

    //region updateCategory

    @Test
    @DisplayName("Update category (success)")
    void updateCategorySuccessTest() {
        String username = "Dainis";
        Long categoryId = 1L;

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Before title");
        existingCategory.setUser(user);

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();
        categoryRequestDTO.setName("After title");

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryResponseDTO responseDTO = categoryService.updateCategory(categoryId, categoryRequestDTO, username);

        assertNotNull(responseDTO);
        assertEquals(categoryRequestDTO.getName(), existingCategory.getName());
        assertEquals(categoryRequestDTO.getName(), responseDTO.getName());
        assertEquals(categoryId, responseDTO.getId());

        verify(categoryRepository, times(1)).save(existingCategory);
    }

    @Test
    @DisplayName("Update category (user not found)")
    void updateCategoryUserNotFoundTest() {

        String username = "Dainis";

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();

        when(userService.findByUsername(username)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () ->
                categoryService.updateCategory(1L, categoryRequestDTO, username));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update category (category not found)")
    void updateCategoryWhenCategoryNotFoundTest() {
        String username = "Dainis";
        Long categoryId = 1L;

        User user = new User();
        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(RuntimeException.class, () ->
                categoryService.updateCategory(categoryId, categoryRequestDTO, username));

        assertEquals("Category not found", exception.getMessage());

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update category (user is not the owner)")
    void updateCategoryWhenUserIsNotOwnerTest() {
        String username = "Dainis";
        Long categoryId = 1L;

        User user = new User();
        user.setId(1L);

        User categoryOwner = new User();
        categoryOwner.setId(2L);

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();

        Category existingCategory = new Category();
        existingCategory.setUser(categoryOwner);

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        Exception exception = assertThrows(RuntimeException.class, () ->
                categoryService.updateCategory(categoryId, categoryRequestDTO, username));

        assertEquals("You can update only your own categories", exception.getMessage());

        verify(categoryRepository, never()).save(any());
    }

    //endregion

    //region deleteCategory

    @Test
    @DisplayName("Delete category (success)")
    void deleteCategorySuccessTest() {
        String username = "Dainis";
        Long categoryId = 1L;

        User user = new User();
        user.setId(1L);

        Category existingCategory = new Category();
        existingCategory.setUser(user);

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        categoryService.deleteCategory(categoryId, username);

        verify(categoryRepository, times(1)).delete(existingCategory);
    }

    @Test
    @DisplayName("Delete category (user not found)")
    void deleteCategoryUserNotFoundTest() {
        String username = "Dainis";
        Long categoryId = 1L;

        when(userService.findByUsername(username)).thenThrow(new RuntimeException("User not found"));

        Exception exception = assertThrows(RuntimeException.class, () ->
                categoryService.deleteCategory(categoryId, username));

        assertEquals("User not found", exception.getMessage());

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete category (category not found")
    void deleteCategoryWhenCategoryNotFoundTest() {
        String username = "Dainis";
        Long categoryId = 1L;

        User user = new User();

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                categoryService.deleteCategory(categoryId, username));

        assertEquals("Category not found", exception.getMessage());

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete category (user is not the owner)")
    void deleteCategoryWhenUserIsNotOwnerTest() {
        String username = "Dainis";
        Long categoryId = 1L;

        User user = new User();
        user.setId(1L);

        User categoryOwner = new User();
        categoryOwner.setId(2L);

        Category existingCategory = new Category();
        existingCategory.setUser(categoryOwner);

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        Exception exception = assertThrows(RuntimeException.class, () ->
                categoryService.deleteCategory(categoryId, username));

        assertEquals("You can delete only your own categories", exception.getMessage());

        verify(categoryRepository, never()).delete(any());
    }

    //endregion
}
