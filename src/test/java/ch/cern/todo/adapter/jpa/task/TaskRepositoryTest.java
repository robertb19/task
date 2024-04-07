package ch.cern.todo.adapter.jpa.task;

import ch.cern.todo.adapter.jpa.task.category.TaskCategoryEntity;
import ch.cern.todo.adapter.jpa.task.category.TaskCategoryRepositoryJpa;
import ch.cern.todo.core.application.exception.TaskCategoryNotFoundException;
import ch.cern.todo.core.application.query.dto.*;
import ch.cern.todo.core.domain.Task;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRepositoryTest {

    @Mock
    private TaskRepositoryJpa taskRepositoryJpa;

    @Mock
    private TaskCategoryRepositoryJpa taskCategoryRepositoryJpa;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TaskRepository taskRepository;

    @Test
    void givenNonExistentTask_whenSave_returnTask() {
        //given
        final Instant now = Instant.now();
        final Task task = new Task(
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                1L);
        final Task expectedTask = new Task(
                1L,
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                1L);
        final TaskCategoryEntity taskCategoryEntity = new TaskCategoryEntity(1L, "name", "description");
        final TaskEntity persistedTaskEntity = new TaskEntity(1L, task.getName(), task.getDescription(), task.getDeadline(), taskCategoryEntity);

        //when
        when(taskCategoryRepositoryJpa.findById(taskCategoryEntity.getId())).thenReturn(Optional.of(taskCategoryEntity));
        when(taskRepositoryJpa.save(any(TaskEntity.class)))
                .thenReturn(persistedTaskEntity);
        final Task result = taskRepository.save(task);

        //then
        assertEquals(expectedTask, result);
        assertEquals(expectedTask.getName(), result.getName());
        assertEquals(expectedTask.getDescription(), result.getDescription());
        assertEquals(expectedTask.getDeadline(), result.getDeadline());
        assertEquals(expectedTask.getTaskCategoryId(), result.getTaskCategoryId());
    }

    @Test
    void givenNonExistentTaskWithInexistentCategoryId_whenSave_throwNewTaskCategoryNotFoundException() {
        //given
        final Instant now = Instant.now();
        final Task task = new Task(
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                1L);

        //when
        when(taskCategoryRepositoryJpa.findById(task.getTaskCategoryId()))
                .thenThrow(new TaskCategoryNotFoundException());
        assertThrows(TaskCategoryNotFoundException.class, () -> taskRepository.save(task));
    }

    @Test
    void givenId_whenDelete_thenCheckRepoInvokedOnce() {
        //when and then
        taskRepository.delete(1L);
        verify(taskRepositoryJpa).deleteById(1L);
    }

    @Test
    void givenTask_whenUpdate_returnTaskOptional() {
        //given
        final Instant now = Instant.now();
        final Task task = new Task(
                1L,
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                2L);
        final TaskCategoryEntity oldTaskCategoryEntity = new TaskCategoryEntity(2L, "oldName", "oldDescription");
        final TaskEntity taskEntity = new TaskEntity(1L, "oldName", "oldDescription", now.minusSeconds(1L).atZone(ZoneId.of("UTC")), oldTaskCategoryEntity);

        //when
        when(taskRepositoryJpa.findById(task.getId())).thenReturn(Optional.of(taskEntity));
        when(taskCategoryRepositoryJpa.findById(task.getTaskCategoryId())).thenReturn(Optional.of(oldTaskCategoryEntity));
        final Optional<Task> result = taskRepository.update(task);

        //then
        assertTrue(result.isPresent());
        assertEquals(task, result.get());
    }

    @Test
    void givenTaskWithNullTaskCategory_whenUpdate_returnTaskOptional() {
        //given
        final Instant now = Instant.now();
        final Task task = new Task(
                1L,
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                null);
        final TaskCategoryEntity oldTaskCategoryEntity = new TaskCategoryEntity(2L, "oldName", "oldDescription");
        final TaskEntity taskEntity = new TaskEntity(1L, "oldName", "oldDescription", now.minusSeconds(1L).atZone(ZoneId.of("UTC")), oldTaskCategoryEntity);

        //when
        when(taskRepositoryJpa.findById(task.getId())).thenReturn(Optional.of(taskEntity));
        final Optional<Task> result = taskRepository.update(task);

        //then
        assertTrue(result.isPresent());
        assertEquals(task, result.get());
    }

    @Test
    void givenInexistentTask_whenUpdate_returnEmptyOptional() {
        //given
        final Instant now = Instant.now();
        final Task task = new Task(
                1L,
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                2L);

        //when
        when(taskRepositoryJpa.findById(task.getId())).thenReturn(Optional.empty());
        final Optional<Task> result = taskRepository.update(task);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void givenTaskWithInexistentCategoryName_whenUpdate_throwTaskCategoryNotFoundException() {
        //given
        final Instant now = Instant.now();
        final Task task = new Task(
                1L,
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                2L);
        final TaskCategoryEntity oldTaskCategoryEntity = new TaskCategoryEntity(2L, "oldName", "oldDescription");
        final TaskEntity taskEntity = new TaskEntity(1L, "oldName", "oldDescription", now.minusSeconds(1L).atZone(ZoneId.of("UTC")), oldTaskCategoryEntity);

        //when and then
        when(taskRepositoryJpa.findById(task.getId())).thenReturn(Optional.of(taskEntity));
        when(taskCategoryRepositoryJpa.findById(task.getTaskCategoryId())).thenThrow(new TaskCategoryNotFoundException());
        assertThrows(TaskCategoryNotFoundException.class, () -> taskRepository.update(task));
    }

    @Test
    void givenId_whenGetTask_returnTaskProjectionOptional() {
        //given
        final Long id = 1L;
        final Instant now = Instant.now();
        final TaskCategoryProjection taskCategoryProjection = new TaskCategoryProjection(1L, "name", "description");
        final TaskProjection taskProjection = new TaskProjection(
                1L,
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                taskCategoryProjection);
        final TaskCategoryEntity taskCategoryEntity = new TaskCategoryEntity(taskCategoryProjection.id(), taskCategoryProjection.name(), taskCategoryProjection.description());
        final TaskEntity taskEntity = new TaskEntity(id, taskProjection.name(), taskProjection.description(), taskProjection.deadline(), taskCategoryEntity);

        //when
        when(taskRepositoryJpa.findById(id)).thenReturn(Optional.of(taskEntity));
        final Optional<TaskProjection> result = taskRepository.getTask(id);

        //then
        assertTrue(result.isPresent());
        assertEquals(taskProjection, result.get());
    }

    @Test
    void givenTaskFilters_whenGetTasks_returnTaskProjectionsPage() {
        //given
        final Instant now = Instant.now();
        final TaskFilters taskCategoryFilters = new TaskFilters(
                "name",
                1L,
                now.atZone(ZoneId.of("UTC")),
                DeadlineMode.AFTER,

                0,
                5,
                SortDirection.ASC
        );
        final CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        final CriteriaQuery criteriaQuery = mock(CriteriaQuery.class);
        final Root root = mock(Root.class);
        final Path path = mock(Path.class);
        final Predicate predicate = mock(Predicate.class);
        final TypedQuery typedQuery = mock(TypedQuery.class);
        final TaskCategoryEntity taskCategoryEntity = new TaskCategoryEntity(1L, "name", "description");
        final List<TaskEntity> taskEntities = List.of(
                new TaskEntity(1L, "name1", "desc1", now.minusSeconds(1L).atZone(ZoneId.of("UTC")), taskCategoryEntity),
                new TaskEntity(2L, "name2", "desc2", now.minusSeconds(10L).atZone(ZoneId.of("UTC")), taskCategoryEntity)

        );
        final Long count = 2L;
        final TaskCategoryProjection taskCategoryProjection = new TaskCategoryProjection(taskCategoryEntity.getId(), taskCategoryEntity.getName(), taskCategoryEntity.getDescription());
        final List<TaskProjection> taskProjections = List.of(
                new TaskProjection(taskEntities.get(0).getId(), taskEntities.get(0).getName(), taskEntities.get(0).getDescription(), taskEntities.get(0).getDeadline(), taskCategoryProjection),
                new TaskProjection(taskEntities.get(1).getId(), taskEntities.get(1).getName(), taskEntities.get(1).getDescription(), taskEntities.get(1).getDeadline(), taskCategoryProjection)
        );
        final CustomPage<TaskProjection> expected = new CustomPage<>(taskProjections, 2, 1);

        //when
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(TaskEntity.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(TaskEntity.class)).thenReturn(root);
        when(root.get(anyString())).thenReturn(path);
        when(criteriaBuilder.lessThanOrEqualTo(any(Path.class), any(ZonedDateTime.class))).thenReturn(predicate);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(taskCategoryFilters.pageSize())).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(taskCategoryFilters.pageSize() * taskCategoryFilters.pageNumber())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(taskEntities);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQuery);
        when(criteriaBuilder.count(any())).thenReturn(root);
        when(criteriaQuery.select(any(Expression.class))).thenReturn(criteriaQuery);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        when(criteriaQuery.where(any(Expression.class))).thenReturn(criteriaQuery);
        when(typedQuery.getSingleResult()).thenReturn(count);

        final CustomPage<TaskProjection> result = taskRepository.getTasks(taskCategoryFilters);

        //then
        assertEquals(expected, result);
    }

    @Test
    void givenTaskFiltersWithPageSize0DescOrderAndBeforeDeadline_whenGetTasks_returnTaskProjectionsPage() {
        //given
        final Instant now = Instant.now();
        final TaskFilters taskCategoryFilters = new TaskFilters(
                "name",
                1L,
                now.atZone(ZoneId.of("UTC")),
                DeadlineMode.BEFORE,

                0,
                0,
                SortDirection.DESC
        );
        final CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        final CriteriaQuery criteriaQuery = mock(CriteriaQuery.class);
        final Root root = mock(Root.class);
        final Path path = mock(Path.class);
        final Predicate predicate = mock(Predicate.class);
        final TypedQuery typedQuery = mock(TypedQuery.class);
        final TaskCategoryEntity taskCategoryEntity = new TaskCategoryEntity(1L, "name", "description");
        final List<TaskEntity> taskEntities = List.of(
                new TaskEntity(1L, "name1", "desc1", now.plusSeconds(1L).atZone(ZoneId.of("UTC")), taskCategoryEntity),
                new TaskEntity(2L, "name2", "desc2", now.plusSeconds(10L).atZone(ZoneId.of("UTC")), taskCategoryEntity)

        );
        final Long count = 2L;
        final TaskCategoryProjection taskCategoryProjection = new TaskCategoryProjection(taskCategoryEntity.getId(), taskCategoryEntity.getName(), taskCategoryEntity.getDescription());
        final List<TaskProjection> taskProjections = List.of(
                new TaskProjection(taskEntities.get(0).getId(), taskEntities.get(0).getName(), taskEntities.get(0).getDescription(), taskEntities.get(0).getDeadline(), taskCategoryProjection),
                new TaskProjection(taskEntities.get(1).getId(), taskEntities.get(1).getName(), taskEntities.get(1).getDescription(), taskEntities.get(1).getDeadline(), taskCategoryProjection)
        );
        final CustomPage<TaskProjection> expected = new CustomPage<>(taskProjections, 2, 0);

        //when
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(TaskEntity.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(TaskEntity.class)).thenReturn(root);
        when(root.get(anyString())).thenReturn(path);
        when(criteriaBuilder.greaterThan(any(Path.class), any(ZonedDateTime.class))).thenReturn(predicate);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(taskCategoryFilters.pageSize())).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(taskCategoryFilters.pageSize() * taskCategoryFilters.pageNumber())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(taskEntities);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQuery);
        when(criteriaBuilder.count(any())).thenReturn(root);
        when(criteriaQuery.select(any(Expression.class))).thenReturn(criteriaQuery);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        when(criteriaQuery.where(any(Expression.class))).thenReturn(criteriaQuery);
        when(typedQuery.getSingleResult()).thenReturn(count);

        final CustomPage<TaskProjection> result = taskRepository.getTasks(taskCategoryFilters);

        //then
        assertEquals(expected, result);
    }


}
