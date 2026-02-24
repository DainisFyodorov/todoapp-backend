package lv.dainis.todoapp.service;

import lv.dainis.todoapp.dao.TaskRepository;
import lv.dainis.todoapp.dao.UserRepository;
import lv.dainis.todoapp.entity.Category;
import lv.dainis.todoapp.entity.Task;
import lv.dainis.todoapp.entity.User;
import lv.dainis.todoapp.requestmodel.TaskRequestDTO;
import lv.dainis.todoapp.responsemodel.TaskResponseDTO;
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
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TaskService taskService;

    @DisplayName("Get all tasks by username (success)")
    @Test
    void getAllTasksByUsernameTest() {
        String username = "Dainis";

        User user = new User();
        user.setUsername(username);

        Task taskOne = new Task();
        Task taskTwo = new Task();

        when(userService.findByUsername(username)).thenReturn(user);
        when(taskRepository.findAllByUser(user)).thenReturn(List.of(taskOne, taskTwo));

        assertEquals(2, taskService.getAllTasksByUsername(username).size());
    }

    @DisplayName("Get all tasks by username (user not found)")
    @Test
    void getAllTasksByUsernameUserNotFoundTest() {
        String username = "Dainis";

        Task taskOne = new Task();
        Task taskTwo = new Task();

        when(userService.findByUsername(username)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> taskService.getAllTasksByUsername(username));
    }

    @DisplayName("Create task (success)")
    @Test
    void createTaskSuccess() {
        String username = "Dainis";
        Long categoryId = 1L;
        Long createdId = 100L;

        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Test title");
        requestDTO.setDescription("Test description");
        requestDTO.setCategoryId(categoryId);

        User user = new User();
        user.setUsername(username);

        Category category = new Category();
        category.setId(categoryId);

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryService.findById(eq(categoryId))).thenReturn(category);
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> {
            Task savedTask = i.getArgument(0);
            savedTask.setId(createdId);
            return savedTask;
        });

        TaskResponseDTO createdTask = taskService.createTask(requestDTO, username);

        assertNotNull(createdTask);
        assertEquals(createdId, createdTask.getId());
        assertEquals(requestDTO.getTitle(), createdTask.getTitle());
        assertEquals(requestDTO.getDescription(), createdTask.getDescription());
        assertEquals(categoryId, createdTask.getCategoryId());

        verify(taskRepository).save(argThat(task ->
                task.getUser().equals(user) &&
                task.getCategory().equals(category) &&
                task.getTitle().equals(requestDTO.getTitle())));
    }

    @DisplayName("Create task (user not found)")
    @Test
    void createTaskUserNotFound() {
        String username = "Dainis";
        TaskRequestDTO task = new TaskRequestDTO();

        when(userService.findByUsername(username)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> taskService.createTask(task, username));

        verify(taskRepository, never()).save(any());
    }

    @DisplayName("Create task (category not found)")
    @Test
    void createTaskCategoryNotFound() {
        String username = "Dainis";
        Long categoryId = 1L;

        TaskRequestDTO task = new TaskRequestDTO();
        task.setCategoryId(categoryId);

        User user = new User();

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryService.findById(categoryId)).thenThrow(new RuntimeException("Category not found"));

        assertThrows(RuntimeException.class, () -> taskService.createTask(task, username));

        verify(taskRepository, never()).save(any());
    }

    @DisplayName("Update task (success)")
    @Test
    void updateTaskSuccessTest() {
        String username = "Dainis";
        Long taskId = 1L;
        Long categoryId = 2L;

        User user = new User();
        user.setUsername(username);
        user.setId(1L);

        Category category = new Category();
        category.setId(categoryId);

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle("Title before");
        existingTask.setDescription("Description before");
        existingTask.setCompleted(false);
        existingTask.setUser(user);
        existingTask.setCategory(null);

        TaskRequestDTO taskDetails = new TaskRequestDTO();
        taskDetails.setTitle("Title after");
        taskDetails.setDescription("Description after");
        taskDetails.setCompleted(true);
        taskDetails.setCategoryId(categoryId);

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryService.findById(categoryId)).thenReturn(category);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> {
            Task updated = i.getArgument(0);
            updated.setId(taskId);
            return updated;
        });

        TaskResponseDTO updatedTask = taskService.updateTask(taskId, taskDetails, username);

        assertNotNull(updatedTask);
        assertEquals(taskId, updatedTask.getId());
        assertEquals("Title after", updatedTask.getTitle());
        assertEquals("Description after", updatedTask.getDescription());
        assertTrue(updatedTask.isCompleted());
        assertEquals(categoryId, updatedTask.getCategoryId());

        verify(taskRepository, times(1)).save(existingTask);
    }

    @DisplayName("Update task (user not found)")
    @Test
    void updateTaskUserNotFoundTest() {
        String username = "Dainis";
        Long taskId = 1L;

        TaskRequestDTO task = new TaskRequestDTO();

        when(userService.findByUsername(username)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> taskService.updateTask(taskId, task, username));

        verify(taskRepository, never()).save(any());
    }

    @DisplayName("Update task (task not found)")
    @Test
    void updateTaskWhenTaskNotFoundTest() {
        String username = "Dainis";
        Long taskId = 1L;

        User user = new User();
        user.setId(1L);

        TaskRequestDTO task = new TaskRequestDTO();

        when(userService.findByUsername(username)).thenReturn(user);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.updateTask(taskId, task, username));

        verify(taskRepository, never()).save(any());
    }

    @DisplayName("Update task (user is not the owner of the task)")
    @Test
    void updateTaskUserIsNotTheOwnerOfTheTaskTest() {
        String username = "Dainis";
        Long taskId = 1L;

        User user = new User();
        user.setUsername(username);
        user.setId(1L);

        User taskOwner = new User();
        taskOwner.setUsername("Owner");
        taskOwner.setId(2L);

        Task existingTask = new Task();
        existingTask.setUser(taskOwner);

        TaskRequestDTO taskDetails = new TaskRequestDTO();

        when(userService.findByUsername(username)).thenReturn(user);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                taskService.updateTask(taskId, taskDetails, username));

        assertEquals("You can only edit your own tasks", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @DisplayName("Delete task (success)")
    @Test
    void deleteTaskSuccessTest() {
        String username = "Dainis";
        Long taskId = 1L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Task task = new Task();
        task.setUser(user);

        when(userService.findByUsername(username)).thenReturn(user);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertDoesNotThrow(() -> taskService.deleteTask(taskId, username));
        verify(taskRepository, times(1)).delete(task);
    }

    @DisplayName("Delete task (user not found)")
    @Test
    void deleteTaskUserNotFoundTest() {
        String username = "Dainis";
        Long taskId = 1L;

        when(userService.findByUsername(username)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> taskService.deleteTask(taskId, username));
        verify(taskRepository, never()).delete(any());
    }

    @DisplayName("Delete task (task not found)")
    @Test
    void deleteTaskWhenTaskNotFoundTest() {
        String username = "Dainis";
        Long taskId = 1L;

        User user = new User();
        user.setId(1L);

        when(userService.findByUsername(username)).thenReturn(user);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.deleteTask(taskId, username));
        verify(taskRepository, never()).delete(any());
    }

    @DisplayName("Delete task (user is not the owner of the task)")
    @Test
    void deleteTaskWhenUserIsNotTheOwnerTest() {
        String username = "Dainis";
        Long taskId = 1L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        User taskOwner = new User();
        taskOwner.setId(2L);

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setUser(taskOwner);

        when(userService.findByUsername(username)).thenReturn(user);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        Exception exception = assertThrows(RuntimeException.class, () -> taskService.deleteTask(taskId, username));

        assertEquals("You can only delete your own tasks", exception.getMessage());
        verify(taskRepository, never()).delete(any());
    }
}
