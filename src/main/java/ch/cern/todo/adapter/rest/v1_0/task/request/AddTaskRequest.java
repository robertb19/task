package ch.cern.todo.adapter.rest.v1_0.task.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record AddTaskRequest(@NotNull(message = "Name must be specified in request") @Size(max = 100, message = "Name must be smaller than 100") String name,
                             @Size(max = 500, message = "Description must be smaller than 500") String description,
                             @NotNull(message = "Deadline must be specified in request") @Future(message = "Deadline must be in the future") Instant deadline,
                             @NotNull(message = "Category ID must be specified in request") Long categoryId) {}
