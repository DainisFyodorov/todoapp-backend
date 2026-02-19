package lv.dainis.todoapp.controller;

import lv.dainis.todoapp.entity.User;
import lv.dainis.todoapp.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Registration endpoint (success - 200 OK)")
    @Test
    void registerSuccessTest() throws Exception {
        User user = new User();
        user.setUsername("Dainis");
        user.setPassword("password");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    @DisplayName("Registration endpoint (validation failed 400 bad request)")
    @Test
    void registerValidationFailureTest() throws Exception {
        User user = new User();
        user.setUsername("");
        user.setPassword("");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Registration endpoint (username already taken 400 bad request)")
    @Test
    void registerUsernameAlreadyTakenTest() throws Exception {
        User user = new User();
        user.setUsername("Dainis");
        user.setPassword("password");

        doThrow(new RuntimeException("Username is already taken"))
                .when(userService).registerUser(any(User.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username is already taken"));
    }

    @DisplayName("Check status endpoint (logged in)")
    @Test
    @WithMockUser(username = "Dainis")
    void checkStatusTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLoggedIn").value(true));
    }

    @DisplayName("Check status endpoint (not logged in)")
    @Test
    void checkStatusNotLoggedInTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLoggedIn").value(false));
    }
}
