package ch.cern.todo.core.application.task.category.command;

import ch.cern.todo.core.application.task.category.command.dto.AddTaskCategoryCommand;
import ch.cern.todo.core.application.task.category.command.dto.DeleteTaskCategoryCommand;
import ch.cern.todo.core.application.task.category.command.dto.UpdateTaskCategoryCommand;
import ch.cern.todo.core.application.task.category.command.mapper.TaskCategoryCommandMapper;
import ch.cern.todo.core.application.task.category.exception.TaskCategoryNotFoundException;
import ch.cern.todo.core.application.task.category.port.TaskCategoryWriteStore;
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
        taskCategoryWriteStore.delete(deleteTaskCategoryCommand.id());
    }

}
