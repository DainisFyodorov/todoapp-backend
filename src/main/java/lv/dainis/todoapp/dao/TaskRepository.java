package lv.dainis.todoapp.dao;

import lv.dainis.todoapp.entity.Task;
import lv.dainis.todoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByUser(User user);
}
