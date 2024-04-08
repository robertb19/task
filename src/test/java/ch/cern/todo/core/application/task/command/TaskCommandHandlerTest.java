package ch.cern.todo.core.application.task.command;

import ch.cern.todo.core.application.task.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.task.command.dto.DeleteTaskCommand;
import ch.cern.todo.core.application.task.command.dto.UpdateTaskCommand;
import ch.cern.todo.core.application.task.exception.TaskNotFoundException;
import ch.cern.todo.core.application.task.port.TaskWriteStore;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskCommandHandlerTest {

    @Mock
    private TaskWriteStore taskWriteStore;

    @InjectMocks
    private TaskCommandHandler taskCommandHandler;

    @Test
    void givenAddTaskCommand_whenHandleAddTask_returnAddedId() {
        //given
        final Instant now = Instant.now();
        final AddTaskCommand addTaskCommand = new AddTaskCommand(
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                1L);
        final Task task = new Task(addTaskCommand.name(), addTaskCommand.description(), addTaskCommand.deadline(), addTaskCommand.categoryId());
        final Task persistedTask = new Task(1L, addTaskCommand.name(), addTaskCommand.description(), addTaskCommand.deadline(), addTaskCommand.categoryId());

        //when
        when(taskWriteStore.save(task)).thenReturn(persistedTask);
        final Long result = taskCommandHandler.handleAddTask(addTaskCommand);

        //then
        assertEquals(1L, result);
    }

    @Test
    void givenDeleteTaskCommand_whenHandleDeleteTask_executeSuccessfully() {
        //given
        final DeleteTaskCommand deleteTaskCommand = new DeleteTaskCommand(1L);

        //when
        taskCommandHandler.handleDeleteTask(deleteTaskCommand);

        //then
        verify(taskWriteStore).delete(deleteTaskCommand.id());
    }

    @Test
    void givenUpdateTaskCommand_whenHandleUpdateTask_executeSuccessfully() {
        //given
        final Instant now = Instant.now();
        final Task task = new Task(
                1L,
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                1L);
        final UpdateTaskCommand updateTaskCommand = new UpdateTaskCommand(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getDeadline(),
                task.getTaskCategoryId()
        );

        //when
        when(taskWriteStore.update(task)).thenReturn(Optional.of(task));
        taskCommandHandler.handleUpdateTask(updateTaskCommand);

        //then
        verify(taskWriteStore).update(task);
    }

    @Test
    void givenUpdateTaskCommandWithInexistentId_whenHandleUpdateTask_throwTaskNotFoundException() {
        //given
        final Instant now = Instant.now();
        final Task task = new Task(
                1L,
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                1L);
        final UpdateTaskCommand updateTaskCommand = new UpdateTaskCommand(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getDeadline(),
                task.getTaskCategoryId()
        );

        //when and then
        when(taskWriteStore.update(task)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskCommandHandler.handleUpdateTask(updateTaskCommand));
    }

}
