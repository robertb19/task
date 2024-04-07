package ch.cern.todo.adapter.rest.v1_0.exception;

import ch.cern.todo.adapter.rest.v1_0.request.ListErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ListErrorResponse> handleException(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest()
                .body(new ListErrorResponse(exception.getAllErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.toSet()))
                );
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ListErrorResponse> handleException(ConstraintViolationException exception) {
        return ResponseEntity.badRequest()
                .body(new ListErrorResponse(exception.getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.toSet())));
    }


}
