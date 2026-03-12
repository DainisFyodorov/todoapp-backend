package lv.dainis.todoapp.service;

import lv.dainis.todoapp.dao.CategoryRepository;
import lv.dainis.todoapp.dao.TaskRepository;
import lv.dainis.todoapp.entity.Category;
import lv.dainis.todoapp.entity.Task;
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
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CategoryService categoryService;

    //region findById

    @Test
    @DisplayName("Find by id (success)")
    void findByIdSuccessTest() {
        Long categoryId = 3L;

        Category category = new Category();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        assertEquals(category, categoryService.findById(categoryId));
    }

    @Test
    @DisplayName("Find by id (not found)")
    void findByIdNotFoundTest() {
        Long categoryId = 3L;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> categoryService.findById(categoryId));
        assertEquals("Category not found", exception.getMessage());
    }

    //endregion

    //region getAllCategoriesByUsername
    @Test
    @DisplayName("Get all user's categories (success)")
    void getUserCategoriesTest() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Category categoryOne = new Category();
        Category categoryTwo = new Category();

        when(userService.findById(userId)).thenReturn(user);
        when(categoryRepository.findAllByUser(user)).thenReturn(List.of(categoryOne, categoryTwo));

        List<CategoryResponseDTO> categories = categoryService.getAllCategoriesByUserId(userId);
        assertEquals(2, categories.size());
    }

    @Test
    @DisplayName("Get all user's categories (user not found)")
    void getUserCategoriesUserNotFoundTest() {
        Long userId = 1L;

        when(userService.findById(userId)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> categoryService.getAllCategoriesByUserId(userId));
    }
    //endregion

    //region createCategory
    @Test
    @DisplayName("Create category (success)")
    void createCategorySuccessTest() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();
        categoryRequestDTO.setName("Test category");

        when(userService.findById(userId)).thenReturn(user);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryResponseDTO categoryResponseDTO = categoryService.createCategory(categoryRequestDTO, userId);

        assertNotNull(categoryResponseDTO);
        assertNotEquals(0, categoryResponseDTO.getId());
        assertEquals(categoryRequestDTO.getName(), categoryResponseDTO.getName());

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Create category (user not found)")
    void createCategoryUserNotFoundTest() {
        Long userId = 1L;

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();

        when(userService.findById(userId)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> categoryService.createCategory(categoryRequestDTO, userId));

        verify(categoryRepository, never()).save(any());
    }
    //endregion

    //region updateCategory

    @Test
    @DisplayName("Update category (success)")
    void updateCategorySuccessTest() {
        Long userId = 1L;
        Long categoryId = 1L;

        User user = new User();
        user.setId(userId);

        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Before title");
        existingCategory.setUser(user);

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();
        categoryRequestDTO.setName("After title");

        when(userService.findById(userId)).thenReturn(user);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryResponseDTO responseDTO = categoryService.updateCategory(categoryId, categoryRequestDTO, userId);

        assertNotNull(responseDTO);
        assertEquals(categoryRequestDTO.getName(), existingCategory.getName());
        assertEquals(categoryRequestDTO.getName(), responseDTO.getName());
        assertEquals(categoryId, responseDTO.getId());

        verify(categoryRepository, times(1)).save(existingCategory);
    }

    @Test
    @DisplayName("Update category (user not found)")
    void updateCategoryUserNotFoundTest() {
        Long userId = 1L;

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();

        when(userService.findById(userId)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () ->
                categoryService.updateCategory(1L, categoryRequestDTO, userId));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update category (category not found)")
    void updateCategoryWhenCategoryNotFoundTest() {
        Long userId = 2L;
        Long categoryId = 1L;

        User user = new User();
        user.setId(userId);

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();

        when(userService.findById(userId)).thenReturn(user);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(RuntimeException.class, () ->
                categoryService.updateCategory(categoryId, categoryRequestDTO, userId));

        assertEquals("Category not found", exception.getMessage());

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update category (user is not the owner)")
    void updateCategoryWhenUserIsNotOwnerTest() {
        Long userId = 2L;
        Long ownerId = 3L;
        Long categoryId = 1L;

        User user = new User();
        user.setId(userId);

        User categoryOwner = new User();
        categoryOwner.setId(ownerId);

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();

        Category existingCategory = new Category();
        existingCategory.setUser(categoryOwner);

        when(userService.findById(userId)).thenReturn(user);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        Exception exception = assertThrows(RuntimeException.class, () ->
                categoryService.updateCategory(categoryId, categoryRequestDTO, userId));

        assertEquals("You can update only your own categories", exception.getMessage());

        verify(categoryRepository, never()).save(any());
    }

    //endregion

    //region deleteCategory

    @Test
    @DisplayName("Delete category (success)")
    void deleteCategorySuccessTest() {
        Long userId = 2L;
        Long categoryId = 1L;

        User user = new User();
        user.setId(userId);

        Category existingCategory = new Category();
        existingCategory.setUser(user);

        Task existingTask = new Task();
        existingTask.setCategory(existingCategory);

        when(userService.findById(userId)).thenReturn(user);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        doAnswer(i -> {
            existingTask.setCategory(null);
            return null;
        }).when(taskRepository).clearTasksFromCategory(eq(categoryId));

        categoryService.deleteCategory(categoryId, userId);

        assertNull(existingTask.getCategory());

        verify(taskRepository, times(1)).clearTasksFromCategory(eq(categoryId));
        verify(categoryRepository, times(1)).delete(existingCategory);
    }

    @Test
    @DisplayName("Delete category (user not found)")
    void deleteCategoryUserNotFoundTest() {
        Long userId = 2L;
        Long categoryId = 1L;

        when(userService.findById(userId)).thenThrow(new RuntimeException("User not found"));

        Exception exception = assertThrows(RuntimeException.class, () ->
                categoryService.deleteCategory(categoryId, userId));

        assertEquals("User not found", exception.getMessage());

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete category (category not found")
    void deleteCategoryWhenCategoryNotFoundTest() {
        Long userId = 2L;
        Long categoryId = 1L;

        User user = new User();
        user.setId(userId);

        when(userService.findById(userId)).thenReturn(user);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                categoryService.deleteCategory(categoryId, userId));

        assertEquals("Category not found", exception.getMessage());

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete category (user is not the owner)")
    void deleteCategoryWhenUserIsNotOwnerTest() {
        Long userId = 2L;
        Long ownerId = 3L;
        Long categoryId = 1L;

        User user = new User();
        user.setId(userId);

        User categoryOwner = new User();
        categoryOwner.setId(ownerId);

        Category existingCategory = new Category();
        existingCategory.setUser(categoryOwner);

        when(userService.findById(userId)).thenReturn(user);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        Exception exception = assertThrows(RuntimeException.class, () ->
                categoryService.deleteCategory(categoryId, userId));

        assertEquals("You can delete only your own categories", exception.getMessage());

        verify(categoryRepository, never()).delete(any());
    }

    //endregion
}
