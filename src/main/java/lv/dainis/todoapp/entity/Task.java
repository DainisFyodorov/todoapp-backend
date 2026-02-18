package lv.dainis.todoapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "task")
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String title;
    private String description;
    private boolean completed;
}
