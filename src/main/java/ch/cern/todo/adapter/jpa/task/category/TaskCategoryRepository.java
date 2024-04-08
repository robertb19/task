package ch.cern.todo.adapter.jpa.task.category;

import ch.cern.todo.core.application.dto.CustomPage;
import ch.cern.todo.core.application.task.category.exception.DuplicateTaskCategoryException;
import ch.cern.todo.core.application.task.category.exception.TaskCategoryException;
import ch.cern.todo.core.application.task.category.exception.TaskRecordsMappedException;
import ch.cern.todo.core.application.task.category.port.TaskCategoryReadStore;
import ch.cern.todo.core.application.task.category.port.TaskCategoryWriteStore;
import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryFilters;
import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryProjection;
import ch.cern.todo.core.domain.TaskCategory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static ch.cern.todo.adapter.jpa.task.JpaUtils.getSortingById;
import static ch.cern.todo.adapter.jpa.task.JpaUtils.getTotalPages;

@Repository
@AllArgsConstructor
@Slf4j
public class TaskCategoryRepository implements TaskCategoryWriteStore, TaskCategoryReadStore {

    private final TaskCategoryRepositoryJpa taskCategoryRepositoryJpa;

    private final EntityManager entityManager;

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
    public void delete(final Long id) {
        try {
            taskCategoryRepositoryJpa.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            log.debug(e.getMessage());
            if(e.getCause() instanceof ConstraintViolationException) {
                throw new TaskRecordsMappedException("Unable to delete as tasks are mapped to the category");
            } else {
                throw new TaskCategoryException("Unknown task category error");
            }
        }
    }

    @Override
    public CustomPage<TaskCategoryProjection> getTaskCategories(final TaskCategoryFilters taskCategoryFilters) {
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final List<TaskCategoryEntity> taskEntities = getFilteredEntities(criteriaBuilder, taskCategoryFilters);
        final Long count = getFilteredCount(criteriaBuilder, taskCategoryFilters);
        return new CustomPage<>(taskEntities.stream()
                .map(TaskCategoryMapper::toTaskCategoryProjection)
                .toList(), count, getTotalPages(taskCategoryFilters.pageSize(), count));
    }

    @Override
    public Optional<TaskCategoryProjection> getTaskCategory(final Long id) {
        return taskCategoryRepositoryJpa.findById(id)
                .map(TaskCategoryMapper::toTaskCategoryProjection);
    }

    private Optional<TaskCategory> updateEntity(final TaskCategoryEntity entity, final TaskCategory taskCategory) {
            if(StringUtils.isNotBlank(taskCategory.getName())) {
                entity.setName(taskCategory.getName());
            }

            if(taskCategory.getDescription() != null) {
                entity.setDescription(taskCategory.getDescription());
            }

            return Optional.of(TaskCategoryMapper.toTaskCategory(entity));
    }

    private List<TaskCategoryEntity> getFilteredEntities(final CriteriaBuilder criteriaBuilder, final TaskCategoryFilters taskCategoryFilters) {
        final CriteriaQuery<TaskCategoryEntity> criteriaQuery = criteriaBuilder.createQuery(TaskCategoryEntity.class);
        final Root<TaskCategoryEntity> root = criteriaQuery.from(TaskCategoryEntity.class);
        final Set<Predicate> pagePredicates = getTaskCategoryPredicates(taskCategoryFilters, root, criteriaBuilder);
        criteriaQuery.where(criteriaBuilder.and(pagePredicates.toArray(new Predicate[pagePredicates.size()])));
        criteriaQuery.orderBy(getSortingById(criteriaBuilder, root, taskCategoryFilters.sortDirection()));
        final int offset = taskCategoryFilters.pageNumber() * taskCategoryFilters.pageSize();
        return entityManager.createQuery(criteriaQuery)
                .setMaxResults(taskCategoryFilters.pageSize())
                .setFirstResult(offset)
                .getResultList();
    }

    private Long getFilteredCount(final CriteriaBuilder criteriaBuilder, final TaskCategoryFilters taskCategoryFilters) {
        final CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        final Root<TaskCategoryEntity> root = countQuery.from(TaskCategoryEntity.class);
        final Set<Predicate> countPredicates = getTaskCategoryPredicates(taskCategoryFilters, root, criteriaBuilder);
        countQuery.select(criteriaBuilder.count(root))
                .where(criteriaBuilder.and(countPredicates.toArray(new Predicate[countPredicates.size()])));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private Set<Predicate> getTaskCategoryPredicates(final TaskCategoryFilters taskCategoryFilters, final Root<TaskCategoryEntity> root, final CriteriaBuilder criteriaBuilder) {
        final Set<Predicate> predicates = new HashSet<>();

        if (taskCategoryFilters.name() != null) {
            final Path<String> name = root.get("name");
            predicates.add(criteriaBuilder.equal(name, taskCategoryFilters.name()));
        }

        return predicates;
    }
}
