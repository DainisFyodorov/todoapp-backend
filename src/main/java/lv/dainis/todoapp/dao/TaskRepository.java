package lv.dainis.todoapp.dao;

import lv.dainis.todoapp.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
