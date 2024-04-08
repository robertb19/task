package ch.cern.todo.core.application.task.category.command.dto;

import jakarta.validation.constraints.NotNull;

public record AddTaskCategoryCommand(@NotNull(message = "Name cannot be null") String name, String description) {}
