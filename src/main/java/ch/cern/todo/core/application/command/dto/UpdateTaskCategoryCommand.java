package ch.cern.todo.core.application.command.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateTaskCategoryCommand(@NotNull(message = "ID cannot be null") Long id, String name, String description) {}
