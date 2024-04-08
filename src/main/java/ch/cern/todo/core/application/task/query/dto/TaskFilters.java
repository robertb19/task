package ch.cern.todo.core.application.task.query.dto;

import ch.cern.todo.core.application.dto.SortDirection;
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
