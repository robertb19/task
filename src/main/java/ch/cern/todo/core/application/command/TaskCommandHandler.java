package ch.cern.todo.core.application.command;

import ch.cern.todo.core.application.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.command.mapper.TaskCommandMapper;
import ch.cern.todo.core.application.port.TaskWriteStore;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TaskCommandHandler {

    private final TaskWriteStore taskWriteStore;

    public Long handleAddTask(final AddTaskCommand addTaskCommand) {
        return taskWriteStore.save(TaskCommandMapper.toTaskCategory(addTaskCommand))
                .getId();
    }

}
