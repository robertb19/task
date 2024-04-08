package ch.cern.todo.it;

import ch.cern.todo.adapter.rest.v1_0.request.CommonPage;
import ch.cern.todo.adapter.rest.v1_0.request.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.task.category.request.AddTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.task.category.response.GetTaskCategoryResponse;
import ch.cern.todo.adapter.rest.v1_0.task.request.AddTaskRequest;
import ch.cern.todo.adapter.rest.v1_0.task.request.UpdateTaskRequest;
import ch.cern.todo.adapter.rest.v1_0.task.response.GetTaskResponse;
import ch.cern.todo.it.config.ITTestConfiguration;
import ch.cern.todo.it.config.TaskCategoryTestClient;
import ch.cern.todo.it.config.TaskTestClient;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static ch.cern.todo.it.TaskITUtils.*;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
        ITTestConfiguration.class,
        TaskCategoryTestClient.class,
        TaskTestClient.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
class TaskIT {

    @Autowired
    private TaskCategoryTestClient taskCategoryTestClient;

    @Autowired
    private TaskTestClient taskTestClient;

    @Autowired
    private Flyway flyway;

    @Autowired
    private Clock clock;

    @AfterEach
    public void cleanUp() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void addTasksToDifferentCategories_performDifferentFilteringOperations_deleteAndPatch_getAll() {
        //add first category, get by ID
        final AddTaskCategoryRequest addTaskCategoryRequest = new AddTaskCategoryRequest("name1", "desc1");
        final GetTaskCategoryResponse expectedCategoryResponse = new GetTaskCategoryResponse(1L, "name1", "desc1");
        final GetTaskCategoryResponse firstCategoryGetResponse = addCategoryAndCheckById(taskCategoryTestClient, addTaskCategoryRequest, expectedCategoryResponse);

        //add second category, get by ID
        final AddTaskCategoryRequest addSecondTaskCategoryRequest = new AddTaskCategoryRequest("name2", "desc1");
        final GetTaskCategoryResponse expectedSecondCategoryResponse = new GetTaskCategoryResponse(2L, "name2", "desc1");
        final GetTaskCategoryResponse secondCategoryGetResponse = addCategoryAndCheckById(taskCategoryTestClient, addSecondTaskCategoryRequest, expectedSecondCategoryResponse);

        //add tasks
        final Instant tenMinutesFromNow = Instant.now().plusSeconds(600);
        final Instant oneHourFromNow = Instant.now().plusSeconds(3600);
        final Instant betweenTenMAndOneH = Instant.now().plusSeconds(1000);
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        final ZonedDateTime tenMinutesFromNowAsZdt = ZonedDateTime.parse(tenMinutesFromNow.atZone(clock.getZone()).format(dateTimeFormatter), dateTimeFormatter);
        final ZonedDateTime oneHourFromNowAsZdt = ZonedDateTime.parse(oneHourFromNow.atZone(clock.getZone()).format(dateTimeFormatter), dateTimeFormatter);

        final AddTaskRequest addTaskRequest = new AddTaskRequest("name1", "desc1", tenMinutesFromNow, firstCategoryGetResponse.id());
        final AddTaskRequest addSecondTaskRequest = new AddTaskRequest("name1", "desc1", tenMinutesFromNow, firstCategoryGetResponse.id());
        final AddTaskRequest addThirdRequest = new AddTaskRequest("name1", "desc1", oneHourFromNow, secondCategoryGetResponse.id());
        final AddTaskRequest requestForInexistentCategoryId = new AddTaskRequest("name1", "desc1", tenMinutesFromNow, 10000L);
        final GetTaskResponse expectedFirstResponse = new GetTaskResponse(1L, "name1", "desc1", tenMinutesFromNowAsZdt, firstCategoryGetResponse);
        final GetTaskResponse expectedSecondResponse = new GetTaskResponse(2L, "name1", "desc1", tenMinutesFromNowAsZdt, firstCategoryGetResponse);
        final GetTaskResponse expectedThirdResponse = new GetTaskResponse(3L ,"name1", "desc1", oneHourFromNowAsZdt, secondCategoryGetResponse);

        final GetTaskResponse addTaskResponse = addTaskAndCheckById(addTaskRequest, expectedFirstResponse);
        final GetTaskResponse addSecondTaskResponse = addTaskAndCheckById(addSecondTaskRequest, expectedSecondResponse);
        addTaskAndCheckById(addThirdRequest, expectedThirdResponse);
        addForInexistentTask(requestForInexistentCategoryId);

        //get all tasks
        final int firstPageNumber = 0;
        final CommonPage<GetTaskResponse> allTasks = getTasksByFilters(empty(), empty(), Optional.of(String.valueOf(firstPageNumber)), empty(), empty(), empty(), empty());
        assertEquals(List.of(expectedThirdResponse, expectedSecondResponse, expectedFirstResponse), allTasks.getElements());
        assertEquals(3, allTasks.getTotalElements());
        assertEquals(1, allTasks.getTotalPages());
        assertEquals(10, allTasks.getPageSize());
        assertEquals(firstPageNumber, allTasks.getPageNumber());

        //get filtered tasks by category id
        final CommonPage<GetTaskResponse> firstCategoryTasks = getTasksByFilters(empty(), Optional.of("ASC"), Optional.of(String.valueOf(firstPageNumber)), empty(), empty(), empty(), Optional.of(String.valueOf(firstCategoryGetResponse.id())));
        assertEquals(List.of(expectedFirstResponse, expectedSecondResponse), firstCategoryTasks.getElements());
        assertEquals(2, firstCategoryTasks.getTotalElements());
        assertEquals(1, firstCategoryTasks.getTotalPages());
        assertEquals(10, firstCategoryTasks.getPageSize());
        assertEquals(firstPageNumber, firstCategoryTasks.getPageNumber());

        //get filtered tasks by deadline after
        final long betweenTenMAndOneHAsEpochSeconds = betweenTenMAndOneH.toEpochMilli()/1000;
        final CommonPage<GetTaskResponse> deadlineBeforeTasks = getTasksByFilters(empty(), empty(), Optional.of(String.valueOf(firstPageNumber)), empty(), Optional.of(String.valueOf(betweenTenMAndOneHAsEpochSeconds)), empty(), empty());
        assertEquals(List.of(expectedSecondResponse, expectedFirstResponse), deadlineBeforeTasks.getElements());
        assertEquals(2, deadlineBeforeTasks.getTotalElements());
        assertEquals(1, deadlineBeforeTasks.getTotalPages());
        assertEquals(10, deadlineBeforeTasks.getPageSize());
        assertEquals(firstPageNumber, deadlineBeforeTasks.getPageNumber());

        //get filtered tasks by deadline before
        final int pageSize = 1;
        final CommonPage<GetTaskResponse> deadlineAfterTasks = getTasksByFilters(empty(), empty(), Optional.of(String.valueOf(firstPageNumber)), Optional.of(String.valueOf(pageSize)), Optional.of(String.valueOf(betweenTenMAndOneHAsEpochSeconds)), Optional.of("BEFORE"), empty());
        assertEquals(List.of(expectedThirdResponse), deadlineAfterTasks.getElements());
        assertEquals(1, deadlineAfterTasks.getTotalElements());
        assertEquals(1, deadlineAfterTasks.getTotalPages());
        assertEquals(1, deadlineAfterTasks.getPageSize());
        assertEquals(firstPageNumber, deadlineAfterTasks.getPageNumber());

        //delete and update task
        deleteTask(addSecondTaskResponse.id());
        final UpdateTaskRequest updateTaskRequest = new UpdateTaskRequest("newName", "newDesc", oneHourFromNow, secondCategoryGetResponse.id());
        patchTask(addTaskResponse.id(), updateTaskRequest);
        final GetTaskResponse expectedUpdatedTaskResponse = new GetTaskResponse(addTaskResponse.id(), updateTaskRequest.name(), updateTaskRequest.description(), oneHourFromNowAsZdt, secondCategoryGetResponse);
        final GetTaskResponse updatedTaskResponse = getTask(addTaskResponse.id(), expectedUpdatedTaskResponse);

        //verify complete lists after update and delete
        final CommonPage<GetTaskResponse> postDeleteAndUpdateTasks = getTasksByFilters(empty(), empty(), Optional.of(String.valueOf(firstPageNumber)), empty(),empty(), empty(), empty());
        assertEquals(List.of(expectedThirdResponse, updatedTaskResponse), postDeleteAndUpdateTasks.getElements());
        assertEquals(2, postDeleteAndUpdateTasks.getTotalElements());
        assertEquals(1, postDeleteAndUpdateTasks.getTotalPages());
        assertEquals(10, postDeleteAndUpdateTasks.getPageSize());
        assertEquals(firstPageNumber, postDeleteAndUpdateTasks.getPageNumber());
    }

