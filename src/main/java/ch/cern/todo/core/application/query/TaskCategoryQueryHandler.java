package ch.cern.todo.core.application.query;

import ch.cern.todo.core.application.port.TaskCategoryReadStore;
import ch.cern.todo.core.application.query.dto.CustomPage;
import ch.cern.todo.core.application.query.dto.TaskCategoryFilters;
import ch.cern.todo.core.application.query.dto.TaskCategoryProjection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class TaskCategoryQueryHandler {

    private final TaskCategoryReadStore taskCategoryReadStore;

    public Optional<TaskCategoryProjection> handleGetTaskCategory(final Long id) {
        return taskCategoryReadStore.getTaskCategory(id);
    }

    public CustomPage<TaskCategoryProjection> handleGetTaskCategories(final TaskCategoryFilters taskCategoryFilters) {
        return taskCategoryReadStore.getTaskCategories(taskCategoryFilters);
    }

    public CustomPage<TaskCategoryProjection> handleGetTaskCategoriesByName(final TaskCategoryFilters taskCategoryFilters) {
        return taskCategoryReadStore.getTaskCategoriesByName(taskCategoryFilters);
    }

}
