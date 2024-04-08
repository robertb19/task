package ch.cern.todo.adapter.rest.v1_0.task.exception;

import ch.cern.todo.adapter.rest.v1_0.request.ErrorResponse;
import ch.cern.todo.core.application.task.category.exception.TaskCategoryNotFoundException;
import ch.cern.todo.core.application.task.exception.TaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(basePackages="ch.cern.todo.adapter.rest.v1_0.task")
public class TaskExceptionHandler {

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(value = {TaskCategoryNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleException() {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("Task category does not exist"));
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(value = {TaskNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleException(TaskNotFoundException taskNotFoundException) {
        return ResponseEntity
                .notFound()
                .build();
    }

}
