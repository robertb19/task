package ch.cern.todo.core;

import ch.cern.todo.core.application.TaskService;
import ch.cern.todo.core.application.command.TaskCommandHandler;
import ch.cern.todo.core.application.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.command.dto.DeleteTaskCommand;
import ch.cern.todo.core.application.command.dto.UpdateTaskCommand;
import ch.cern.todo.core.application.query.TaskQueryHandler;
import ch.cern.todo.core.application.query.dto.TaskCategoryProjection;
import ch.cern.todo.core.application.query.dto.TaskProjection;
import ch.cern.todo.core.domain.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskCommandHandler taskCommandHandler;

    @Mock
    private TaskQueryHandler taskQueryHandler;

    @InjectMocks
    private TaskService taskService;

    @Test
    void givenAddTaskCommand_whenAddTask_returnId() {
        //given
        final Instant now = Instant.now();
        final AddTaskCommand addTaskCommand = new AddTaskCommand(
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                1L);

        //when
        when(taskCommandHandler.handleAddTask(addTaskCommand)).thenReturn(1L);
        final Long result = taskService.addTask(addTaskCommand);

        //then
        assertEquals(1L, result);
    }

    @Test
    void givenDeleteTaskCommand_whenDeleteTask_executeSuccessfully() {
        //given
        final DeleteTaskCommand deleteTaskCommand = new DeleteTaskCommand(1L);

        //when
        taskService.deleteTask(deleteTaskCommand);

        //then
        verify(taskCommandHandler).handleDeleteTask(deleteTaskCommand);
    }

    @Test
    void givenUpdateTaskCommand_whenUpdateTask_executeSuccessfully() {
        //given
        final Instant now = Instant.now();
        final UpdateTaskCommand updateTaskCommand = new UpdateTaskCommand(
                1L,
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                1L);

        //when
        taskService.updateTask(updateTaskCommand);

        //then
        verify(taskCommandHandler).handleUpdateTask(updateTaskCommand);
    }

    @Test
    void givenId_whenGetTask_returnTaskProjectionOptional() {
        //given
        final Instant now = Instant.now();
        final TaskCategoryProjection taskCategoryProjection = new TaskCategoryProjection(1L, "name", "description");
        final TaskProjection taskProjection = new TaskProjection(
                1L,
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                taskCategoryProjection);

        //when
        when(taskQueryHandler.handleGetTaskCategory(taskProjection.id())).thenReturn(Optional.of(taskProjection));
        final Optional<TaskProjection> result = taskService.getTask(taskProjection.id());

        //then
        assertTrue(result.isPresent());
        assertEquals(taskProjection, result.get());
    }

}
