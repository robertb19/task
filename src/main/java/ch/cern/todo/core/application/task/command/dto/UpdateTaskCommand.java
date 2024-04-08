package ch.cern.todo.core.application.task.command.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record UpdateTaskCommand(
        @NotNull(message = "ID cannot be null") Long id,
        String name,
        String description,
        ZonedDateTime deadline,
        Long categoryId) {}
