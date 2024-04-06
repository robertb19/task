package ch.cern.todo.core;

import ch.cern.todo.core.application.TaskService;
import ch.cern.todo.core.application.command.TaskCommandHandler;
import ch.cern.todo.core.application.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.command.dto.DeleteTaskCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskCommandHandler taskCommandHandler;

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

}
