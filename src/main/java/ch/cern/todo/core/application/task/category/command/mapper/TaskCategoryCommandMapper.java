package ch.cern.todo.core.application.task.category.command.mapper;

import ch.cern.todo.core.application.task.category.command.dto.AddTaskCategoryCommand;
import ch.cern.todo.core.application.task.category.command.dto.UpdateTaskCategoryCommand;
import ch.cern.todo.core.domain.TaskCategory;

public final class TaskCategoryCommandMapper {

    private TaskCategoryCommandMapper() {}

    public static TaskCategory toTaskCategory(final AddTaskCategoryCommand addTaskCategoryCommand) {
        return new TaskCategory(addTaskCategoryCommand.name(), addTaskCategoryCommand.description());
    }

    public static TaskCategory toTaskCategory(final UpdateTaskCategoryCommand updateTaskCategoryCommand) {
        return new TaskCategory(updateTaskCategoryCommand.id(), updateTaskCategoryCommand.name(), updateTaskCategoryCommand.description());
    }

}
