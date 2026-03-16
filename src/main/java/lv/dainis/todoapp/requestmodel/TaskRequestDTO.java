package lv.dainis.todoapp.requestmodel;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lv.dainis.todoapp.entity.TaskPriority;

import java.time.LocalDate;

@Data
public class TaskRequestDTO {

    @Size(min = 3, max = 30, message = "Title length must be between 3 and 30 characters")
    @NotNull(message = "Title is required")
    private String title;

    @NotNull(message = "Description cannot be null")
    private String description;

    @NotNull(message = "Completed cannot be null")
    private boolean completed;

    @NotNull(message = "Priority cannot be null")
    private TaskPriority priority;

    @Nullable
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private Long categoryId;

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : null;
    }
}
