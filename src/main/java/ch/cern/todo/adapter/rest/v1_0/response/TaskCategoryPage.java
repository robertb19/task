package ch.cern.todo.adapter.rest.v1_0.response;

import java.util.List;

public record TaskCategoryPage(int pageNumber, int pageSize, long totalElements, int totalPages, List<GetTaskCategoryResponse> categories) {}
