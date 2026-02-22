package lv.dainis.todoapp.dao;

import lv.dainis.todoapp.entity.Category;
import lv.dainis.todoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByUser(User user);
}
