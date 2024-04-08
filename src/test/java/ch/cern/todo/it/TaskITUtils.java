package ch.cern.todo.it;

import ch.cern.todo.adapter.rest.v1_0.request.CommonPage;
import ch.cern.todo.adapter.rest.v1_0.request.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.task.category.request.AddTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.task.category.request.UpdateTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.task.category.response.GetTaskCategoryResponse;
import ch.cern.todo.it.config.TaskCategoryTestClient;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TaskITUtils {

    static GetTaskCategoryResponse addCategoryAndCheckById(final TaskCategoryTestClient taskCategoryTestClient, final AddTaskCategoryRequest request, final GetTaskCategoryResponse expectedResponse) {
        final ResponseEntity<GenericAddResourceResponse> createdIdResponse = taskCategoryTestClient.create(request);
        assertEquals(201, createdIdResponse.getStatusCode().value());
        final Long createdId = createdIdResponse.getBody().id();
        return getTaskCategory(taskCategoryTestClient, createdId, expectedResponse);
    }

    static void addCategoryAndCheckConflict(final TaskCategoryTestClient taskCategoryTestClient, final AddTaskCategoryRequest request) {
        final ResponseEntity<GenericAddResourceResponse> createdIdResponse = taskCategoryTestClient.create(request);
        assertEquals(409, createdIdResponse.getStatusCode().value());
    }

    static GetTaskCategoryResponse getTaskCategory(final TaskCategoryTestClient taskCategoryTestClient, final Long id, final GetTaskCategoryResponse expectedResponse) {
        final ResponseEntity<GetTaskCategoryResponse> response = taskCategoryTestClient.getById(id);
        assertEquals(200, response.getStatusCode().value());
        final GetTaskCategoryResponse getTaskCategoryResponse = response.getBody();
        assertEquals(expectedResponse, getTaskCategoryResponse);
        return getTaskCategoryResponse;
    }

    static CommonPage<GetTaskCategoryResponse> getTaskCategories(final TaskCategoryTestClient taskCategoryTestClient, final Optional<String> name, final Optional<String> sortDirection, final Optional<String> page, final Optional<String> pageSize) {
        final ResponseEntity<CommonPage<GetTaskCategoryResponse>> taskCategoriesResponse =
                taskCategoryTestClient.get(name, sortDirection, page, pageSize);
        assertEquals(200, taskCategoriesResponse.getStatusCode().value());
        return taskCategoriesResponse.getBody();
    }

    static void assertNotFoundTaskCategory(final TaskCategoryTestClient taskCategoryTestClient, final Long id) {
        final ResponseEntity<GetTaskCategoryResponse> response = taskCategoryTestClient.getById(id);
        assertEquals(404, response.getStatusCode().value());
    }

    static void deleteTaskCategory(final TaskCategoryTestClient taskCategoryTestClient, final Long id) {
        final ResponseEntity<Void> response = taskCategoryTestClient.delete(id);
        assertEquals(204, response.getStatusCode().value());
        assertNotFoundTaskCategory(taskCategoryTestClient, 1L);
    }

    static void patchTaskCategory(final TaskCategoryTestClient taskCategoryTestClient, final Long id, final UpdateTaskCategoryRequest updateTaskCategoryRequest) {
        final ResponseEntity<Void> response = taskCategoryTestClient.update(id, updateTaskCategoryRequest);
        assertEquals(204, response.getStatusCode().value());
    }
    
}
