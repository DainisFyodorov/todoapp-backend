package lv.dainis.todoapp.responsemodel;

import lombok.Data;
import lv.dainis.todoapp.entity.TaskPriority;

@Data
public class TaskResponseDTO {

    private Long id;

    private String title;

    private String description;

    private boolean completed;

    private TaskPriority priority;

    private Long categoryId;
}
