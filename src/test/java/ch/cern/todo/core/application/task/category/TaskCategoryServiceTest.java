package ch.cern.todo.core.application.task.category;

import ch.cern.todo.core.application.task.category.command.TaskCategoryCommandHandler;
import ch.cern.todo.core.application.task.category.command.dto.AddTaskCategoryCommand;
import ch.cern.todo.core.application.task.category.command.dto.DeleteTaskCategoryCommand;
import ch.cern.todo.core.application.task.category.command.dto.UpdateTaskCategoryCommand;
import ch.cern.todo.core.application.task.category.query.TaskCategoryQueryHandler;
import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryProjection;
import ch.cern.todo.core.application.dto.CustomPage;
import ch.cern.todo.core.application.dto.SortDirection;
import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryFilters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskCategoryServiceTest {

    @Mock
    private TaskCategoryCommandHandler taskCategoryCommandHandler;

    @Mock
    private TaskCategoryQueryHandler taskCategoryQueryHandler;

    @InjectMocks
    private TaskCategoryService taskCategoryService;

    @Test
    void givenAddTaskCategoryCommand_whenAddTaskCategory_returnId() {
        //given
        final AddTaskCategoryCommand addTaskCategoryCommand = new AddTaskCategoryCommand("name", "description");

        //when
        when(taskCategoryCommandHandler.handleAddTaskCategory(addTaskCategoryCommand)).thenReturn(1L);
        final Long result = taskCategoryService.addTaskCategory(addTaskCategoryCommand);

        //then
        assertEquals(1L, result);
    }

    @Test
    void givenUpdateTaskCategoryCommand_whenUpdateTaskCategory_executeSuccessfully() {
        //given
        final UpdateTaskCategoryCommand updateTaskCategoryCommand = new UpdateTaskCategoryCommand(1L, "name", "description");

        //when
        taskCategoryService.updateTaskCategory(updateTaskCategoryCommand);

        //then
        verify(taskCategoryCommandHandler).handleUpdateTaskCategory(updateTaskCategoryCommand);
    }

    @Test
    void givenDeleteTaskCategoryCommand_whenDeleteTaskCategory_executeSuccessfully() {
        //given
        final DeleteTaskCategoryCommand deleteTaskCategoryCommand = new DeleteTaskCategoryCommand(1L);

        //when
        taskCategoryService.deleteTaskCategory(deleteTaskCategoryCommand);

        //then
        verify(taskCategoryCommandHandler).handleDeleteTaskCategory(deleteTaskCategoryCommand);
    }

    @Test
    void givenId_whenGetTaskCategory_returnTaskCategoryProjectionOptional() {
        //given
        final Long id = 1L;
        final TaskCategoryProjection taskCategoryProjection = new TaskCategoryProjection(1L, "name1", "description");

        //when
        when(taskCategoryQueryHandler.handleGetTaskCategory(id)).thenReturn(Optional.of(taskCategoryProjection));
        final Optional<TaskCategoryProjection> result = taskCategoryService.getTaskCategory(id);

        //then
        assertTrue(result.isPresent());
        assertEquals(taskCategoryProjection, result.get());
    }

    @Test
    void givenId_whenGetTaskCategories_returnTaskCategoryProjectionPage() {
        //given
        final TaskCategoryFilters taskCategoryFilters = new TaskCategoryFilters(null, 0, 10, SortDirection.DESC);
        final CustomPage<TaskCategoryProjection> projectionPage = new CustomPage<>(
                List.of(
                        new TaskCategoryProjection(1L, "name1", "description1"),
                        new TaskCategoryProjection(2L, "name2", "description2")
                ), 2, 1
        );

        //when
        when(taskCategoryQueryHandler.handleGetTaskCategories(taskCategoryFilters)).thenReturn(projectionPage);
        final CustomPage<TaskCategoryProjection> result = taskCategoryService.getTaskCategories(taskCategoryFilters);

        //then
        assertEquals(projectionPage, result);
    }

}
