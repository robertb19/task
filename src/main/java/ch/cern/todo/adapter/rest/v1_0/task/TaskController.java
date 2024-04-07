package ch.cern.todo.adapter.rest.v1_0.task;

import ch.cern.todo.adapter.rest.v1_0.request.CommonPage;
import ch.cern.todo.adapter.rest.v1_0.request.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.task.request.AddTaskRequest;
import ch.cern.todo.adapter.rest.v1_0.task.request.UpdateTaskRequest;
import ch.cern.todo.adapter.rest.v1_0.task.response.GetTaskResponse;
import ch.cern.todo.core.application.TaskService;
import ch.cern.todo.core.application.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.command.dto.DeleteTaskCommand;
import ch.cern.todo.core.application.command.dto.UpdateTaskCommand;
import ch.cern.todo.core.application.query.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.Instant;

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

    //todo change to mappers & use builders in mapper as many parameters present
    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") final Long id,
                                       @RequestBody @Valid UpdateTaskRequest updateTaskRequest) {
        taskService.updateTask(new UpdateTaskCommand(
                id,
                updateTaskRequest.name(),
                updateTaskRequest.description(),
                updateTaskRequest.deadline() != null ? updateTaskRequest.deadline().atZone(clock.getZone()) : null,
                updateTaskRequest.categoryId()
        ));

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetTaskResponse> getById(@PathVariable("id") final Long id) {
        return taskService.getTask(id)
                .map(taskProjection -> ResponseEntity.ok(GetTaskResponse.from(taskProjection)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<CommonPage<GetTaskResponse>> get(@RequestParam(value = "page", required = false) @NotNull(message = "Page must be specified as request param") final Integer page,
                                                           @RequestParam(value = "size", defaultValue = "10") final int size,
                                                           @RequestParam(value = "sort", defaultValue = "DESC") final SortDirection sort,
                                                           @RequestParam(value = "name", required = false) final String name,
                                                           @RequestParam(value = "deadlineDate", required = false) final Long deadline,
                                                           @RequestParam(value="deadlineMode", required = false, defaultValue = "AFTER") final DeadlineMode deadlineMode,
                                                           @RequestParam(value="category", required = false) final Long category) {
        final CustomPage<TaskProjection> tasksPage = taskService.getTasks(
                new TaskFilters(name,
                        category,
                        deadline != null ? Instant.ofEpochSecond(deadline).atZone(clock.getZone()) : null,
                        deadlineMode,
                        page,
                        size,
                        sort
                ));

        return ResponseEntity.ok(new CommonPage<>(page, size, tasksPage.getTotalElements(), tasksPage.getTotalPages(),
                tasksPage.getElements().stream()
                        .map(GetTaskResponse::from)
                        .toList()));
    }

}
