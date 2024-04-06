package ch.cern.todo.adapter.rest.v1_0.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CommonPage<T> {

    private int pageNumber;

    private int pageSize;

    private long totalElements;

    private int totalPages;

    private List<T> elements;

}
