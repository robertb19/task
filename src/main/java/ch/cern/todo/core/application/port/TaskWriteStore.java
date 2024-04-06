package ch.cern.todo.core.application.port;

import ch.cern.todo.core.domain.Task;

public interface TaskWriteStore {

    Task save(Task task);

}
