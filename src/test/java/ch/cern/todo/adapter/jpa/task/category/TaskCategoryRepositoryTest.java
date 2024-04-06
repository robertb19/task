package ch.cern.todo.adapter.jpa.task.category;

import ch.cern.todo.core.application.exception.DuplicateTaskCategoryException;
import ch.cern.todo.core.application.exception.TaskCategoryException;
import ch.cern.todo.core.application.exception.TaskRecordsMappedException;
import ch.cern.todo.core.application.query.TaskCategoryProjection;
import ch.cern.todo.core.application.query.dto.CustomPage;
import ch.cern.todo.core.application.query.dto.SortDirection;
import ch.cern.todo.core.application.query.dto.TaskCategoryFilters;
import ch.cern.todo.core.domain.TaskCategory;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    void givenId_whenDeleteTaskCategory_thenCheckRepoInvokedOnce() {
        //when
        taskCategoryRepository.deleteTaskCategory(1L);

        //then
        verify(taskCategoryRepositoryJpa).deleteById(1L);
    }

    @Test
    void givenIdToWhichTasksAreMapped_whenDeleteTaskCategory_throwTaskRecordsMappedException() {
        //given
        final DataIntegrityViolationException exception = new DataIntegrityViolationException("Tasks mapped to entity", mock(ConstraintViolationException.class));
        final Long id = 1L;

        //when
        doThrow(exception).when(taskCategoryRepositoryJpa).deleteById(id);
        final TaskRecordsMappedException result = assertThrows(TaskRecordsMappedException.class, () -> taskCategoryRepository.deleteTaskCategory(id));

        //then
        assertEquals("Unable to delete as tasks are mapped to the category", result.getMessage());
    }

    @Test
    void givenId_whenDeleteTaskCategory_throw() {
        //given
        final DataAccessException exception = new DataIntegrityViolationException("Entity incosistent");
        final Long id = 1L;

        //when
        doThrow(exception).when(taskCategoryRepositoryJpa).deleteById(id);
        final TaskCategoryException result = assertThrows(TaskCategoryException.class, () -> taskCategoryRepository.deleteTaskCategory(id));

        //then
        assertEquals("Unknown task category error", result.getMessage());
    }

    @Test
    void givenTaskCategoryFilters_whenGetTaskCategories_thenReturnCustomPage() {
        //given
        final TaskCategoryFilters taskCategoryFilters = TaskCategoryFilters.builder()
                .pageNumber(0)
                .pageSize(5)
                .sortDirection(SortDirection.ASC)
                .build();
        final List<TaskCategoryEntity> taskCategoryEntities = List.of(
                new TaskCategoryEntity(1L, "name1", "decription1"),
                new TaskCategoryEntity(2L, "name2", "decription2")
        );
        final PageRequest pageRequest = PageRequest.of(taskCategoryFilters.pageNumber(), taskCategoryFilters.pageSize(), getSorting(taskCategoryFilters.sortDirection()));
        final CustomPage<TaskCategoryProjection> expected = new CustomPage<>(taskCategoryEntities.stream()
                .map(entity -> new TaskCategoryProjection(entity.getId(), entity.getName(), entity.getDescription()))
                .toList(),
                2,1);

        //when
        when(taskCategoryRepositoryJpa.findAll(pageRequest))
                .thenReturn(new PageImpl<>(taskCategoryEntities, pageRequest, taskCategoryEntities.size()));
        final CustomPage<TaskCategoryProjection> result = taskCategoryRepository.getTaskCategories(taskCategoryFilters);

        //then
        assertEquals(expected, result);
    }

    @Test
    void givenTaskCategoryFilters_whenGetTaskCategoriesByName_thenReturnCustomPage() {
        //given
        final String name = "name1";
        final TaskCategoryFilters taskCategoryFilters = TaskCategoryFilters.builder()
                .pageNumber(0)
                .pageSize(5)
                .name(name)
                .sortDirection(SortDirection.DESC)
                .build();
        final List<TaskCategoryEntity> taskCategoryEntities = List.of(
                new TaskCategoryEntity(1L, name, "decription1")
        );
        final PageRequest pageRequest = PageRequest.of(taskCategoryFilters.pageNumber(), taskCategoryFilters.pageSize(), getSorting(taskCategoryFilters.sortDirection()));
        final CustomPage<TaskCategoryProjection> expected = new CustomPage<>(taskCategoryEntities.stream()
                .map(entity -> new TaskCategoryProjection(entity.getId(), entity.getName(), entity.getDescription()))
                .toList(),
                1,1);

        //when
        when(taskCategoryRepositoryJpa.findAllByName(name, pageRequest))
                .thenReturn(new PageImpl<>(taskCategoryEntities, pageRequest, taskCategoryEntities.size()));
        final CustomPage<TaskCategoryProjection> result = taskCategoryRepository.getTaskCategoriesByName(taskCategoryFilters);

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
}
