package lv.dainis.todoapp.controller;

import lv.dainis.todoapp.entity.Task;
import lv.dainis.todoapp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    public ResponseEntity<?> getAllTasks(Principal principal) {
        try {
            return ResponseEntity.ok().body(taskService.getAllTasksByUsername(principal.getName()));
        } catch(RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTask(@RequestBody Task task, Principal principal) {
        try {
            Task createdTask = taskService.createTask(task, principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch(RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Principal principal) {
        try {
            taskService.deleteTask(id, principal.getName());
            return ResponseEntity.noContent().build();
        } catch(RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
