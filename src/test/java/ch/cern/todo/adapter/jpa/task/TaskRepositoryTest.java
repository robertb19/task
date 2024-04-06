package ch.cern.todo.adapter.jpa.task;

import ch.cern.todo.adapter.jpa.task.category.TaskCategoryEntity;
import ch.cern.todo.adapter.jpa.task.category.TaskCategoryRepositoryJpa;
import ch.cern.todo.core.application.exception.TaskCategoryNotFoundException;
import ch.cern.todo.core.domain.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRepositoryTest {

    @Mock
    private TaskRepositoryJpa taskRepositoryJpa;

    @Mock
    private TaskCategoryRepositoryJpa taskCategoryRepositoryJpa;

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


}
