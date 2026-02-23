package lv.dainis.todoapp.requestmodel;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequestDTO {

    @Size(min = 3, max = 30, message = "Category name length must be between 3 and 30 characters")
    private String name;
}
