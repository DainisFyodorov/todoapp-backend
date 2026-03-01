package lv.dainis.todoapp.controller;

import lv.dainis.todoapp.entity.User;
import lv.dainis.todoapp.requestmodel.TaskRequestDTO;
import lv.dainis.todoapp.responsemodel.TaskResponseDTO;
import lv.dainis.todoapp.service.TaskService;
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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Get tasks endpoint (success 200 OK)")
    @Test
    @WithMockUser(username = "Dainis")
    void getTasksSuccess() throws Exception {
        String username = "Dainis";

        User user = new User();
        user.setUsername(username);

        TaskResponseDTO taskOne = new TaskResponseDTO();
        TaskResponseDTO taskTwo = new TaskResponseDTO();

        taskOne.setTitle("Task 1");
        taskTwo.setTitle("Task 2");

        when(taskService.getAllTasksByUsername(username)).thenReturn(List.of(taskOne, taskTwo));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/get"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value(taskOne.getTitle()))
                .andExpect(jsonPath("$[1].title").value(taskTwo.getTitle()));

        verify(taskService, times(1)).getAllTasksByUsername(username);
    }

    @DisplayName("Get tasks endpoint (empty list 200 OK)")
    @Test
    @WithMockUser(username = "Dainis")
    void getTasksEmptyListTest() throws Exception {
        String username = "Dainis";

        when(taskService.getAllTasksByUsername(username)).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @DisplayName("Get tasks endpoint (user not found 400 bad request)")
    @Test
    @WithMockUser(username = "Dainis")
    void getTasksUserNotFoundTest() throws Exception {
        when(taskService.getAllTasksByUsername("Dainis"))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/get"))
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
    @WithMockUser(username = "Dainis")
    void createTaskTest() throws Exception {
        Long createdId = 1L;

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

        when(taskService.createTask(any(TaskRequestDTO.class), eq("Dainis"))).thenReturn(responseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.title").value(task.getTitle()))
                .andExpect(jsonPath("$.description").value(task.getDescription()))
                .andExpect(jsonPath("$.completed").value(task.isCompleted()))
                .andExpect(jsonPath("$.categoryId").value(task.getCategoryId()));
    }

    @DisplayName("Create task endpoint (validation failure 400 bad request)")
    @Test
    @WithMockUser(username = "Dainis")
    void createTaskValidationFailureTest() throws Exception {
        TaskRequestDTO task = new TaskRequestDTO();
        task.setTitle("");
        task.setDescription("");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task))
                .with(csrf()))
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
    @WithMockUser(username = "Dainis")
    void updateTaskTest() throws Exception {
        Long taskId = 1L;
        String username = "Dainis";

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

        when(taskService.updateTask(eq(taskId), eq(task), eq(username))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task/update/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(task.getTitle()))
                .andExpect(jsonPath("$.description").value(task.getDescription()))
                .andExpect(jsonPath("$.completed").value(task.isCompleted()))
                .andExpect(jsonPath("$.categoryId").value(task.getCategoryId()));

        verify(taskService, times(1)).updateTask(eq(taskId), eq(task), eq(username));
    }

    @DisplayName("Update task endpoint (validation failure 400 bad request)")
    @Test
    @WithMockUser(username = "Dainis")
    void updateTaskValidationFailureTest() throws Exception {
        TaskRequestDTO invalidTask = new TaskRequestDTO();
        invalidTask.setTitle("");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTask))
                .with(csrf()))
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
    @WithMockUser(username = "Dainis")
    void updateTaskWhenUserIsNotTheOwnerTest() throws Exception {
        String username = "Dainis";
        Long taskId = 1L;

        TaskRequestDTO task = new TaskRequestDTO();
        task.setTitle("Task title");
        task.setDescription("");

        when(taskService.updateTask(eq(taskId), eq(task), eq(username)))
                .thenThrow(new RuntimeException("You can only edit your own tasks"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task/update/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You can only edit your own tasks"));
    }

    @DisplayName("Delete task endpoint (200 OK)")
    @Test
    @WithMockUser(username = "Dainis")
    void deleteTaskTest() throws Exception {
        Long taskId = 1L;
        String username = "Dainis";

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/delete/" + taskId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(eq(taskId), eq(username));
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
    @WithMockUser(username = "Dainis")
    void deleteTaskWhenUserIsNotTheOwnerTest() throws Exception {
        String username = "Dainis";
        Long taskId = 1L;

        doThrow(new RuntimeException("You can only delete your own tasks"))
                .when(taskService).deleteTask(eq(taskId), eq(username));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/delete/" + taskId)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You can only delete your own tasks"));
    }
}
