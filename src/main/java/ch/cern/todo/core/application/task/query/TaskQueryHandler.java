package ch.cern.todo.core.application.task.query;

import ch.cern.todo.core.application.dto.CustomPage;
import ch.cern.todo.core.application.task.port.TaskReadStore;
import ch.cern.todo.core.application.task.query.dto.TaskFilters;
import ch.cern.todo.core.application.task.query.dto.TaskProjection;
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

    public CustomPage<TaskProjection> handleGetTasks(final TaskFilters taskFilters) {
        return taskReadStore.getTasks(taskFilters);
    }

}
