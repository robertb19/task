package ch.cern.todo.core.application.task.port;

import ch.cern.todo.core.application.dto.CustomPage;
import ch.cern.todo.core.application.task.query.dto.TaskFilters;
import ch.cern.todo.core.application.task.query.dto.TaskProjection;

import java.util.Optional;

public interface TaskReadStore {

    Optional<TaskProjection> getTask(Long id);

    CustomPage<TaskProjection> getTasks(final TaskFilters taskFilters);

}
