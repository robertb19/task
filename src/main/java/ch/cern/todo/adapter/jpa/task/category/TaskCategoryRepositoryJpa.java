package ch.cern.todo.adapter.jpa.task.category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCategoryRepositoryJpa extends JpaRepository<TaskCategoryEntity, Long> {}
