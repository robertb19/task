package ch.cern.todo.adapter.rest.v1_0.task;

import ch.cern.todo.adapter.rest.v1_0.request.CommonPage;
import ch.cern.todo.adapter.rest.v1_0.request.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.task.request.AddTaskRequest;
import ch.cern.todo.adapter.rest.v1_0.task.request.UpdateTaskRequest;
import ch.cern.todo.adapter.rest.v1_0.task.response.GetTaskResponse;
import ch.cern.todo.core.application.dto.CustomPage;
import ch.cern.todo.core.application.dto.SortDirection;
import ch.cern.todo.core.application.task.TaskService;
import ch.cern.todo.core.application.task.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.task.command.dto.DeleteTaskCommand;
import ch.cern.todo.core.application.task.command.dto.UpdateTaskCommand;
import ch.cern.todo.core.application.task.query.dto.DeadlineMode;
import ch.cern.todo.core.application.task.query.dto.TaskFilters;
import ch.cern.todo.core.application.task.query.dto.TaskProjection;
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

    @PostMapping
    public ResponseEntity<GenericAddResourceResponse> create(@RequestBody @Valid AddTaskRequest addTaskRequest) {
        final Long createdId = taskService.addTask(AddTaskCommand.builder()
                .name(addTaskRequest.name())
                .description(addTaskRequest.description())
                .deadline(addTaskRequest.deadline().atZone(clock.getZone()))
                .categoryId(addTaskRequest.categoryId()).build());

        return new ResponseEntity<>(new GenericAddResourceResponse(createdId), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final Long id) {
        taskService.deleteTask(new DeleteTaskCommand(id));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") final Long id,
                                       @RequestBody @Valid UpdateTaskRequest updateTaskRequest) {

        taskService.updateTask(UpdateTaskCommand.builder()
                .id(id)
                .name(updateTaskRequest.name())
                .description(updateTaskRequest.description())
                .deadline(updateTaskRequest.deadline() != null ? updateTaskRequest.deadline().atZone(clock.getZone()) : null)
                .categoryId(updateTaskRequest.categoryId()).build());

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
                                                           @RequestParam(value = "deadlineMode", required = false, defaultValue = "AFTER") final DeadlineMode deadlineMode,
                                                           @RequestParam(value = "category", required = false) final Long category) {
        final CustomPage<TaskProjection> tasksPage = taskService.getTasks(TaskFilters.builder()
                .name(name)
                .categoryId(category)
                .deadline(deadline != null ? Instant.ofEpochSecond(deadline).atZone(clock.getZone()) : null)
                .deadlineMode(deadlineMode)
                .pageNumber(page)
                .pageSize(size)
                .sortDirection(sort)
                .build());

        return ResponseEntity.ok(new CommonPage<>(page, size, tasksPage.getTotalElements(), tasksPage.getTotalPages(),
                tasksPage.getElements().stream()
                        .map(GetTaskResponse::from)
                        .toList()));
    }

}
