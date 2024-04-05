package ch.cern.todo.core.application.command;

import ch.cern.todo.core.application.command.dto.AddTaskCategoryCommand;
import ch.cern.todo.core.application.command.dto.DeleteTaskCategoryCommand;
import ch.cern.todo.core.application.command.dto.UpdateTaskCategoryCommand;
import ch.cern.todo.core.application.command.mapper.TaskCategoryCommandMapper;
import ch.cern.todo.core.application.exception.TaskCategoryNotFoundException;
import ch.cern.todo.core.application.port.TaskCategoryWriteStore;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TaskCategoryCommandHandler {

    private final TaskCategoryWriteStore taskCategoryWriteStore;

    public Long handleAddTaskCategory(final AddTaskCategoryCommand addTaskCategoryCommand) {
        return taskCategoryWriteStore.save(TaskCategoryCommandMapper.toTaskCategory(addTaskCategoryCommand))
                .getId();
    }

    public void handleUpdateTaskCategory(final UpdateTaskCategoryCommand updateTaskCategoryCommand) {
        if(taskCategoryWriteStore.update(TaskCategoryCommandMapper.toTaskCategory(updateTaskCategoryCommand)).isEmpty()) {
            throw new TaskCategoryNotFoundException();
        }
    }

    public void handleDeleteTaskCategory(final DeleteTaskCategoryCommand deleteTaskCategoryCommand) {
        taskCategoryWriteStore.deleteTaskCategory(deleteTaskCategoryCommand.id());
    }

}
