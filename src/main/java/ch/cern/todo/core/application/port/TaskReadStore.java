package ch.cern.todo.core.application.port;

import ch.cern.todo.core.application.query.dto.CustomPage;
import ch.cern.todo.core.application.query.dto.TaskFilters;
import ch.cern.todo.core.application.query.dto.TaskProjection;
import java.util.Optional;

public interface TaskReadStore {

    Optional<TaskProjection> getTask(Long id);

    CustomPage<TaskProjection> getTasks(final TaskFilters taskFilters);

}
