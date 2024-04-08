package ch.cern.todo.core.application.task.command.mapper;

import ch.cern.todo.core.application.task.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.task.command.dto.UpdateTaskCommand;
import ch.cern.todo.core.domain.Task;

public final class TaskCommandMapper {

    private TaskCommandMapper() {}

    public static Task toTask(final AddTaskCommand addTaskCommand) {
        return new Task(addTaskCommand.name(), addTaskCommand.description(), addTaskCommand.deadline(), addTaskCommand.categoryId());
    }

    public static Task toTask(final UpdateTaskCommand updateTaskCommand) {
        return new Task(updateTaskCommand.id(), updateTaskCommand.name(), updateTaskCommand.description(), updateTaskCommand.deadline(), updateTaskCommand.categoryId());
    }

}
