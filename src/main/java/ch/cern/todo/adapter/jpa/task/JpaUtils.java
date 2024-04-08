package ch.cern.todo.adapter.jpa.task;

import ch.cern.todo.core.application.dto.SortDirection;
import jakarta.persistence.criteria.*;

public final class JpaUtils {

    private JpaUtils() {}

    public static <T> Order getSortingById(final CriteriaBuilder criteriaBuilder, final Root<T> root, final SortDirection sortDirection) {
        return sortDirection == SortDirection.ASC ?
                criteriaBuilder.asc(root.get("id")) :
                criteriaBuilder.desc(root.get("id"));
    }

    public static int getTotalPages(final int pageSize, final Long count) {
        if(pageSize == 0) {
            return 0;
        } else {
            return count % pageSize == 0 ? (int) (count / pageSize) : (int) (count / pageSize) + 1;
        }
    }

}
