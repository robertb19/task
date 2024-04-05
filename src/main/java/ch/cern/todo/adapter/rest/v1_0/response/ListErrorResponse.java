package ch.cern.todo.adapter.rest.v1_0.response;

import java.util.Set;

public record ListErrorResponse(Set<String> messages) {}
