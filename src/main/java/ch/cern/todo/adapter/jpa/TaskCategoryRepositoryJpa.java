package ch.cern.todo.adapter.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCategoryRepositoryJpa extends JpaRepository<TaskCategoryEntity, Long> {

    Page<TaskCategoryEntity> findAll(Pageable pageable);

    Page<TaskCategoryEntity> findAllByName(String name, Pageable pageable);

}
