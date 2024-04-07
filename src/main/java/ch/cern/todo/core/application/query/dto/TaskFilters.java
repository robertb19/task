package ch.cern.todo.core.application.query.dto;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record TaskFilters(String name,
                          Long categoryId,
                          ZonedDateTime deadline,
                          DeadlineMode deadlineMode,
                          int pageNumber,
                          int pageSize,
                          SortDirection sortDirection) {}
