package ch.cern.todo.adapter.rest.v1_0;

import ch.cern.todo.adapter.rest.v1_0.request.AddTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.request.UpdateTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.response.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.response.GetTaskCategoryResponse;
import ch.cern.todo.adapter.rest.v1_0.response.TaskCategoryPage;
import ch.cern.todo.core.application.TaskCategoryService;
import ch.cern.todo.core.application.command.dto.AddTaskCategoryCommand;
import ch.cern.todo.core.application.command.dto.DeleteTaskCategoryCommand;
import ch.cern.todo.core.application.command.dto.UpdateTaskCategoryCommand;
import ch.cern.todo.core.application.query.TaskCategoryProjection;
import ch.cern.todo.core.application.query.dto.CustomPage;
import ch.cern.todo.core.application.query.dto.SortDirection;
import ch.cern.todo.core.application.query.dto.TaskCategoryFilters;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static ch.cern.todo.adapter.rest.v1_0.RestConstant.VERSION_NUMBER_PATH;

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

    @GetMapping
    public ResponseEntity<TaskCategoryPage> get(@RequestParam("page") @NotNull final int page,
                                                @RequestParam(value = "size", defaultValue = "10") final int size,
                                                @RequestParam(value = "sort", defaultValue = "DESC") final SortDirection sort,
                                                @RequestParam(value = "name", required = false) final String name) {
        final CustomPage<TaskCategoryProjection> taskCategoriesPage = StringUtils.isNotBlank(name) ?
                taskCategoryService.getTaskCategoriesByName(new TaskCategoryFilters(name, page, size, sort)) :
                taskCategoryService.getTaskCategories(new TaskCategoryFilters(name, page, size, sort));

        return ResponseEntity.ok(new TaskCategoryPage(page, size, taskCategoriesPage.getTotalElements(), taskCategoriesPage.getTotalPages(),
                taskCategoriesPage.getElements().stream()
                .map(GetTaskCategoryResponse::from)
                .toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetTaskCategoryResponse> get(@PathVariable("id") final Long id) {
        return taskCategoryService.getTaskCategory(id)
                .map(taskCategory -> ResponseEntity.ok(GetTaskCategoryResponse.from(taskCategory)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") final Long id,
                                       @Valid @RequestBody UpdateTaskCategoryRequest updateTaskCategoryRequest) {
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
