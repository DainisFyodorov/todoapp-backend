package lv.dainis.todoapp.controller;

import lv.dainis.todoapp.config.SecurityConfiguration;
import lv.dainis.todoapp.entity.User;
import lv.dainis.todoapp.entity.UserPrincipal;
import lv.dainis.todoapp.requestmodel.CategoryRequestDTO;
import lv.dainis.todoapp.responsemodel.CategoryResponseDTO;
import lv.dainis.todoapp.service.CategoryService;
import lv.dainis.todoapp.service.CustomOAuth2UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfiguration.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    //region getAllCategoriesByUsername

    @Test
    @DisplayName("Get all categories (success 200 OK)")
    void getAllUserCategoriesTest() throws Exception {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        CategoryResponseDTO categoryOne = new CategoryResponseDTO();
        categoryOne.setId(1L);
        categoryOne.setName("First category");

        CategoryResponseDTO categoryTwo = new CategoryResponseDTO();
        categoryTwo.setId(2L);
        categoryTwo.setName("Second category");

        when(categoryService.getAllCategoriesByUserId(userId)).thenReturn(List.of(categoryOne, categoryTwo));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/category")
                .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(categoryOne.getId()))
                    .andExpect(jsonPath("$[0].name").value(categoryOne.getName()))
                    .andExpect(jsonPath("$[1].id").value(categoryTwo.getId()))
                    .andExpect(jsonPath("$[1].name").value(categoryTwo.getName()));

        verify(categoryService, times(1)).getAllCategoriesByUserId(userId);
    }

    @Test
    @DisplayName("Get all categories (empty list 200 OK)")
    void getAllUserCategoriesEmptyTest() throws Exception {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        when(categoryService.getAllCategoriesByUserId(userId)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/category")
                .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Get all categories (user not found 400 bad request)")
    void getAllUserCategoriesUserNotFoundBadRequestTest() throws Exception {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        when(categoryService.getAllCategoriesByUserId(userId)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/category")
                .with(user(principal)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("Get all categories (unauthorized)")
    void getAllUserCategoriesUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/category"))
                .andExpect(status().isUnauthorized());
    }

    //endregion

    //region createCategory

    @Test
    @DisplayName("Create category (success 200 OK)")
    void createCategorySuccessTest() throws Exception {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        CategoryRequestDTO categoryDTO = new CategoryRequestDTO();
        categoryDTO.setName("Test category");

        CategoryResponseDTO responseDTO = new CategoryResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName(categoryDTO.getName());

        when(categoryService.createCategory(any(CategoryRequestDTO.class), eq(userId))).thenReturn(responseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/category")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO))
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(responseDTO.getId()))
                    .andExpect(jsonPath("$.name").value(categoryDTO.getName()));
    }

    @Test
    @DisplayName("Create category (validation failure 400 bad request)")
    @WithMockUser(username = "Dainis")
    void createCategoryValidationFailureTest() throws Exception {
        CategoryRequestDTO categoryDTO = new CategoryRequestDTO();
        categoryDTO.setName("");

        UserPrincipal principal = new UserPrincipal(new User(), List.of(new SimpleGrantedAuthority("USER")));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/category")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO))
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Create category (unauthorized)")
    void createCategoryUnauthorizedTest() throws Exception {
        CategoryRequestDTO categoryDTO = new CategoryRequestDTO();
        categoryDTO.setName("123456");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/category")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO))
                .with(csrf()))
                    .andExpect(status().isUnauthorized());
    }

    //endregion

    //region updateCategory

    @Test
    @DisplayName("Update category (success 200 OK)")
    void updateCategorySuccessTest() throws Exception {
        Long userId = 2L;
        Long categoryId = 1L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        CategoryRequestDTO categoryDTO = new CategoryRequestDTO();
        categoryDTO.setName("Category name");

        CategoryResponseDTO responseDTO = new CategoryResponseDTO();
        responseDTO.setId(categoryId);
        responseDTO.setName("Category name");

        when(categoryService.updateCategory(categoryId, categoryDTO, userId)).thenReturn(responseDTO);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/category/" + categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO))
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(categoryId))
                    .andExpect(jsonPath("$.name").value(categoryDTO.getName()));

        verify(categoryService, times(1)).updateCategory(eq(categoryId), eq(categoryDTO), eq(userId));
    }

    @Test
    @DisplayName("Update category (validation failure bad request)")
    void updateCategoryValidationFailureTest() throws Exception {
        Long categoryId = 1L;

        UserPrincipal principal = new UserPrincipal(new User(), List.of(new SimpleGrantedAuthority("USER")));

        CategoryRequestDTO categoryDTO = new CategoryRequestDTO();
        categoryDTO.setName("");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/category/" + categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO))
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isBadRequest());

        verify(categoryService, never()).updateCategory(eq(categoryId), eq(categoryDTO), any());
    }

    @Test
    @DisplayName("Update category (unauthorized)")
    void updateCategoryUnauthorizedTest() throws Exception {
        CategoryRequestDTO categoryDTO = new CategoryRequestDTO();
        categoryDTO.setName("");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/category/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO))
                .with(csrf()))
                    .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Update category (user is not the owner 400 bad request)")
    void updateCategoryWhenUserIsNotTheOwnerTest() throws Exception {
        Long userId = 2L;
        Long categoryId = 1L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        CategoryRequestDTO categoryRequestDTO = new CategoryRequestDTO();
        categoryRequestDTO.setName("123456");

        when(categoryService.updateCategory(eq(categoryId), eq(categoryRequestDTO), eq(userId))).thenThrow(
                new RuntimeException("You can only edit your own categories"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/category/" + categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequestDTO))
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("You can only edit your own categories"));
    }

    //endregion

    //region deleteCategory

    @Test
    @DisplayName("Delete category (success 200 OK)")
    void deleteCategorySuccessTest() throws Exception {
        Long userId = 2L;
        Long categoryId = 1L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/category/" + categoryId)
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(eq(categoryId), eq(userId));
    }

    @Test
    @DisplayName("Delete category (unauthorized)")
    void deleteCategoryUnauthorizedTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/category/1")
                .with(csrf()))
                    .andExpect(status().isUnauthorized());

        verify(categoryService, never()).deleteCategory(any(), any());
    }

    @Test
    @DisplayName("Delete category (user is not the owner)")
    void deleteCategoryWhenUserIsNotTheOwnerTest() throws Exception {
        Long userId = 2L;
        Long categoryId = 1L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        doThrow(new RuntimeException("You can delete only your own tasks"))
                .when(categoryService).deleteCategory(eq(categoryId), eq(userId));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/category/" + categoryId)
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("You can delete only your own tasks"));
    }

    //endregion
}
