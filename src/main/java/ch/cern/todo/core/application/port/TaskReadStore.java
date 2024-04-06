package ch.cern.todo.core.application.port;

import ch.cern.todo.core.application.query.dto.TaskProjection;
import java.util.Optional;

public interface TaskReadStore {

    Optional<TaskProjection> getTask(Long id);

}
