package ch.cern.todo.core.application.task.query.dto;

import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryProjection;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record TaskProjection(Long id, String name, String description, ZonedDateTime deadline, TaskCategoryProjection category) {}
