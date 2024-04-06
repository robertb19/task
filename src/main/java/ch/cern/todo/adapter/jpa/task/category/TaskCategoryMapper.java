package ch.cern.todo.adapter.jpa.task.category;

import ch.cern.todo.core.application.query.dto.TaskCategoryProjection;
import ch.cern.todo.core.domain.TaskCategory;

public final class TaskCategoryMapper {

    private TaskCategoryMapper(){}

    static TaskCategoryEntity toTaskCategoryEntity(final TaskCategory taskCategory) {
        return new TaskCategoryEntity(taskCategory.getId(), taskCategory.getName(), taskCategory.getDescription());
    }

    static TaskCategory toTaskCategory(final TaskCategoryEntity taskCategoryEntity) {
        return new TaskCategory(taskCategoryEntity.getId(), taskCategoryEntity.getName(), taskCategoryEntity.getDescription());
    }

    public static TaskCategoryProjection toTaskCategoryProjection(final TaskCategoryEntity taskCategoryEntity) {
        return new TaskCategoryProjection(taskCategoryEntity.getId(), taskCategoryEntity.getName(), taskCategoryEntity.getDescription());
    }

}
