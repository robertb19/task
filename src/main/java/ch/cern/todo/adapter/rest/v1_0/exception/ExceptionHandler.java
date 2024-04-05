package ch.cern.todo.adapter.rest.v1_0.exception;

import ch.cern.todo.adapter.rest.v1_0.response.ErrorResponse;
import ch.cern.todo.core.application.exception.DuplicateTaskCategoryException;
import ch.cern.todo.core.application.exception.NotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionHandler {

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(value = {DuplicateTaskCategoryException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleException(RuntimeException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(exception.getAllErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(", ")))
                );
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(value = {NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Void> handleException() {
        return ResponseEntity.notFound().build();
    }


}
