package ch.cern.todo.adapter.rest.v1_0.request;

import java.util.Set;

public record ListErrorResponse(Set<String> messages) {}
