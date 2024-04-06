package ch.cern.todo.core.application.query;
import ch.cern.todo.core.application.port.TaskReadStore;
import ch.cern.todo.core.application.query.dto.TaskProjection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class TaskQueryHandler {

    private final TaskReadStore taskReadStore;

    public Optional<TaskProjection> handleGetTaskCategory(final Long id) {
        return taskReadStore.getTask(id);
    }


}
