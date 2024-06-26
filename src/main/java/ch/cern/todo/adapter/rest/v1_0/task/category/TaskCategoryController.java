package ch.cern.todo.adapter.rest.v1_0.task.category;

import ch.cern.todo.adapter.rest.v1_0.task.category.request.AddTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.task.category.request.UpdateTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.request.CommonPage;
import ch.cern.todo.adapter.rest.v1_0.request.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.task.category.response.GetTaskCategoryResponse;
import ch.cern.todo.core.application.task.category.TaskCategoryService;
import ch.cern.todo.core.application.task.category.command.dto.AddTaskCategoryCommand;
import ch.cern.todo.core.application.task.category.command.dto.DeleteTaskCategoryCommand;
import ch.cern.todo.core.application.task.category.command.dto.UpdateTaskCategoryCommand;
import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryProjection;
import ch.cern.todo.core.application.dto.CustomPage;
import ch.cern.todo.core.application.dto.SortDirection;
import ch.cern.todo.core.application.task.category.query.dto.TaskCategoryFilters;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static ch.cern.todo.adapter.rest.v1_0.RestConstant.VERSION_NUMBER_PATH;

@Validated
@RestController
@RequestMapping(VERSION_NUMBER_PATH + "/categories")
@AllArgsConstructor
public class TaskCategoryController {

    private final TaskCategoryService taskCategoryService;

    @PostMapping
    public ResponseEntity<GenericAddResourceResponse> create(@RequestBody @Valid final AddTaskCategoryRequest addTaskCategoryRequest) {
        final Long createdId = taskCategoryService
                .addTaskCategory(new AddTaskCategoryCommand(addTaskCategoryRequest.name(), addTaskCategoryRequest.description()));

        return new ResponseEntity<>(new GenericAddResourceResponse(createdId), HttpStatus.CREATED);
    }

    //set required on page as false, as otherwise spring doesn't let you specify custom message and I'd like to inform the API user what's missing
    @GetMapping
    public ResponseEntity<CommonPage<GetTaskCategoryResponse>> get(@RequestParam(value = "page", required = false) @NotNull(message = "Page must be specified as request param") final Integer page,
                                          @RequestParam(value = "size", defaultValue = "10") final int size,
                                          @RequestParam(value = "sort", defaultValue = "DESC") final SortDirection sort,
                                          @RequestParam(value = "name", required = false) final String name) {
        final CustomPage<TaskCategoryProjection> taskCategoriesPage = taskCategoryService.getTaskCategories(new TaskCategoryFilters(name, page, size, sort));

        return ResponseEntity.ok(new CommonPage<>(page, size, taskCategoriesPage.getTotalElements(), taskCategoriesPage.getTotalPages(),
                taskCategoriesPage.getElements().stream()
                .map(GetTaskCategoryResponse::from)
                .toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetTaskCategoryResponse> getById(@PathVariable("id") final Long id) {
        return taskCategoryService.getTaskCategory(id)
                .map(taskCategory -> ResponseEntity.ok(GetTaskCategoryResponse.from(taskCategory)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") final Long id,
                                       @RequestBody @Valid UpdateTaskCategoryRequest updateTaskCategoryRequest) {
        taskCategoryService.updateTaskCategory(new UpdateTaskCategoryCommand(
                id, updateTaskCategoryRequest.name(), updateTaskCategoryRequest.description()));

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final Long id) {
        taskCategoryService.deleteTaskCategory(new DeleteTaskCategoryCommand(id));
        return ResponseEntity.noContent().build();
    }

}
