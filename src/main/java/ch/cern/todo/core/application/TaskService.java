package ch.cern.todo.core.application;

import ch.cern.todo.core.application.command.TaskCommandHandler;
import ch.cern.todo.core.application.command.dto.AddTaskCommand;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TaskService {

    private final TaskCommandHandler taskCommandHandler;

    @Transactional
    public Long addTask(final AddTaskCommand addTaskCommand) {
        return taskCommandHandler.handleAddTask(addTaskCommand);
    }

}
