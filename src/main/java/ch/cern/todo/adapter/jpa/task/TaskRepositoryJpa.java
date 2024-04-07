package ch.cern.todo.adapter.jpa.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepositoryJpa extends JpaRepository<TaskEntity, Long> {

    Page<TaskEntity> findAll(Pageable pageable);


}
