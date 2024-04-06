package ch.cern.todo.adapter.rest.v1_0.task.category.response;

import ch.cern.todo.core.application.query.dto.TaskCategoryProjection;

public record GetTaskCategoryResponse(Long id, String name, String description) {

    public static GetTaskCategoryResponse from(TaskCategoryProjection taskCategoryProjection) {
        return new GetTaskCategoryResponse(taskCategoryProjection.id(), taskCategoryProjection.name(), taskCategoryProjection.description());
    }

}
