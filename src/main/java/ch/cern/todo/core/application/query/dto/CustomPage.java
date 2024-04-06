package ch.cern.todo.core.application.query.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@EqualsAndHashCode
@AllArgsConstructor
public final class CustomPage<T> {

    private final List<T> elements;

    @Getter
    private final long totalElements;

    @Getter
    private final int totalPages;

    public List<T> getElements() {
        return Collections.unmodifiableList(elements);
    }
}
