package ch.cern.todo.core.application.command.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record AddTaskCommand(@NotNull(message = "Name cannot be null") String name,
                             String description,
                             @NotNull(message = "Deadline cannot be null") ZonedDateTime deadline,
                             @NotNull(message = "Category ID cannot be null") Long categoryId) {}
