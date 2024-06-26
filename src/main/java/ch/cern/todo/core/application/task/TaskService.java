package ch.cern.todo.core.application.task;

import ch.cern.todo.core.application.dto.CustomPage;
import ch.cern.todo.core.application.task.command.TaskCommandHandler;
import ch.cern.todo.core.application.task.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.task.command.dto.DeleteTaskCommand;
import ch.cern.todo.core.application.task.command.dto.UpdateTaskCommand;
import ch.cern.todo.core.application.task.query.TaskQueryHandler;
import ch.cern.todo.core.application.task.query.dto.TaskFilters;
import ch.cern.todo.core.application.task.query.dto.TaskProjection;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskService {

    private final TaskCommandHandler taskCommandHandler;

    private final TaskQueryHandler taskQueryHandler;

    @Transactional
    public Long addTask(final AddTaskCommand addTaskCommand) {
        return taskCommandHandler.handleAddTask(addTaskCommand);
    }

    public void deleteTask(final DeleteTaskCommand deleteTaskCommand) {
        taskCommandHandler.handleDeleteTask(deleteTaskCommand);
    }

    @Transactional
    public void updateTask(final UpdateTaskCommand updateTaskCommand) {
        taskCommandHandler.handleUpdateTask(updateTaskCommand);
    }

    public Optional<TaskProjection> getTask(final Long id) {
        return taskQueryHandler.handleGetTaskCategory(id);
    }

    public CustomPage<TaskProjection> getTasks(final TaskFilters taskFilters) {
        return taskQueryHandler.handleGetTasks(taskFilters);
    }

}
