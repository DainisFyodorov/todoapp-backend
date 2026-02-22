package lv.dainis.todoapp.dao;

import lv.dainis.todoapp.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
