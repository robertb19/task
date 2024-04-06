package ch.cern.todo.adapter.jpa.task;

import ch.cern.todo.adapter.jpa.task.category.TaskCategoryEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskEntityTest {

    @Test
    void givenTaskEntities_whenEquals_returnTrue() {
        //given
        final ZonedDateTime now = Instant.now().atZone(ZoneId.of("UTC"));
        final TaskCategoryEntity taskCategoryEntity = new TaskCategoryEntity(1L, "name", "description");
        final TaskEntity taskEntity = new TaskEntity(1L, "name", "description", now, taskCategoryEntity);
        final TaskEntity otherEntity = new TaskEntity(1L, "otherName", "otherDescription", now, taskCategoryEntity);

        //when and then
        //equals behaves as expected, as the unique identifier for a database record is the identifier it has on the database (while in the domain language, name would suit better as its unique)
        assertTrue(taskEntity.equals(otherEntity) && otherEntity.equals(taskEntity)); //is reflexive, symmetric and transitive
    }

    @Test
    void givenUnequalTaskEntities_whenEquals_returnFalse() {
        //given
        final ZonedDateTime now = Instant.now().atZone(ZoneId.of("UTC"));
        final TaskCategoryEntity taskCategoryEntity = new TaskCategoryEntity(1L, "name", "description");
        final TaskEntity taskEntity = new TaskEntity(1L, "name", "description", now, taskCategoryEntity);
        final TaskEntity otherEntity = new TaskEntity(2L, "name", "description", now, taskCategoryEntity);

        //when and then
        assertNotEquals(taskEntity, otherEntity);
        assertNotEquals(otherEntity, taskEntity);
    }

    @Test
    void givenTaskEntities_whenHashCodeThemCompare_returnTrue() {
        final ZonedDateTime now = Instant.now().atZone(ZoneId.of("UTC"));
        final TaskCategoryEntity taskCategoryEntity = new TaskCategoryEntity(1L, "name", "description");
        final TaskEntity taskEntity = new TaskEntity(1L, "name", "description", now, taskCategoryEntity);
        final TaskEntity otherEntity = new TaskEntity(2L, "otherName", "otherDescription", now, taskCategoryEntity);

        assertEquals(taskEntity.hashCode(), otherEntity.hashCode());
    }

}
