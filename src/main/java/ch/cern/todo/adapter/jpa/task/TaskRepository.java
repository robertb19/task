package ch.cern.todo.adapter.jpa.task;

import ch.cern.todo.adapter.jpa.task.category.TaskCategoryEntity;
import ch.cern.todo.adapter.jpa.task.category.TaskCategoryRepositoryJpa;
import ch.cern.todo.core.application.dto.CustomPage;
import ch.cern.todo.core.application.task.category.exception.TaskCategoryNotFoundException;
import ch.cern.todo.core.application.task.port.TaskReadStore;
import ch.cern.todo.core.application.task.port.TaskWriteStore;
import ch.cern.todo.core.application.task.query.dto.*;
import ch.cern.todo.core.domain.Task;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static ch.cern.todo.adapter.jpa.task.JpaUtils.getSortingById;
import static ch.cern.todo.adapter.jpa.task.JpaUtils.getTotalPages;

@Repository
@AllArgsConstructor
@Slf4j
public class TaskRepository implements TaskWriteStore, TaskReadStore {

    private final TaskRepositoryJpa taskRepositoryJpa;

    private final TaskCategoryRepositoryJpa taskCategoryRepositoryJpa;

    private final EntityManager entityManager;

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
        taskEntity.setCategory(taskCategoryEntity);
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

    public Optional<TaskProjection> getTask(final Long id) {
        return taskRepositoryJpa.findById(id)
                .map(TaskMapper::toTaskProjection);
    }

    public CustomPage<TaskProjection> getTasks(final TaskFilters taskFilters) {
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final List<TaskEntity> taskEntities = getFilteredEntities(criteriaBuilder, taskFilters);
        final Long count = getFilteredCount(criteriaBuilder, taskFilters);
        return new CustomPage<>(taskEntities.stream()
                .map(TaskMapper::toTaskProjection)
                .toList(), count, getTotalPages(taskFilters.pageSize(), count));
    }

    private Optional<TaskCategoryEntity> findForUpdate(final Task task) {
        return task.getTaskCategoryId() != null ? Optional.of(taskCategoryRepositoryJpa.findById(task.getTaskCategoryId())
                .orElseThrow(TaskCategoryNotFoundException::new)) :
                Optional.empty();
    }

    private Optional<Task> updateEntity(final TaskEntity entity, final Optional<TaskCategoryEntity> taskCategoryEntity, final Task task) {
        if (StringUtils.isNotBlank(task.getName())) {
            entity.setName(task.getName());
        }

        if (task.getDescription() != null) {
            entity.setDescription(task.getDescription());
        }

        if (task.getDeadline() != null) {
            entity.setDeadline(task.getDeadline());
        }

        taskCategoryEntity.ifPresent(entity::setCategory);
        return Optional.of(TaskMapper.toTask(entity));
    }

    private List<TaskEntity> getFilteredEntities(final CriteriaBuilder criteriaBuilder, final TaskFilters taskFilters) {
        final CriteriaQuery<TaskEntity> criteriaQuery = criteriaBuilder.createQuery(TaskEntity.class);

        final Root<TaskEntity> root = criteriaQuery.from(TaskEntity.class);
        root.fetch("category", JoinType.INNER);
        final Set<Predicate> pagePredicates = getTaskPredicates(taskFilters, root, criteriaBuilder);
        criteriaQuery.where(criteriaBuilder.and(pagePredicates.toArray(new Predicate[pagePredicates.size()])));
        criteriaQuery.orderBy(getSortingById(criteriaBuilder, root, taskFilters.sortDirection()));

        final int offset = taskFilters.pageNumber() * taskFilters.pageSize();
        return entityManager.createQuery(criteriaQuery)
                .setMaxResults(taskFilters.pageSize())
                .setFirstResult(offset)
                .getResultList();
    }

    private Long getFilteredCount(final CriteriaBuilder criteriaBuilder, final TaskFilters taskFilters) {
        final CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        final Root<TaskEntity> root = countQuery.from(TaskEntity.class);
        final Set<Predicate> countPredicates = getTaskPredicates(taskFilters, root, criteriaBuilder);
        countQuery.select(criteriaBuilder.count(root))
                .where(criteriaBuilder.and(countPredicates.toArray(new Predicate[countPredicates.size()])));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private Set<Predicate> getTaskPredicates(final TaskFilters taskFilters, final Root<TaskEntity> root, final CriteriaBuilder criteriaBuilder) {
        final Set<Predicate> predicates = new HashSet<>();

        if (taskFilters.categoryId() != null) {
            final Path<TaskCategoryEntity> category = root.get("category");
            predicates.add(criteriaBuilder.equal(category.get("id"), taskFilters.categoryId()));
        }

        if (taskFilters.deadline() != null) {
            final Path<ZonedDateTime> deadline = root.get("deadline");
            if (taskFilters.deadlineMode() == DeadlineMode.BEFORE) {
                predicates.add(criteriaBuilder.greaterThan(deadline, taskFilters.deadline()));
            } else {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(deadline, taskFilters.deadline()));
            }
        }

        if (taskFilters.name() != null) {
            final Path<String> name = root.get("name");
            predicates.add(criteriaBuilder.equal(name, taskFilters.name()));
        }

        return predicates;
    }

}
