package ch.cern.todo.adapter.jpa;

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
import org.springframework.dao.DataAccessException;
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
            log.error(e.getMessage());
            throw new DuplicateTaskCategoryException("Task category already exists");
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new TaskCategoryException("Unknown task category error");
        }
    }

    @Override
    public Optional<TaskCategory> get(final Long id) {
        return taskCategoryRepositoryJpa.findById(id)
                .map(TaskCategoryMapper::toTaskCategory);
    }

    @Override
    public Optional<TaskCategory> update(final TaskCategory taskCategory) {
        final Optional<TaskCategoryEntity> taskCategoryEntity = taskCategoryRepositoryJpa.findById(taskCategory.getId());
        if(taskCategoryEntity.isPresent()) {
            final TaskCategoryEntity entity = taskCategoryEntity.get();

            if(StringUtils.isNotEmpty(taskCategory.getName())) {
                entity.setName(taskCategory.getName());
            }

            if(StringUtils.isNotBlank(taskCategory.getDescription())) {
                entity.setDescription(taskCategory.getDescription());
            }

            return Optional.of(TaskCategoryMapper.toTaskCategory(entity));
        } else {
            return Optional.empty();
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

    private Sort getSorting(SortDirection sortDirection) {
        return sortDirection == SortDirection.ASC ?
                Sort.by("id").ascending() :
                Sort.by("id").descending();
    }

}
