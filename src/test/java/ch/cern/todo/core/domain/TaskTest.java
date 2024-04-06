package ch.cern.todo.core.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void givenTasks_whenEquals_returnTrue() {
        //given
        final ZonedDateTime now = Instant.now().atZone(ZoneId.of("UTC"));
        final ZonedDateTime hourFromNow = Instant.now().plus(Duration.ofHours(1)).atZone(ZoneId.of("UTC"));
        final Task task = new Task(1L, "name", "description", now, 1L);
        final Task other = new Task(1L, "otherName", "otherDescription", hourFromNow, 2L);

        //when and then
        assertTrue(task.equals(other) && other.equals(task)); //is reflexive, symmetric and transitive
    }

    @Test
    void givenUnequalTasks_whenEquals_returnFalse() {
        //given
        final ZonedDateTime now = Instant.now().atZone(ZoneId.of("UTC"));
        final Task task = new Task(1L, "name", "description", now, 1L);
        final Task other = new Task(2L, "name", "description", now, 1L);

        //when and then
        assertNotEquals(task, other);
        assertNotEquals(other, task);
    }

    @Test
    void givenTasks_whenHashCodeThemCompare_returnTrue() {
        final ZonedDateTime now = Instant.now().atZone(ZoneId.of("UTC"));
        final ZonedDateTime hourFromNow = Instant.now().plus(Duration.ofHours(1)).atZone(ZoneId.of("UTC"));
        final Task taskEntity = new Task(1L, "name", "description", now, 1L);
        final Task otherEntity = new Task(1L, "otherName", "otherDescription", hourFromNow, 2L);

        assertEquals(taskEntity.hashCode(), otherEntity.hashCode());
    }

}
