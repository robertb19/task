package ch.cern.todo.core.application;

import ch.cern.todo.core.application.command.TaskCommandHandler;
import ch.cern.todo.core.application.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.command.dto.DeleteTaskCommand;
import ch.cern.todo.core.application.command.dto.UpdateTaskCommand;
import ch.cern.todo.core.application.query.TaskQueryHandler;
import ch.cern.todo.core.application.query.dto.TaskProjection;
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

    public Optional<TaskProjection> getTask(Long id) {
        return taskQueryHandler.handleGetTaskCategory(id);
    }

}
