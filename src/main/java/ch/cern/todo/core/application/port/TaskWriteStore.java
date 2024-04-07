package ch.cern.todo.core.application.port;

import ch.cern.todo.core.domain.Task;

import java.util.Optional;

public interface TaskWriteStore {

    Task save(Task task);

    void delete(final Long id);

    Optional<Task> update(final Task task);

}
