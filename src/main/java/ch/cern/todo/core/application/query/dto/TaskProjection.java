package ch.cern.todo.core.application.query.dto;

import java.time.ZonedDateTime;

public record TaskProjection(Long id, String name, String description, ZonedDateTime deadline, TaskCategoryProjection category) {}
