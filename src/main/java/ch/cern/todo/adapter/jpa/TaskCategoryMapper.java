package ch.cern.todo.adapter.jpa;

import ch.cern.todo.core.application.query.TaskCategoryProjection;
import ch.cern.todo.core.domain.TaskCategory;

final class TaskCategoryMapper {

    private TaskCategoryMapper(){}

    static TaskCategoryEntity toTaskCategoryEntity(final TaskCategory taskCategory) {
        return new TaskCategoryEntity(taskCategory.getId(), taskCategory.getName(), taskCategory.getDescription());
    }

    static TaskCategory toTaskCategory(final TaskCategoryEntity taskCategoryEntity) {
        return new TaskCategory(taskCategoryEntity.getId(), taskCategoryEntity.getName(), taskCategoryEntity.getDescription());
    }

    static TaskCategoryProjection toTaskCategoryProjection(final TaskCategoryEntity taskCategoryEntity) {
        return new TaskCategoryProjection(taskCategoryEntity.getId(), taskCategoryEntity.getName(), taskCategoryEntity.getDescription());
    }

}
