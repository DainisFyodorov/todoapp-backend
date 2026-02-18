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
    public ResponseEntity<List<Task>> getAllTasks(Principal principal) {
        return ResponseEntity.ok().body(taskService.getAllTasksByUsername(principal.getName()));
    }

    @PostMapping("/create")
    public ResponseEntity<Task> createTask(@RequestBody Task task, Principal principal) {
        Task createdTask = taskService.createTask(task, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task, Principal principal) {
        Task updatedTask = taskService.updateTask(id, task, principal.getName());
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Principal principal) {
        taskService.deleteTask(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

}
