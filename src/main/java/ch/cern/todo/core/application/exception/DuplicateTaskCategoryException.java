package ch.cern.todo.core.application.exception;

public class DuplicateTaskCategoryException extends TaskCategoryException {

    public DuplicateTaskCategoryException(final String message) {
        super(message);
    }

}
