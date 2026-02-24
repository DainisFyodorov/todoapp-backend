package lv.dainis.todoapp.requestmodel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequestDTO {

    @Size(min = 3, max = 30, message = "Category name length must be between 3 and 30 characters")
    @NotNull(message = "Category name is required")
    private String name;
}
