package ch.cern.todo.adapter.rest.v1_0.task.response;

import ch.cern.todo.core.application.query.dto.TaskCategoryProjection;
import ch.cern.todo.core.application.query.dto.TaskProjection;

import java.time.ZonedDateTime;

public record GetTaskResponse(Long id, String name, String description, ZonedDateTime deadline, TaskCategoryProjection category) {

    public static GetTaskResponse from(final TaskProjection taskProjection) {
        return new GetTaskResponse(taskProjection.id(),
                taskProjection.name(),
                taskProjection.description(),
                taskProjection.deadline(),
                taskProjection.category());
    }

}
