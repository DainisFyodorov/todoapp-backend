package lv.dainis.todoapp.responsemodel;

import lombok.Data;
import lv.dainis.todoapp.entity.TaskPriority;

import java.time.LocalDate;

@Data
public class TaskResponseDTO {

    private Long id;

    private String title;

    private String description;

    private boolean completed;

    private TaskPriority priority;

    private LocalDate dueDate;

    private Long categoryId;
}
