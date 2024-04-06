package ch.cern.todo.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskCategoryTest {

    @Test
    void givenTaskCategory_whenEquals_returnTrue() {
        //given
        final TaskCategory taskCategory = new TaskCategory(1L, "name", "description");
        final TaskCategory otherTaskCategory = new TaskCategory(2L, "name", "otherDescription");

        //when and then
        //equals behaves as expected, the unique name in the domain concept symbolizes two objects are equal
        assertTrue(taskCategory.equals(otherTaskCategory) && otherTaskCategory.equals(taskCategory)); //is reflexive, symmetric and transitive
    }

    @Test
    void givenUnequalTaskCategory_whenEquals_returnFalse() {
        //given
        final TaskCategory taskCategory = new TaskCategory(1L, "name", "description");
        final TaskCategory otherTaskCategory = new TaskCategory(1L, "otherName", "description");

        //when and then
        assertNotEquals(taskCategory, otherTaskCategory);
        assertNotEquals(otherTaskCategory, taskCategory);
    }

    @Test
    void givenTaskCategory_whenHashCodeThemCompare_returnTrue() {
        final TaskCategory taskCategory = new TaskCategory(1L, "name", "description");
        final TaskCategory otherTaskCategory = new TaskCategory(2L, "name", "otherDescription");

        assertEquals(taskCategory.hashCode(), otherTaskCategory.hashCode());
    }

    @Test
    void givenTaskCategory_whenHashCodeThemCompare_returnFalse() {
        final TaskCategory taskCategory = new TaskCategory(1L, "name", "description");
        final TaskCategory otherTaskCategory = new TaskCategory(1L, "otherName", "description");

        assertNotEquals(taskCategory.hashCode(), otherTaskCategory.hashCode());
    }

}
