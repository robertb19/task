package ch.cern.todo.core.application.task.command;

import ch.cern.todo.core.application.task.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.task.command.dto.DeleteTaskCommand;
import ch.cern.todo.core.application.task.command.dto.UpdateTaskCommand;
import ch.cern.todo.core.application.task.command.mapper.TaskCommandMapper;
import ch.cern.todo.core.application.task.exception.TaskNotFoundException;
import ch.cern.todo.core.application.task.port.TaskWriteStore;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TaskCommandHandler {

    private final TaskWriteStore taskWriteStore;

    public Long handleAddTask(final AddTaskCommand addTaskCommand) {
        return taskWriteStore.save(TaskCommandMapper.toTask(addTaskCommand))
                .getId();
    }

    public void handleDeleteTask(final DeleteTaskCommand deleteTaskCommand) {
        taskWriteStore.delete(deleteTaskCommand.id());
    }

    public void handleUpdateTask(final UpdateTaskCommand updateTaskCommand) {
        if(taskWriteStore.update(TaskCommandMapper.toTask(updateTaskCommand)).isEmpty()) {
            throw new TaskNotFoundException();
        }
    }

}
