package ch.cern.todo.core.application.query.dto;

import lombok.Builder;

@Builder
public record TaskCategoryFilters(String name, int pageNumber, int pageSize, SortDirection sortDirection) {}
