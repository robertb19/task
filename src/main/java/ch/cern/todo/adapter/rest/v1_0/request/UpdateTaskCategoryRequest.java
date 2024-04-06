package ch.cern.todo.adapter.rest.v1_0.request;

import jakarta.validation.constraints.Size;

public record UpdateTaskCategoryRequest(@Size(max = 100, message = "Name must be smaller than 100") String name,
                                        @Size(max = 500, message = "Description must be smaller than 500") String description) {}
