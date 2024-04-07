package ch.cern.todo.adapter.rest.v1_0.task.category.exception;

import ch.cern.todo.adapter.rest.v1_0.request.ErrorResponse;
import ch.cern.todo.core.application.exception.DuplicateTaskCategoryException;
import ch.cern.todo.core.application.exception.TaskCategoryNotFoundException;
import ch.cern.todo.core.application.exception.TaskRecordsMappedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(basePackages="ch.cern.todo.adapter.rest.v1_0.task.category")
public class TaskCategoryExceptionHandler {

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(value = {DuplicateTaskCategoryException.class, TaskRecordsMappedException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleException(RuntimeException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatus.CONFLICT);
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(value = {TaskCategoryNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Void> handleException() {
        return ResponseEntity.notFound().build();
    }


}
