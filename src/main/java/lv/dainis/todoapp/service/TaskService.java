package lv.dainis.todoapp.service;

import lv.dainis.todoapp.dao.TaskRepository;
import lv.dainis.todoapp.entity.Category;
import lv.dainis.todoapp.entity.Task;
import lv.dainis.todoapp.entity.User;
import lv.dainis.todoapp.requestmodel.TaskRequestDTO;
import lv.dainis.todoapp.responsemodel.TaskResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final UserService userService;

    private final CategoryService categoryService;

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(UserService userService, CategoryService categoryService, TaskRepository taskRepository) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.taskRepository = taskRepository;
    }

    public List<TaskResponseDTO> getAllTasksByUserId(Long userId) {
        User user = userService.findById(userId);
        return taskRepository.findAllByUser(user).stream().map(Task::toDTO).toList();
    }

    public TaskResponseDTO createTask(TaskRequestDTO task, Long userId) {
        User user = userService.findById(userId);
        Category category = null;

        if(task.getCategoryId() != null) {
            category = categoryService.findById(task.getCategoryId());
        }

        Task createdTask = Task.fromDTO(task, user, category);

        return taskRepository.save(createdTask).toDTO();
    }

    public TaskResponseDTO updateTask(Long id, TaskRequestDTO taskDetails, Long userId) {
        User user = userService.findById(userId);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if(!task.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only edit your own tasks");
        }

        Category category = null;
        if(taskDetails.getCategoryId() != null) {
            category = categoryService.findById(taskDetails.getCategoryId());
        }

        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());
        task.setPriority(taskDetails.getPriority());
        task.setDueDate(taskDetails.getDueDate());
        task.setCategory(category);

        return taskRepository.save(task).toDTO();
    }

    public void deleteTask(Long id, Long userId) {
        User user = userService.findById(userId);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own tasks");
        }

        taskRepository.delete(task);
    }
}
