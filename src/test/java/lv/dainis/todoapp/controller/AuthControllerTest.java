package lv.dainis.todoapp.controller;

import lv.dainis.todoapp.config.SecurityConfiguration;
import lv.dainis.todoapp.entity.User;
import lv.dainis.todoapp.entity.UserPrincipal;
import lv.dainis.todoapp.responsemodel.UserInfoResponse;
import lv.dainis.todoapp.service.CustomOAuth2UserService;
import lv.dainis.todoapp.service.UserService;
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


import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfiguration.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

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
    void checkStatusTest() throws Exception {
        String username = "Dainis";

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        UserInfoResponse response = new UserInfoResponse();
        response.setUsername(username);
        response.setLoggedIn(true);

        when(userService.getUserInformation(any())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/me")
                .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.loggedIn").value(true))
                    .andExpect(jsonPath("$.username").value("Dainis"));
    }

    @DisplayName("Check status endpoint (not logged in)")
    @Test
    void checkStatusNotLoggedInTest() throws Exception {
        UserInfoResponse response = new UserInfoResponse();
        response.setUsername("");
        response.setLoggedIn(false);

        when(userService.getUserInformation(any())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loggedIn").value(false))
                .andExpect(jsonPath("$.username").value(""));
    }
}
