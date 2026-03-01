package lv.dainis.todoapp.responsemodel;

import lombok.Data;

@Data
public class TaskResponseDTO {

    private Long id;

    private String title;

    private String description;

    private boolean completed;

    private Long categoryId;
}
