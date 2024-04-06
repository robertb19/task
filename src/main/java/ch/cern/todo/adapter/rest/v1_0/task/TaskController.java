package ch.cern.todo.adapter.rest.v1_0.task;

import ch.cern.todo.adapter.rest.v1_0.request.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.task.request.AddTaskRequest;
import ch.cern.todo.core.application.TaskService;
import ch.cern.todo.core.application.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.command.dto.DeleteTaskCommand;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;

import static ch.cern.todo.adapter.rest.v1_0.RestConstant.VERSION_NUMBER_PATH;

@Validated
@RestController
@RequestMapping(VERSION_NUMBER_PATH + "/tasks")
@AllArgsConstructor
public class TaskController {

    private final TaskService taskService;

    private final Clock clock;

    //todo change to mappers & use builders in mapper as many parameters present
    @PostMapping
    public ResponseEntity<GenericAddResourceResponse> create(@RequestBody @Valid AddTaskRequest addTaskRequest) {
        final Long createdId = taskService
                .addTask(new AddTaskCommand(addTaskRequest.name(),
                        addTaskRequest.description(),
                        addTaskRequest.deadline().atZone(clock.getZone()),
                        addTaskRequest.categoryId()));

        return new ResponseEntity<>(new GenericAddResourceResponse(createdId), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final Long id) {
        taskService.deleteTask(new DeleteTaskCommand(id));
        return ResponseEntity.noContent().build();
    }

}
