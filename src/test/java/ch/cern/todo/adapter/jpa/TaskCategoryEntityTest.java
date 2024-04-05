package ch.cern.todo.adapter.jpa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskCategoryEntityTest {

    @Test
    void givenTaskCategoryEntities_whenEquals_returnTrue() {
        //given
        final TaskCategoryEntity taskCategoryEntity = new TaskCategoryEntity(1L, "name", "description");
        final TaskCategoryEntity otherEntity = new TaskCategoryEntity(1L, "otherName", "otherDescription");

        //when and then
        //equals behaves as expected, as the unique identifier for a database record is the identifier it has on the database (while in the domain language, name would suit better as its unique)
        assertTrue(taskCategoryEntity.equals(otherEntity) && otherEntity.equals(taskCategoryEntity)); //is reflexive, symmetric and transitive
    }

    @Test
    void givenUnequalTaskCategoryEntities_whenEquals_returnFalse() {
        //given
        final TaskCategoryEntity taskCategoryEntity = new TaskCategoryEntity(1L, "name", "description");
        final TaskCategoryEntity otherEntity = new TaskCategoryEntity(2L, "name", "description");

        //when and then
        assertFalse(taskCategoryEntity.equals(otherEntity) && otherEntity.equals(taskCategoryEntity));
    }

    @Test
    void givenTaskCategoryEntities_whenHashCodeThemCompare_returnTrue() {
        final TaskCategoryEntity taskCategoryEntity = new TaskCategoryEntity(1L, "name", "description");
        final TaskCategoryEntity otherEntity = new TaskCategoryEntity(2L, "otherName", "otherDescription");

        assertEquals(taskCategoryEntity.hashCode(), otherEntity.hashCode());
    }

}
