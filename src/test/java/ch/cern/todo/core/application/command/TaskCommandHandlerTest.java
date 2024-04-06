package ch.cern.todo.core.application.command;

import ch.cern.todo.core.application.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.port.TaskWriteStore;
import ch.cern.todo.core.domain.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

}
