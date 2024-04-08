package ch.cern.todo.it;

import ch.cern.todo.adapter.rest.v1_0.request.CommonPage;
import ch.cern.todo.adapter.rest.v1_0.request.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.task.category.request.AddTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.task.category.request.UpdateTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.task.category.response.GetTaskCategoryResponse;
import ch.cern.todo.it.config.TaskCategoryTestClient;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
        TaskCategoryTestClient.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
class TaskCategoryIT {

    @Autowired
    private TaskCategoryTestClient taskCategoryTestClient;

    @Autowired
    private Flyway flyway;

    @AfterEach
    public void cleanUp(){
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void addTaskCategories_checkIfAdded_updateOne_deleteNonUpdated_checkIfReturnedOneUpdated_thenDeleteAndCheckEmptyPage() {
        //add first category, get by ID
        final AddTaskCategoryRequest addTaskCategoryRequest = new AddTaskCategoryRequest("name1", "desc1");
        final GetTaskCategoryResponse expectedFirstResponse = new GetTaskCategoryResponse(1L, "name1", "desc1");
        final GetTaskCategoryResponse firstGetResponse = addCategoryAndCheckById(addTaskCategoryRequest, expectedFirstResponse);

        //add second category, get by ID
        final AddTaskCategoryRequest addSecondTaskCategoryRequest = new AddTaskCategoryRequest("name2", "desc1");
        final GetTaskCategoryResponse expectedSecondResponse = new GetTaskCategoryResponse(2L, "name2", "desc1");
        final GetTaskCategoryResponse secondGetResponse = addCategoryAndCheckById(addSecondTaskCategoryRequest, expectedSecondResponse);

        //get all and check
        final int firstPageNumber = 0;
        final CommonPage<GetTaskCategoryResponse> firstFetchTaskCategories = getTaskCategories(Optional.empty(), Optional.empty(), Optional.of(String.valueOf(firstPageNumber)), Optional.empty());
        assertEquals(List.of(secondGetResponse, firstGetResponse), firstFetchTaskCategories.getElements());
        assertEquals(2, firstFetchTaskCategories.getTotalElements());
        assertEquals(1, firstFetchTaskCategories.getTotalPages());
        assertEquals(10, firstFetchTaskCategories.getPageSize());
        assertEquals(firstPageNumber, firstFetchTaskCategories.getPageNumber());

        //remove element and check
        deleteTaskCategory(1L);
        assertNotFoundTaskCategory(1L);

        //update and verify get
        final UpdateTaskCategoryRequest updateTaskCategoryRequest = new UpdateTaskCategoryRequest("newName", "newDescription");
        final GetTaskCategoryResponse expectedUpdatedResponse = new GetTaskCategoryResponse(secondGetResponse.id(), "newName", "newDescription");
        patchTaskCategory(secondGetResponse.id(), updateTaskCategoryRequest);
        final GetTaskCategoryResponse updatedResponse = getTaskCategory(secondGetResponse.id(), expectedUpdatedResponse);

        //get all and verify one left with valid values
        final int pageNumber = 0;
        final int pageSize = 2;
        final CommonPage<GetTaskCategoryResponse> secondFetchTaskCategories = getTaskCategories(Optional.empty(), Optional.empty(), Optional.of(String.valueOf(pageNumber)), Optional.of(String.valueOf(pageSize)));
        assertEquals(List.of(updatedResponse), secondFetchTaskCategories.getElements());
        assertEquals(1, secondFetchTaskCategories.getTotalElements());
        assertEquals(1, secondFetchTaskCategories.getTotalPages());
        assertEquals(pageSize, secondFetchTaskCategories.getPageSize());
        assertEquals(pageNumber, secondFetchTaskCategories.getPageNumber());
    }

    @Test
    void addTaskCategoriesWithDuplicate_checkIfAdded_checkIfDuplicateAdditionFailed_displayListByNameAndCheckContainsOneElement() {
        //add first category, get by ID
        final AddTaskCategoryRequest addTaskCategoryRequest = new AddTaskCategoryRequest("name1", "desc1");
        final GetTaskCategoryResponse expectedFirstResponse = new GetTaskCategoryResponse(1L, "name1", "desc1");
        final GetTaskCategoryResponse firstGetResponse = addCategoryAndCheckById(addTaskCategoryRequest, expectedFirstResponse);

        //add category with same name but different description
        final AddTaskCategoryRequest addSecondTaskCategoryRequest = new AddTaskCategoryRequest("name1", "desc2");
        addCategoryAndCheckConflict(addSecondTaskCategoryRequest);

        //get all and check
        final int firstPageNumber = 0;
        final CommonPage<GetTaskCategoryResponse> firstFetchTaskCategories = getTaskCategories(Optional.of(addTaskCategoryRequest.name()), Optional.of("ASC"), Optional.of(String.valueOf(firstPageNumber)), Optional.empty());
        assertEquals(List.of(firstGetResponse), firstFetchTaskCategories.getElements());
        assertEquals(1, firstFetchTaskCategories.getTotalElements());
        assertEquals(1, firstFetchTaskCategories.getTotalPages());
        assertEquals(10, firstFetchTaskCategories.getPageSize());
        assertEquals(firstPageNumber, firstFetchTaskCategories.getPageNumber());
    }

    private GetTaskCategoryResponse addCategoryAndCheckById(final AddTaskCategoryRequest request, final GetTaskCategoryResponse expectedResponse) {
        final ResponseEntity<GenericAddResourceResponse> createdIdResponse = taskCategoryTestClient.create(request);
        assertEquals(201, createdIdResponse.getStatusCode().value());
        final Long createdId = createdIdResponse.getBody().id();
        return getTaskCategory(createdId, expectedResponse);
    }

    private void addCategoryAndCheckConflict(final AddTaskCategoryRequest request) {
        final ResponseEntity<GenericAddResourceResponse> createdIdResponse = taskCategoryTestClient.create(request);
        assertEquals(409, createdIdResponse.getStatusCode().value());
    }

    private GetTaskCategoryResponse getTaskCategory(final Long id, final GetTaskCategoryResponse expectedResponse) {
        final ResponseEntity<GetTaskCategoryResponse> response = taskCategoryTestClient.getById(id);
        assertEquals(200, response.getStatusCode().value());
        final GetTaskCategoryResponse getTaskCategoryResponse = response.getBody();
        assertEquals(expectedResponse, getTaskCategoryResponse);
        return getTaskCategoryResponse;
    }

    private CommonPage<GetTaskCategoryResponse> getTaskCategories(final Optional<String> name, final Optional<String> sortDirection, final Optional<String> page, final Optional<String> pageSize) {
        final ResponseEntity<CommonPage<GetTaskCategoryResponse>> taskCategoriesResponse =
                taskCategoryTestClient.get(name, sortDirection, page, pageSize);
        assertEquals(200, taskCategoriesResponse.getStatusCode().value());
        return taskCategoriesResponse.getBody();
    }

    private void assertNotFoundTaskCategory(final Long id) {
        final ResponseEntity<GetTaskCategoryResponse> response = taskCategoryTestClient.getById(id);
        assertEquals(404, response.getStatusCode().value());
    }

    private void deleteTaskCategory(final Long id) {
        final ResponseEntity<Void> response = taskCategoryTestClient.delete(id);
        assertEquals(204, response.getStatusCode().value());
    }

    private void patchTaskCategory(final Long id, final UpdateTaskCategoryRequest updateTaskCategoryRequest) {
        final ResponseEntity<Void> response = taskCategoryTestClient.update(id, updateTaskCategoryRequest);
        assertEquals(204, response.getStatusCode().value());
    }
}
