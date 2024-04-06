package ch.cern.todo.adapter.jpa.task;

import ch.cern.todo.adapter.jpa.task.category.TaskCategoryMapper;
import ch.cern.todo.core.application.query.dto.TaskProjection;
import ch.cern.todo.core.domain.Task;

final class TaskMapper {

    private TaskMapper(){}

    static TaskEntity toTaskEntity(final Task task) {
        return new TaskEntity(task.getName(), task.getDescription(), task.getDeadline());
    }

    static Task toTask(final TaskEntity taskEntity) {
        return new Task(taskEntity.getId(), taskEntity.getName(), taskEntity.getDescription(), taskEntity.getDeadline(), taskEntity.getTaskCategory().getId());
    }
    static TaskProjection toTaskProjection(final TaskEntity taskEntity) {
        return new TaskProjection(taskEntity.getId(),
                taskEntity.getName(),
                taskEntity.getDescription(),
                taskEntity.getDeadline(),
                TaskCategoryMapper.toTaskCategoryProjection(taskEntity.getTaskCategory()));
    }


}
