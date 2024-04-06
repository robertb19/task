package ch.cern.todo.adapter.jpa.task.category;

import ch.cern.todo.core.application.exception.DuplicateTaskCategoryException;
import ch.cern.todo.core.application.exception.TaskCategoryException;
import ch.cern.todo.core.application.port.TaskCategoryReadStore;
import ch.cern.todo.core.application.port.TaskCategoryWriteStore;
import ch.cern.todo.core.application.query.TaskCategoryProjection;
import ch.cern.todo.core.application.query.dto.CustomPage;
import ch.cern.todo.core.application.query.dto.SortDirection;
import ch.cern.todo.core.application.query.dto.TaskCategoryFilters;
import ch.cern.todo.core.domain.TaskCategory;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
@Slf4j
public class TaskCategoryRepository implements TaskCategoryWriteStore, TaskCategoryReadStore {

    private final TaskCategoryRepositoryJpa taskCategoryRepositoryJpa;

    @Override
    public TaskCategory save(final TaskCategory taskCategory) {
        try {
            final TaskCategoryEntity taskCategoryEntity = taskCategoryRepositoryJpa
                    .save(TaskCategoryMapper.toTaskCategoryEntity(taskCategory));
            return TaskCategoryMapper.toTaskCategory(taskCategoryEntity);
        } catch (DataIntegrityViolationException e) {
            log.debug(e.getMessage());
            if(e.getCause() instanceof ConstraintViolationException) {
                throw new DuplicateTaskCategoryException("Task category already exists");
            } else {
                throw new TaskCategoryException("Unknown task category error");
            }
        }
    }

    @Override
    public Optional<TaskCategory> get(final Long id) {
        return taskCategoryRepositoryJpa.findById(id)
                .map(TaskCategoryMapper::toTaskCategory);
    }

    //I could update in one database run, however I believe this is cleaner for now and we do not have any performance issues
    @Override
    public Optional<TaskCategory> update(final TaskCategory taskCategory) {
        try {
            final Optional<TaskCategoryEntity> taskCategoryEntity = taskCategoryRepositoryJpa.findById(taskCategory.getId());
            if(taskCategoryEntity.isPresent()) {
                final Optional<TaskCategory> updatedEntity = updateEntity(taskCategoryEntity.get(), taskCategory);
                taskCategoryRepositoryJpa.flush();
                return updatedEntity;
            } else {
                return Optional.empty();
            }
        } catch (DataIntegrityViolationException e) {
            log.debug(e.getMessage());
            if(e.getCause() instanceof ConstraintViolationException) {
                throw new DuplicateTaskCategoryException("Task category already exists");
            } else {
                throw new TaskCategoryException("Unknown task category error");
            }
        }
    }

    @Override
    public void deleteTaskCategory(final Long id) {
        taskCategoryRepositoryJpa.deleteById(id);
    }

    @Override
    public CustomPage<TaskCategoryProjection> getTaskCategories(final TaskCategoryFilters taskCategoryFilters) {
        final Sort sort = getSorting(taskCategoryFilters.sortDirection());

        final Page<TaskCategoryEntity> taskCategoryEntityPage =  taskCategoryRepositoryJpa.findAll(
                PageRequest.of(taskCategoryFilters.pageNumber(), taskCategoryFilters.pageSize(), sort));

        return new CustomPage<>(taskCategoryEntityPage.stream()
                .map(TaskCategoryMapper::toTaskCategoryProjection)
                .toList(), taskCategoryEntityPage.getTotalElements(), taskCategoryEntityPage.getTotalPages());
    }

    @Override
    public CustomPage<TaskCategoryProjection> getTaskCategoriesByName(final TaskCategoryFilters taskCategoryFilters) {
        final Sort sort = getSorting(taskCategoryFilters.sortDirection());

        final Page<TaskCategoryEntity> taskCategoryEntityPage =  taskCategoryRepositoryJpa.findAllByName(taskCategoryFilters.name(),
                PageRequest.of(taskCategoryFilters.pageNumber(), taskCategoryFilters.pageSize(), sort));

        return new CustomPage<>(taskCategoryEntityPage.stream()
                .map(TaskCategoryMapper::toTaskCategoryProjection)
                .toList(), taskCategoryEntityPage.getTotalElements(), taskCategoryEntityPage.getTotalPages());
    }

    @Override
    public Optional<TaskCategoryProjection> getTaskCategory(final Long id) {
        return taskCategoryRepositoryJpa.findById(id)
                .map(TaskCategoryMapper::toTaskCategoryProjection);
    }

    private Optional<TaskCategory> updateEntity(final TaskCategoryEntity entity, final TaskCategory taskCategory) {
            if(StringUtils.isNotEmpty(taskCategory.getName())) {
                entity.setName(taskCategory.getName());
            }

            if(StringUtils.isNotBlank(taskCategory.getDescription())) {
                entity.setDescription(taskCategory.getDescription());
            }

            taskCategoryRepositoryJpa.flush();
            return Optional.of(TaskCategoryMapper.toTaskCategory(entity));
    }

    private Sort getSorting(SortDirection sortDirection) {
        return sortDirection == SortDirection.ASC ?
                Sort.by("id").ascending() :
                Sort.by("id").descending();
    }

}