    private GetTaskResponse addTaskAndCheckById(final AddTaskRequest request, final GetTaskResponse expectedResponse) {
        final ResponseEntity<GenericAddResourceResponse> createdIdResponse = taskTestClient.create(request);
        assertEquals(201, createdIdResponse.getStatusCode().value());
        final Long createdId = createdIdResponse.getBody().id();
        return getTask(createdId, expectedResponse);
    }

    private void addForInexistentTask(final AddTaskRequest request) {
        final ResponseEntity<GenericAddResourceResponse> createdIdResponse = taskTestClient.create(request);
        assertEquals(400, createdIdResponse.getStatusCode().value());
    }

    private GetTaskResponse getTask(final Long id, final GetTaskResponse expectedResponse) {
        final ResponseEntity<GetTaskResponse> response = taskTestClient.getById(id);
        assertEquals(200, response.getStatusCode().value());
        final GetTaskResponse getTaskCategoryResponse = response.getBody();
        assertEquals(expectedResponse, getTaskCategoryResponse);
        return getTaskCategoryResponse;
    }

    private CommonPage<GetTaskResponse> getTasksByFilters(final Optional<String> name,
                                                          final Optional<String> sortDirection,
                                                          final Optional<String> page,
                                                          final Optional<String> pageSize,
                                                          final Optional<String> deadline,
                                                          final Optional<String> deadlineMode,
                                                          final Optional<String> categoryId) {
        final ResponseEntity<CommonPage<GetTaskResponse>> response = taskTestClient.get(name, sortDirection, page, pageSize, deadline, deadlineMode,categoryId);
        assertEquals(200, response.getStatusCode().value());
        return response.getBody();
    }

    private void deleteTask(final Long id) {
        final ResponseEntity<Void> response = taskTestClient.delete(id);
        assertEquals(204, response.getStatusCode().value());
        assertNotFoundTask(id);
    }

    private void patchTask(final Long id, final UpdateTaskRequest updateTaskRequest) {
        final ResponseEntity<Void> response = taskTestClient.update(id, updateTaskRequest);
        assertEquals(204, response.getStatusCode().value());
    }

    private void assertNotFoundTask(final Long id) {
        final ResponseEntity<GetTaskResponse> response = taskTestClient.getById(id);
        assertEquals(404, response.getStatusCode().value());
    }
}
