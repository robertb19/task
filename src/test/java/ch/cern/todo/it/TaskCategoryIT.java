package ch.cern.todo.it;

import ch.cern.todo.adapter.rest.v1_0.request.CommonPage;
import ch.cern.todo.adapter.rest.v1_0.task.category.request.AddTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.task.category.request.UpdateTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.task.category.response.GetTaskCategoryResponse;
import ch.cern.todo.it.config.ITTestConfiguration;
import ch.cern.todo.it.config.TaskCategoryTestClient;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static ch.cern.todo.it.TaskITUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
        ITTestConfiguration.class,
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
    public void cleanUp() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void addTaskCategories_checkIfAdded_updateOne_deleteNonUpdated_checkIfReturnedOneUpdated_thenDeleteAndCheckEmptyPage() {
        //add first category, get by ID
        final AddTaskCategoryRequest addTaskCategoryRequest = new AddTaskCategoryRequest("name1", "desc1");
        final GetTaskCategoryResponse expectedFirstResponse = new GetTaskCategoryResponse(1L, "name1", "desc1");
        final GetTaskCategoryResponse firstGetResponse = addCategoryAndCheckById(taskCategoryTestClient, addTaskCategoryRequest, expectedFirstResponse);

        //add second category, get by ID
        final AddTaskCategoryRequest addSecondTaskCategoryRequest = new AddTaskCategoryRequest("name2", "desc1");
        final GetTaskCategoryResponse expectedSecondResponse = new GetTaskCategoryResponse(2L, "name2", "desc1");
        final GetTaskCategoryResponse secondGetResponse = addCategoryAndCheckById(taskCategoryTestClient, addSecondTaskCategoryRequest, expectedSecondResponse);

        //get all and check
        final int firstPageNumber = 0;
        final CommonPage<GetTaskCategoryResponse> firstFetchTaskCategories = getTaskCategories(taskCategoryTestClient, Optional.empty(), Optional.empty(), Optional.of(String.valueOf(firstPageNumber)), Optional.empty());
        assertEquals(List.of(secondGetResponse, firstGetResponse), firstFetchTaskCategories.getElements());
        assertEquals(2, firstFetchTaskCategories.getTotalElements());
        assertEquals(1, firstFetchTaskCategories.getTotalPages());
        assertEquals(10, firstFetchTaskCategories.getPageSize());
        assertEquals(firstPageNumber, firstFetchTaskCategories.getPageNumber());

        //remove element and check
        deleteTaskCategory(taskCategoryTestClient, 1L);

        //update and verify get
        final UpdateTaskCategoryRequest updateTaskCategoryRequest = new UpdateTaskCategoryRequest("newName", "newDescription");
        final GetTaskCategoryResponse expectedUpdatedResponse = new GetTaskCategoryResponse(secondGetResponse.id(), "newName", "newDescription");
        patchTaskCategory(taskCategoryTestClient, secondGetResponse.id(), updateTaskCategoryRequest);
        final GetTaskCategoryResponse updatedResponse = getTaskCategory(taskCategoryTestClient, secondGetResponse.id(), expectedUpdatedResponse);

        //get all and verify one left with valid values
        final int pageNumber = 0;
        final int pageSize = 2;
        final CommonPage<GetTaskCategoryResponse> secondFetchTaskCategories = getTaskCategories(taskCategoryTestClient, Optional.empty(), Optional.empty(), Optional.of(String.valueOf(pageNumber)), Optional.of(String.valueOf(pageSize)));
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
        final GetTaskCategoryResponse firstGetResponse = addCategoryAndCheckById(taskCategoryTestClient, addTaskCategoryRequest, expectedFirstResponse);

        //add category with same name but different description
        final AddTaskCategoryRequest addSecondTaskCategoryRequest = new AddTaskCategoryRequest("name1", "desc2");
        addCategoryAndCheckConflict(taskCategoryTestClient, addSecondTaskCategoryRequest);

        //get all and check
        final int firstPageNumber = 0;
        final CommonPage<GetTaskCategoryResponse> firstFetchTaskCategories = getTaskCategories(taskCategoryTestClient, Optional.of(addTaskCategoryRequest.name()), Optional.of("ASC"), Optional.of(String.valueOf(firstPageNumber)), Optional.empty());
        assertEquals(List.of(firstGetResponse), firstFetchTaskCategories.getElements());
        assertEquals(1, firstFetchTaskCategories.getTotalElements());
        assertEquals(1, firstFetchTaskCategories.getTotalPages());
        assertEquals(10, firstFetchTaskCategories.getPageSize());
        assertEquals(firstPageNumber, firstFetchTaskCategories.getPageNumber());
    }

}
