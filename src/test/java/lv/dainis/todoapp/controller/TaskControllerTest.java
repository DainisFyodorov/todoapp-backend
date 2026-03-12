package lv.dainis.todoapp.controller;

import lv.dainis.todoapp.config.SecurityConfiguration;
import lv.dainis.todoapp.entity.User;
import lv.dainis.todoapp.entity.UserPrincipal;
import lv.dainis.todoapp.requestmodel.TaskRequestDTO;
import lv.dainis.todoapp.responsemodel.TaskResponseDTO;
import lv.dainis.todoapp.service.CustomOAuth2UserService;
import lv.dainis.todoapp.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(SecurityConfiguration.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @DisplayName("Get tasks endpoint (success 200 OK)")
    @Test
    void getTasksSuccess() throws Exception {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        TaskResponseDTO taskOne = new TaskResponseDTO();
        TaskResponseDTO taskTwo = new TaskResponseDTO();

        taskOne.setTitle("Task 1");
        taskTwo.setTitle("Task 2");

        when(taskService.getAllTasksByUserId(userId)).thenReturn(List.of(taskOne, taskTwo));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/get")
                .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].title").value(taskOne.getTitle()))
                    .andExpect(jsonPath("$[1].title").value(taskTwo.getTitle()));

        verify(taskService, times(1)).getAllTasksByUserId(userId);
    }

    @DisplayName("Get tasks endpoint (empty list 200 OK)")
    @Test
    void getTasksEmptyListTest() throws Exception {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        when(taskService.getAllTasksByUserId(userId)).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/get")
                .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
    }

    @DisplayName("Get tasks endpoint (user not found 400 bad request)")
    @Test
    void getTasksUserNotFoundTest() throws Exception {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        when(taskService.getAllTasksByUserId(userId))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/get")
                .with(user(principal)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("User not found"));
    }

    @DisplayName("Get tasks endpoint (unauthorized 401/403)")
    @Test
    void getTasksUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/get"))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Create task endpoint (success 201 created)")
    @Test
    void createTaskTest() throws Exception {
        Long createdId = 1L;
        Long userId = 2L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        TaskRequestDTO task = new TaskRequestDTO();
        task.setTitle("Task");
        task.setDescription("Description");
        task.setCompleted(false);
        task.setCategoryId(null);

        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setId(createdId);
        responseDTO.setTitle("Task");
        responseDTO.setDescription("Description");
        responseDTO.setCompleted(false);
        responseDTO.setCategoryId(null);

        when(taskService.createTask(any(TaskRequestDTO.class), eq(userId))).thenReturn(responseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task))
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(createdId))
                    .andExpect(jsonPath("$.title").value(task.getTitle()))
                    .andExpect(jsonPath("$.description").value(task.getDescription()))
                    .andExpect(jsonPath("$.completed").value(task.isCompleted()))
                    .andExpect(jsonPath("$.categoryId").value(task.getCategoryId()));
    }

    @DisplayName("Create task endpoint (validation failure 400 bad request)")
    @Test
    void createTaskValidationFailureTest() throws Exception {
        TaskRequestDTO task = new TaskRequestDTO();
        task.setTitle("");
        task.setDescription("");

        UserPrincipal principal = new UserPrincipal(new User(), List.of(new SimpleGrantedAuthority("USER")));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task))
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isBadRequest());
    }

    @DisplayName("Create task endpoint (user not logged in 401)")
    @Test
    void createTaskNotLoggedInTest() throws Exception {
        TaskRequestDTO task = new TaskRequestDTO();
        task.setTitle("Task title");
        task.setDescription("Task description");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Update task endpoint (200 OK)")
    @Test
    void updateTaskTest() throws Exception {
        Long taskId = 1L;
        Long userId = 2L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        TaskRequestDTO task = new TaskRequestDTO();
        task.setTitle("Title");
        task.setDescription("Description");
        task.setCompleted(true);
        task.setCategoryId(null);

        TaskResponseDTO response = new TaskResponseDTO();
        response.setTitle("Title");
        response.setDescription("Description");
        response.setCompleted(true);
        response.setCategoryId(null);

        when(taskService.updateTask(eq(taskId), eq(task), eq(userId))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task/update/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task))
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value(task.getTitle()))
                    .andExpect(jsonPath("$.description").value(task.getDescription()))
                    .andExpect(jsonPath("$.completed").value(task.isCompleted()))
                    .andExpect(jsonPath("$.categoryId").value(task.getCategoryId()));

        verify(taskService, times(1)).updateTask(eq(taskId), eq(task), eq(userId));
    }

    @DisplayName("Update task endpoint (validation failure 400 bad request)")
    @Test
    void updateTaskValidationFailureTest() throws Exception {
        TaskRequestDTO invalidTask = new TaskRequestDTO();
        invalidTask.setTitle("");

        UserPrincipal principal = new UserPrincipal(new User(), List.of(new SimpleGrantedAuthority("USER")));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTask))
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isBadRequest());
    }

    @DisplayName("Update task endpoint (unauthorized 401)")
    @Test
    void updateTaskUnauthorizedTest() throws Exception {
        TaskRequestDTO task = new TaskRequestDTO();
        task.setTitle("Task title");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task))
                .with(csrf()))
                    .andExpect(status().isUnauthorized());
    }

    @DisplayName("Update task endpoint (user is not the owner of the task 400 bad request)")
    @Test
    void updateTaskWhenUserIsNotTheOwnerTest() throws Exception {
        Long userId = 2L;
        Long taskId = 1L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        TaskRequestDTO task = new TaskRequestDTO();
        task.setTitle("Task title");
        task.setDescription("");

        when(taskService.updateTask(eq(taskId), eq(task), eq(userId)))
                .thenThrow(new RuntimeException("You can only edit your own tasks"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task/update/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task))
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("You can only edit your own tasks"));
    }

    @DisplayName("Delete task endpoint (200 OK)")
    @Test
    void deleteTaskTest() throws Exception {
        Long taskId = 1L;
        Long userId = 2L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/delete/" + taskId)
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(eq(taskId), eq(userId));
    }

    @DisplayName("Delete task endpoint (401 unauthorized)")
    @Test
    void deleteTaskWhenNotLoggedInt() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/delete/1")
                .with(csrf()))
                    .andExpect(status().isUnauthorized());

        verify(taskService, never()).deleteTask(any(), any());
    }

    @DisplayName("Delete task endpoint (user is not the owner of the task 400 bad request)")
    @Test
    void deleteTaskWhenUserIsNotTheOwnerTest() throws Exception {
        Long userId = 2L;
        Long taskId = 1L;

        User user = new User();
        user.setId(userId);

        UserPrincipal principal = new UserPrincipal(user, List.of(new SimpleGrantedAuthority("USER")));

        doThrow(new RuntimeException("You can only delete your own tasks"))
                .when(taskService).deleteTask(eq(taskId), eq(userId));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/delete/" + taskId)
                .with(csrf())
                .with(user(principal)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("You can only delete your own tasks"));
    }
}
