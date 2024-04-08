package ch.cern.todo.core.application.task.category;

import ch.cern.todo.core.application.task.category.command.TaskCategoryCommandHandler;
import ch.cern.todo.core.application.task.category.command.dto.AddTaskCategoryCommand;
import ch.cern.todo.core.application.task.category.command.dto.DeleteTaskCategoryCommand;
import ch.cern.todo.core.application.task.category.command.dto.UpdateTaskCategoryCommand;
import ch.cern.todo.core.application.task.category.query.TaskCategoryQueryHandler;
import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryProjection;
import ch.cern.todo.core.application.dto.CustomPage;
import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryFilters;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskCategoryService {

    private final TaskCategoryCommandHandler taskCategoryCommandHandler;

    private final TaskCategoryQueryHandler taskCategoryQueryHandler;

    public Long addTaskCategory(final AddTaskCategoryCommand addTaskCategoryCommand) {
        return taskCategoryCommandHandler.handleAddTaskCategory(addTaskCategoryCommand);
    }

    @Transactional
    public void updateTaskCategory(final UpdateTaskCategoryCommand updateTaskCategoryCommand) {
        taskCategoryCommandHandler.handleUpdateTaskCategory(updateTaskCategoryCommand);
    }

    public void deleteTaskCategory(final DeleteTaskCategoryCommand deleteTaskCategoryCommand) {
        taskCategoryCommandHandler.handleDeleteTaskCategory(deleteTaskCategoryCommand);
    }

    public Optional<TaskCategoryProjection> getTaskCategory(final Long id) {
        return taskCategoryQueryHandler.handleGetTaskCategory(id);
    }

    public CustomPage<TaskCategoryProjection> getTaskCategories(final TaskCategoryFilters taskCategoryFilters) {
        return taskCategoryQueryHandler.handleGetTaskCategories(taskCategoryFilters);
    }

}
