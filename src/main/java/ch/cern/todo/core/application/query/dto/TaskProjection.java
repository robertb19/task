package ch.cern.todo.core.application.query.dto;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record TaskProjection(Long id, String name, String description, ZonedDateTime deadline, TaskCategoryProjection category) {}
