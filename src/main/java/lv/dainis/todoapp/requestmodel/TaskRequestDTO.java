package lv.dainis.todoapp.requestmodel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskRequestDTO {

    @Size(min = 3, max = 30, message = "Title length must be between 3 and 30 characters")
    @NotNull(message = "Title is required")
    private String title;

    @NotNull(message = "Description cannot be null")
    private String description;

    @NotNull(message = "Completed cannot be null")
    private boolean completed;

    private Long categoryId;

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : null;
    }
}
