package ch.cern.todo.core.application.query;

import ch.cern.todo.core.application.port.TaskReadStore;
import ch.cern.todo.core.application.query.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskQueryHandlerTest {

    @Mock
    private TaskReadStore taskReadStore;

    @InjectMocks
    private TaskQueryHandler taskQueryHandler;

    @Test
    void givenId_whenHandleGetTask_returnTaskProjectionOptional() {
        //given
        final Long id = 1L;
        final Instant now = Instant.now();
        final TaskCategoryProjection taskCategoryProjection = new TaskCategoryProjection(1L, "name", "description");
        final TaskProjection taskProjection = new TaskProjection(
                1L,
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                taskCategoryProjection);

        //when
        when(taskReadStore.getTask(id)).thenReturn(Optional.of(taskProjection));
        final Optional<TaskProjection> result = taskQueryHandler.handleGetTaskCategory(id);

        //then
        assertTrue(result.isPresent());
        assertEquals(taskProjection, result.get());
    }

    @Test
    void givenTaskFilters_whenHandleGetTasks_returnTaskProjectionPage() {
        //given
        final Instant now = Instant.now();
        final ZonedDateTime zonedDateTime = now.atZone(ZoneId.of("UTC"));
        final TaskCategoryProjection taskCategoryProjection = new TaskCategoryProjection(1L, "name", "description");
        final TaskProjection taskProjection = new TaskProjection(
                1L,
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                taskCategoryProjection);
        final CustomPage<TaskProjection> expected = new CustomPage<>(List.of(taskProjection), 10, 2);
        final TaskFilters filters = new TaskFilters("name", 1L, zonedDateTime, DeadlineMode.AFTER, 0, 5, SortDirection.ASC);


        //when
        when(taskReadStore.getTasks(filters)).thenReturn(expected);
        final CustomPage<TaskProjection> result = taskQueryHandler.handleGetTasks(filters);

        //then
        assertEquals(expected, result);
    }

}
