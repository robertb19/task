package ch.cern.todo.core.application.task.category.query;

import ch.cern.todo.core.application.task.category.port.TaskCategoryReadStore;
import ch.cern.todo.core.application.dto.CustomPage;
import ch.cern.todo.core.application.dto.SortDirection;
import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryFilters;
import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskCategoryQueryHandlerTest {

    @Mock
    private TaskCategoryReadStore taskCategoryReadStore;

    @InjectMocks
    private TaskCategoryQueryHandler taskCategoryQueryHandler;

    @Test
    void givenId_whenHandleGetTaskCategory_returnTaskCategoryProjectionOptional() {
        //given
        final Long id = 1L;
        final TaskCategoryProjection taskCategoryProjection = new TaskCategoryProjection(1L, "name1", "description");

        //when
        when(taskCategoryReadStore.getTaskCategory(id)).thenReturn(Optional.of(taskCategoryProjection));
        final Optional<TaskCategoryProjection> result = taskCategoryQueryHandler.handleGetTaskCategory(id);

        //then
        assertTrue(result.isPresent());
        assertEquals(taskCategoryProjection, result.get());
    }

    @Test
    void givenTaskCategoryFilters_whenHandleGetTaskCategories_returnTaskCategoryProjectionPage() {
        //given
        final TaskCategoryFilters taskCategoryFilters = new TaskCategoryFilters(null, 0, 10, SortDirection.DESC);
        final CustomPage<TaskCategoryProjection> projectionPage = new CustomPage<>(
                List.of(
                        new TaskCategoryProjection(1L, "name1", "description1"),
                        new TaskCategoryProjection(2L, "name2", "description2")
                ), 2, 1
        );

        //when
        when(taskCategoryReadStore.getTaskCategories(taskCategoryFilters)).thenReturn(projectionPage);
        final CustomPage<TaskCategoryProjection> result = taskCategoryQueryHandler.handleGetTaskCategories(taskCategoryFilters);

        //then
        assertEquals(projectionPage, result);
    }

}
