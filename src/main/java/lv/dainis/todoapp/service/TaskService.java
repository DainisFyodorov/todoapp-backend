package lv.dainis.todoapp.service;

import lv.dainis.todoapp.dao.TaskRepository;
import lv.dainis.todoapp.dao.UserRepository;
import lv.dainis.todoapp.entity.Task;
import lv.dainis.todoapp.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final UserService userService;

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(UserService userService, TaskRepository taskRepository) {
        this.userService = userService;
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasksByUsername(String username) {
        User user = userService.findByUsername(username);
        return taskRepository.findAllByUser(user);
    }

    public Task createTask(Task task, String username) {
        User user = userService.findByUsername(username);
        task.setUser(user);
        return taskRepository.save(task);
    }

    public void deleteTask(Long id, String username) {
        User user = userService.findByUsername(username);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own tasks");
        }

        taskRepository.delete(task);
    }
}
