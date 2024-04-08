package ch.cern.todo.core.application.task.category.port;

import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryProjection;
import ch.cern.todo.core.application.dto.CustomPage;
import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryFilters;

import java.util.Optional;

public interface TaskCategoryReadStore {

    CustomPage<TaskCategoryProjection> getTaskCategories(TaskCategoryFilters taskCategoryFilters);

    Optional<TaskCategoryProjection> getTaskCategory(Long id);

}
