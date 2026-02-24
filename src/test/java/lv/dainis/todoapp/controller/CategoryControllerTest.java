package lv.dainis.todoapp.controller;

import lv.dainis.todoapp.entity.Category;
import lv.dainis.todoapp.requestmodel.CategoryRequestDTO;
import lv.dainis.todoapp.responsemodel.CategoryResponseDTO;
import lv.dainis.todoapp.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    //region getAllCategoriesByUsername

    @Test
    @DisplayName("Get all categories (success 200 OK)")
    @WithMockUser(username = "Dainis")
    void getAllUserCategoriesTest() throws Exception {
        String username = "Dainis";

        CategoryResponseDTO categoryOne = new CategoryResponseDTO();
        categoryOne.setId(1L);
        categoryOne.setName("First category");

        CategoryResponseDTO categoryTwo = new CategoryResponseDTO();
        categoryTwo.setId(2L);
        categoryTwo.setName("Second category");

        when(categoryService.getAllCategoriesByUsername(username)).thenReturn(List.of(categoryOne, categoryTwo));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/category"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(categoryOne.getId()))
                .andExpect(jsonPath("$[0].name").value(categoryOne.getName()))
                .andExpect(jsonPath("$[1].id").value(categoryTwo.getId()))
                .andExpect(jsonPath("$[1].name").value(categoryTwo.getName()));

        verify(categoryService, times(1)).getAllCategoriesByUsername(username);
    }

    @Test
    @DisplayName("Get all categories (empty list 200 OK)")
    @WithMockUser(username = "Dainis")
    void getAllUserCategoriesEmptyTest() throws Exception {
        String username = "Dainis";

        when(categoryService.getAllCategoriesByUsername(username)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/category"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Get all categories (user not found 400 bad request)")
    @WithMockUser(username = "Dainis")
    void getAllUserCategoriesUserNotFoundBadRequestTest() throws Exception {
        String username = "Dainis";

        when(categoryService.getAllCategoriesByUsername(username)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/category"))
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
    @WithMockUser(username = "Dainis")
    void createCategorySuccessTest() throws Exception {
        String username = "Dainis";

        CategoryRequestDTO categoryDTO = new CategoryRequestDTO();
        categoryDTO.setName("Test category");

        CategoryResponseDTO responseDTO = new CategoryResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName(categoryDTO.getName());

        when(categoryService.createCategory(any(CategoryRequestDTO.class), eq(username))).thenReturn(responseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/category")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO))
                .with(csrf()))
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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/category")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO))
                .with(csrf()))
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
}
