package ch.cern.todo.core.application.port;

import ch.cern.todo.core.application.query.dto.TaskCategoryProjection;
import ch.cern.todo.core.application.query.dto.CustomPage;
import ch.cern.todo.core.application.query.dto.TaskCategoryFilters;

import java.util.Optional;

public interface TaskCategoryReadStore {

    CustomPage<TaskCategoryProjection> getTaskCategories(TaskCategoryFilters taskCategoryFilters);

    CustomPage<TaskCategoryProjection> getTaskCategoriesByName(TaskCategoryFilters taskCategoryFilters);

    Optional<TaskCategoryProjection> getTaskCategory(Long id);

}
