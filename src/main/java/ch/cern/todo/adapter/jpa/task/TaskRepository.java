package ch.cern.todo.adapter.jpa.task;

import ch.cern.todo.adapter.jpa.task.category.TaskCategoryEntity;
import ch.cern.todo.adapter.jpa.task.category.TaskCategoryRepositoryJpa;
import ch.cern.todo.core.application.exception.TaskCategoryNotFoundException;
import ch.cern.todo.core.application.port.TaskWriteStore;
import ch.cern.todo.core.domain.Task;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
@Slf4j
public class TaskRepository implements TaskWriteStore {

    private final TaskRepositoryJpa taskRepositoryJpa;

    private final TaskCategoryRepositoryJpa taskCategoryRepositoryJpa;

    /*
    again I could optimize this and have one DB run to insert and then handle foreign key exception, but for simplicity sake
    and readability over performance (and boost performance only with needed) I do it like that, it's more declarative and easier to manage
    rather than dealing eg. with a native query
    */
    @Override
    public Task save(final Task task) {
        final TaskCategoryEntity taskCategoryEntity = taskCategoryRepositoryJpa.findById(task.getTaskCategoryId())
                .orElseThrow(TaskCategoryNotFoundException::new);

        final TaskEntity taskEntity = TaskMapper.toTaskEntity(task);
        taskEntity.setTaskCategory(taskCategoryEntity);
        final TaskEntity persistedTask = taskRepositoryJpa.save(taskEntity);
        return TaskMapper.toTask(persistedTask);
    }

    @Override
    public void delete(Long id) {
        taskRepositoryJpa.deleteById(id);
    }

    @Override
    public Optional<Task> update(final Task task) {
        final Optional<TaskEntity> taskEntity = taskRepositoryJpa.findById(task.getId());
        if (taskEntity.isPresent()) {
            final Optional<Task> updatedEntity = updateEntity(taskEntity.get(), findForUpdate(task), task);
            taskCategoryRepositoryJpa.flush();
            return updatedEntity;
        } else {
            return Optional.empty();
        }
    }

    private Optional<TaskCategoryEntity> findForUpdate(final Task task) {
        return task.getTaskCategoryId() != null ? Optional.of(taskCategoryRepositoryJpa.findById(task.getTaskCategoryId())
                .orElseThrow(TaskCategoryNotFoundException::new)) :
                Optional.empty();
    }

    private Optional<Task> updateEntity(final TaskEntity entity, final Optional<TaskCategoryEntity> taskCategoryEntity, final Task task) {
        if(StringUtils.isNotBlank(task.getName())) {
            entity.setName(task.getName());
        }

        if(task.getDescription() != null) {
            entity.setDescription(task.getDescription());
        }

        if(task.getDeadline() != null) {
            entity.setDeadline(task.getDeadline());
        }

        taskCategoryEntity.ifPresent(entity::setTaskCategory);
        return Optional.of(TaskMapper.toTask(entity));
    }

}
