package lv.dainis.todoapp.controller;

import jakarta.validation.Valid;
import lv.dainis.todoapp.entity.TaskPriority;
import lv.dainis.todoapp.entity.UserPrincipal;
import lv.dainis.todoapp.requestmodel.TaskRequestDTO;
import lv.dainis.todoapp.responsemodel.TaskResponseDTO;
import lv.dainis.todoapp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/get")
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok().body(taskService.getAllTasksByUserId(principal.getId()));
    }

    @PostMapping("/create")
    public ResponseEntity<TaskResponseDTO> createTask(
            @Valid @RequestBody TaskRequestDTO task,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        TaskResponseDTO createdTask = taskService.createTask(task, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDTO task,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        TaskResponseDTO updatedTask = taskService.updateTask(id, task, principal.getId());
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        taskService.deleteTask(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/priorities")
    public ResponseEntity<List<String>> getPriorities() {
        List<String> priorities = Arrays.stream(TaskPriority.values()).map(Enum::name).toList();
        return ResponseEntity.ok(priorities);
    }
}
