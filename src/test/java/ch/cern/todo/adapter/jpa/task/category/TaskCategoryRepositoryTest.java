package ch.cern.todo.adapter.jpa.task.category;

import ch.cern.todo.core.application.dto.CustomPage;
import ch.cern.todo.core.application.dto.SortDirection;
import ch.cern.todo.core.application.task.category.exception.DuplicateTaskCategoryException;
import ch.cern.todo.core.application.task.category.exception.TaskCategoryException;
import ch.cern.todo.core.application.task.category.exception.TaskRecordsMappedException;
import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryFilters;
import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryProjection;
import ch.cern.todo.core.domain.TaskCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskCategoryRepositoryTest {

    @Mock
    private TaskCategoryRepositoryJpa taskCategoryRepositoryJpa;
    
    @Mock
    private EntityManager entityManager;
    
    @InjectMocks
    private TaskCategoryRepository taskCategoryRepository;

    @Test
    void givenNonExistentTaskCategory_whenSave_returnTaskCategory() {
        //given
        final TaskCategory taskCategory = new TaskCategory("name", "description");
        final TaskCategory expectedTaskCategory = new TaskCategory(1L, "name", "description");
        final TaskCategoryEntity persistedTaskCategoryEntity = new TaskCategoryEntity(1L, taskCategory.getName(), taskCategory.getDescription());

        //when
        when(taskCategoryRepositoryJpa.save(any(TaskCategoryEntity.class)))
                .thenReturn(persistedTaskCategoryEntity);
        final TaskCategory result = taskCategoryRepository.save(taskCategory);

        //then
        assertEquals(expectedTaskCategory, result);
        assertEquals(expectedTaskCategory.getDescription(), result.getDescription());
        assertEquals(expectedTaskCategory.getId(), result.getId());
    }

    @Test
    void givenTaskCategoryWithDuplicateName_whenSave_throwDuplicateTaskCategoryException() {
        //given
        final TaskCategory taskCategory = new TaskCategory("name", "description");
        final DataIntegrityViolationException expectedException = new DataIntegrityViolationException("Entity already exists", mock(ConstraintViolationException.class));

        //when
        when(taskCategoryRepositoryJpa.save(any(TaskCategoryEntity.class)))
                .thenThrow(expectedException);
        final DuplicateTaskCategoryException duplicateTaskCategoryException =
                assertThrows(DuplicateTaskCategoryException.class, () -> taskCategoryRepository.save(taskCategory));

        //then
        assertEquals("Task category already exists", duplicateTaskCategoryException.getMessage());
    }

    @Test
    void givenTaskCategoryWithUnknownDbError_whenSave_throwTaskCategoryException() {
        //given
        final TaskCategory taskCategory = new TaskCategory("name", "description");
        final DataAccessException expectedException = new DataIntegrityViolationException("Entity incosistent");

        //when
        when(taskCategoryRepositoryJpa.save(any(TaskCategoryEntity.class)))
                .thenThrow(expectedException);
        final TaskCategoryException taskCategoryException =
                assertThrows(TaskCategoryException.class, () -> taskCategoryRepository.save(taskCategory));

        //then
        assertEquals("Unknown task category error", taskCategoryException.getMessage());
    }

    @Test
    void givenId_whenGet_returnTaskCategoryOptional() {
        //given
        final Long id = 1L;
        final TaskCategory expectedTaskCategory = new TaskCategory(1L, "name", "description");
        final TaskCategoryEntity persistedTaskCategoryEntity = new TaskCategoryEntity(1L, expectedTaskCategory.getName(), expectedTaskCategory.getDescription());


        //when
        when(taskCategoryRepositoryJpa.findById(id))
                .thenReturn(Optional.of(persistedTaskCategoryEntity));
        final Optional<TaskCategory> result = taskCategoryRepository.get(id);

        //then
        assertTrue(result.isPresent());
        assertEquals(expectedTaskCategory, result.get());
        assertEquals(expectedTaskCategory.getDescription(), result.get().getDescription());
        assertEquals(expectedTaskCategory.getId(), result.get().getId());
    }

    @Test
    void givenTaskCategory_whenUpdate_returnTaskCategoryOptional() {
        //given
        final TaskCategory expected = new TaskCategory(1L, "name", "description");
        final TaskCategoryEntity persistedTaskCategoryEntity = new TaskCategoryEntity(1L, "oldName", "oldDescription");

        //when
        when(taskCategoryRepositoryJpa.findById(expected.getId()))
                .thenReturn(Optional.of(persistedTaskCategoryEntity));
        final Optional<TaskCategory> result = taskCategoryRepository.update(expected);

        //then
        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
        assertEquals(expected.getDescription(), result.get().getDescription());
        assertEquals(expected.getId(), result.get().getId());
    }

    @Test
    void givenPartialTaskCategory_whenUpdate_returnTaskCategoryOptional() {
        //given
        final TaskCategory taskCategory = new TaskCategory(1L, null, "name");
        final TaskCategory expected = new TaskCategory(1L, "oldName", "name");
        final TaskCategoryEntity persistedTaskCategoryEntity = new TaskCategoryEntity(1L, "oldName", "oldDescription");

        //when
        when(taskCategoryRepositoryJpa.findById(taskCategory.getId()))
                .thenReturn(Optional.of(persistedTaskCategoryEntity));
        final Optional<TaskCategory> result = taskCategoryRepository.update(taskCategory);

        //then
        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
        assertEquals(expected.getDescription(), result.get().getDescription());
        assertEquals(expected.getId(), result.get().getId());
    }

    @Test
    void givenInexistentTaskCategory_whenUpdate_returnEmptyOptional() {
        //given
        final TaskCategory taskCategory = new TaskCategory(1L, null, "name");

        //when
        when(taskCategoryRepositoryJpa.findById(taskCategory.getId()))
                .thenReturn(Optional.empty());
        final Optional<TaskCategory> result = taskCategoryRepository.update(taskCategory);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void givenTaskCategoryWithDuplicateName_whenUpdate_throwDuplicateTaskCategoryException() {
        //given
        final TaskCategory taskCategory = new TaskCategory(1L, "name", "description");
        final DataIntegrityViolationException expectedException = new DataIntegrityViolationException("Entity already exists", mock(ConstraintViolationException.class));
        final TaskCategoryEntity persistedTaskCategoryEntity = new TaskCategoryEntity(1L, "name", "description");

        //when
        when(taskCategoryRepositoryJpa.findById(taskCategory.getId()))
                .thenReturn(Optional.of(persistedTaskCategoryEntity));
        doThrow(expectedException).when(taskCategoryRepositoryJpa).flush();
        final DuplicateTaskCategoryException duplicateTaskCategoryException =
                assertThrows(DuplicateTaskCategoryException.class, () -> taskCategoryRepository.update(taskCategory));

        //then
        assertEquals("Task category already exists", duplicateTaskCategoryException.getMessage());
    }

    @Test
    void givenTaskCategoryWithUnknownDbError_whenUpdate_throwTaskCategoryException() {
        //given
        final TaskCategory taskCategory = new TaskCategory(1L, "name", "description");
        final DataAccessException expectedException = new DataIntegrityViolationException("Entity incosistent");
        final TaskCategoryEntity persistedTaskCategoryEntity = new TaskCategoryEntity(1L, "name", "description");

        //when
        when(taskCategoryRepositoryJpa.findById(taskCategory.getId()))
                .thenReturn(Optional.of(persistedTaskCategoryEntity));
        doThrow(expectedException).when(taskCategoryRepositoryJpa).flush();
        final TaskCategoryException taskCategoryException =
                assertThrows(TaskCategoryException.class, () -> taskCategoryRepository.update(taskCategory));

        //then
        assertEquals("Unknown task category error", taskCategoryException.getMessage());
    }

    @Test
    void givenId_whenDelete_thenCheckRepoInvokedOnce() {
        //when
        taskCategoryRepository.delete(1L);

        //then
        verify(taskCategoryRepositoryJpa).deleteById(1L);
    }

    @Test
    void givenIdToWhichTasksAreMapped_whenDelete_throwTaskRecordsMappedException() {
        //given
        final DataIntegrityViolationException exception = new DataIntegrityViolationException("Tasks mapped to entity", mock(ConstraintViolationException.class));
        final Long id = 1L;

        //when
        doThrow(exception).when(taskCategoryRepositoryJpa).deleteById(id);
        final TaskRecordsMappedException result = assertThrows(TaskRecordsMappedException.class, () -> taskCategoryRepository.delete(id));

        //then
        assertEquals("Unable to delete as tasks are mapped to the category", result.getMessage());
    }

    @Test
    void givenId_whenDelete_throwTaskCategoryException() {
        //given
        final DataAccessException exception = new DataIntegrityViolationException("Entity incosistent");
        final Long id = 1L;

        //when
        doThrow(exception).when(taskCategoryRepositoryJpa).deleteById(id);
        final TaskCategoryException result = assertThrows(TaskCategoryException.class, () -> taskCategoryRepository.delete(id));

        //then
        assertEquals("Unknown task category error", result.getMessage());
    }

    @Test
    void givenTaskCategoryFiltersWithName_whenGetTaskCategories_thenReturnCustomPage() {
        //given
        final TaskCategoryFilters taskCategoryFilters = new TaskCategoryFilters(
                "name",
                0,
                0,
                SortDirection.ASC
        );
        final CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        final CriteriaQuery criteriaQuery = mock(CriteriaQuery.class);
        final Root root = mock(Root.class);
        final Path path = mock(Path.class);
        final Predicate predicate = mock(Predicate.class);
        final TypedQuery typedQuery = mock(TypedQuery.class);
        final TaskCategoryEntity taskCategoryEntity = new TaskCategoryEntity(1L, "name", "description");
        final TaskCategoryEntity taskCategoryEntity2 = new TaskCategoryEntity(2L, "secondName", "secondDescription");
        final Long count = 2L;
        final CustomPage<TaskCategoryProjection> expected = getTaskCategoryProjectionCustomPage(taskCategoryEntity, taskCategoryEntity2);

        //when
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(TaskCategoryEntity.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(TaskCategoryEntity.class)).thenReturn(root);
        when(root.get(anyString())).thenReturn(path);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(taskCategoryFilters.pageSize())).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(taskCategoryFilters.pageSize() * taskCategoryFilters.pageNumber())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(taskCategoryEntity, taskCategoryEntity2));
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQuery);
        when(criteriaBuilder.count(any())).thenReturn(root);
        when(criteriaQuery.select(any(Expression.class))).thenReturn(criteriaQuery);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        when(criteriaQuery.where(any(Expression.class))).thenReturn(criteriaQuery);
        when(typedQuery.getSingleResult()).thenReturn(count);

        final CustomPage<TaskCategoryProjection> result = taskCategoryRepository.getTaskCategories(taskCategoryFilters);

        //then
        assertEquals(expected, result);
    }

    @Test
    void givenTaskCategoryFilters_whenGetTaskCategories_thenReturnCustomPage() {
        //given
        final TaskCategoryFilters taskCategoryFilters = new TaskCategoryFilters(
                null,
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
        final TaskCategoryEntity taskCategoryEntity2 = new TaskCategoryEntity(2L, "secondName", "secondDescription");
        final Long count = 2L;
        final CustomPage<TaskCategoryProjection> expected = getTaskCategoryProjectionCustomPage(taskCategoryEntity, taskCategoryEntity2);

        //when
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(TaskCategoryEntity.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(TaskCategoryEntity.class)).thenReturn(root);
        when(root.get(anyString())).thenReturn(path);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(taskCategoryFilters.pageSize())).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(taskCategoryFilters.pageSize() * taskCategoryFilters.pageNumber())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(taskCategoryEntity, taskCategoryEntity2));
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQuery);
        when(criteriaBuilder.count(any())).thenReturn(root);
        when(criteriaQuery.select(any(Expression.class))).thenReturn(criteriaQuery);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        when(criteriaQuery.where(any(Expression.class))).thenReturn(criteriaQuery);
        when(typedQuery.getSingleResult()).thenReturn(count);

        final CustomPage<TaskCategoryProjection> result = taskCategoryRepository.getTaskCategories(taskCategoryFilters);

        //then
        assertEquals(expected, result);
    }

    @Test
    void givenId_whenGetTaskCategory_returnTaskCategoryProjectionOptional() {
        //given
        final Optional<TaskCategoryProjection> expected = Optional.of(
                new TaskCategoryProjection(1L, "name1", "description")
        );
        final Optional<TaskCategoryEntity> entity = Optional.of(
                new TaskCategoryEntity(expected.get().id(), expected.get().name(), expected.get().description())
        );

        //when
        when(taskCategoryRepositoryJpa.findById(1L))
                .thenReturn(entity);
        final Optional<TaskCategoryProjection> result = taskCategoryRepository.getTaskCategory(1L);

        //then
        assertEquals(expected, result);
    }

    private Sort getSorting(SortDirection sortDirection) {
        return sortDirection == SortDirection.ASC ?
                Sort.by("id").ascending() :
                Sort.by("id").descending();
    }

    private static CustomPage<TaskCategoryProjection> getTaskCategoryProjectionCustomPage(TaskCategoryEntity taskCategoryEntity, TaskCategoryEntity taskCategoryEntity2) {
        final TaskCategoryProjection taskCategoryProjection = new TaskCategoryProjection(taskCategoryEntity.getId(), taskCategoryEntity.getName(), taskCategoryEntity.getDescription());
        final TaskCategoryProjection taskCategoryProjection2 = new TaskCategoryProjection(taskCategoryEntity2.getId(), taskCategoryEntity2.getName(), taskCategoryEntity2.getDescription());
        return new CustomPage<>(List.of(taskCategoryProjection, taskCategoryProjection2), 2, 0);
    }
}
