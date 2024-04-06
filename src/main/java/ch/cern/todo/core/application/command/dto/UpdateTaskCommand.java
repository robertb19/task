package ch.cern.todo.core.application.command.dto;

import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public record UpdateTaskCommand(
        @NotNull(message = "ID cannot be null") Long id,
        String name,
        String description,
        ZonedDateTime deadline,
        Long categoryId) {}
