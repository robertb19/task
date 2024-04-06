package ch.cern.todo.core.application.port;

import ch.cern.todo.core.domain.TaskCategory;

import java.util.Optional;

public interface TaskCategoryWriteStore {

    TaskCategory save(TaskCategory taskCategory);

    Optional<TaskCategory> get(Long id);

    Optional<TaskCategory> update(TaskCategory taskCategory);

    void delete(final Long id);
}
