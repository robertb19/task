package ch.cern.todo.core.application.task.category.query.dto;

import ch.cern.todo.core.application.dto.SortDirection;
import lombok.Builder;

@Builder
public record TaskCategoryFilters(String name, int pageNumber, int pageSize, SortDirection sortDirection) {}
