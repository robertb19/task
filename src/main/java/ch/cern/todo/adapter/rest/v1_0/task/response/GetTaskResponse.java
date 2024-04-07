package ch.cern.todo.adapter.rest.v1_0.task.response;

import ch.cern.todo.core.application.query.dto.TaskCategoryProjection;
import ch.cern.todo.core.application.query.dto.TaskProjection;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;

public record GetTaskResponse(Long id,
                              String name,
                              String description,
                              @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ") ZonedDateTime deadline,
                              TaskCategoryProjection category) {

    public static GetTaskResponse from(final TaskProjection taskProjection) {
        return new GetTaskResponse(taskProjection.id(),
                taskProjection.name(),
                taskProjection.description(),
                taskProjection.deadline(),
                taskProjection.category());
    }

}
