package lv.dainis.todoapp.dao;

import lv.dainis.todoapp.entity.Task;
import lv.dainis.todoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByUser(User user);

    @Modifying
    @Query("""
        update Task t 
        set t.category = null 
        where t.category.id = :categoryId
    """)
    void clearTasksFromCategory(Long categoryId);
}
