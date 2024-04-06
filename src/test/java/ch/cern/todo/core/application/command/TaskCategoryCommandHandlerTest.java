package ch.cern.todo.core.application.command;

import ch.cern.todo.core.application.command.dto.AddTaskCategoryCommand;
import ch.cern.todo.core.application.command.dto.DeleteTaskCategoryCommand;
import ch.cern.todo.core.application.command.dto.UpdateTaskCategoryCommand;
import ch.cern.todo.core.application.exception.TaskCategoryNotFoundException;
import ch.cern.todo.core.application.port.TaskCategoryWriteStore;
import ch.cern.todo.core.domain.TaskCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskCategoryCommandHandlerTest {

    @Mock
    private TaskCategoryWriteStore taskCategoryWriteStore;

    @InjectMocks
    private TaskCategoryCommandHandler taskCategoryCommandHandler;

    @Test
    void givenAddTaskCategoryCommand_whenHandleAddTaskCategory_returnAddedId() {
        //given
        final AddTaskCategoryCommand addTaskCategoryCommand = new AddTaskCategoryCommand("name", "description");
        final TaskCategory taskCategory = new TaskCategory(addTaskCategoryCommand.name(), addTaskCategoryCommand.description());
        final TaskCategory persistedTaskCategory = new TaskCategory(1L, addTaskCategoryCommand.name(), addTaskCategoryCommand.description());

        //when
        when(taskCategoryWriteStore.save(taskCategory)).thenReturn(persistedTaskCategory);
        final Long result = taskCategoryCommandHandler.handleAddTaskCategory(addTaskCategoryCommand);

        //then
        assertEquals(1L, result);
    }

    @Test
    void givenUpdateTaskCategoryCommand_whenHandleAddTaskCategory_executeSuccessfully() {
        //given
        final UpdateTaskCategoryCommand updateTaskCategoryCommand = new UpdateTaskCategoryCommand(1L, "name", "description");
        final TaskCategory taskCategory = new TaskCategory(updateTaskCategoryCommand.name(), updateTaskCategoryCommand.description());
        final TaskCategory persistedTaskCategory = new TaskCategory(1L, updateTaskCategoryCommand.name(), updateTaskCategoryCommand.description());

        //when
        when(taskCategoryWriteStore.update(taskCategory)).thenReturn(Optional.of(persistedTaskCategory));
        taskCategoryCommandHandler.handleUpdateTaskCategory(updateTaskCategoryCommand);

        //
        verify(taskCategoryWriteStore).update(taskCategory);
    }

    @Test
    void givenUpdateTaskCategoryCommandWithNotExistentId_whenHandleAddTaskCategory_throwTaskCategoryNotFoundException() {
        //given
        final UpdateTaskCategoryCommand updateTaskCategoryCommand = new UpdateTaskCategoryCommand(1L, "name", "description");
        final TaskCategory taskCategory = new TaskCategory(updateTaskCategoryCommand.name(), updateTaskCategoryCommand.description());

        //when and then
        when(taskCategoryWriteStore.update(taskCategory)).thenReturn(Optional.empty());
        assertThrows(TaskCategoryNotFoundException.class, () -> taskCategoryCommandHandler.handleUpdateTaskCategory(updateTaskCategoryCommand));
    }

    @Test
    void givenUpdateTaskCategoryCommandWithNotExistentId_whenHandleDeleteTaskCategory_executeSuccessfully() {
        //given
        final DeleteTaskCategoryCommand deleteTaskCategoryCommand = new DeleteTaskCategoryCommand(1L);

        //when
        taskCategoryCommandHandler.handleDeleteTaskCategory(deleteTaskCategoryCommand);

        //then
        verify(taskCategoryWriteStore).delete(deleteTaskCategoryCommand.id());
    }

}
