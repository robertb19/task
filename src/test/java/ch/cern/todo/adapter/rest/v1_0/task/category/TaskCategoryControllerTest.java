package ch.cern.todo.adapter.rest.v1_0.task.category;

import ch.cern.todo.adapter.rest.v1_0.request.CommonPage;
import ch.cern.todo.adapter.rest.v1_0.request.ErrorResponse;
import ch.cern.todo.adapter.rest.v1_0.request.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.request.ListErrorResponse;
import ch.cern.todo.adapter.rest.v1_0.task.category.request.AddTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.task.category.request.UpdateTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.task.category.response.*;
import ch.cern.todo.core.application.TaskCategoryService;
import ch.cern.todo.core.application.command.dto.AddTaskCategoryCommand;
import ch.cern.todo.core.application.command.dto.DeleteTaskCategoryCommand;
import ch.cern.todo.core.application.command.dto.UpdateTaskCategoryCommand;
import ch.cern.todo.core.application.exception.DuplicateTaskCategoryException;
import ch.cern.todo.core.application.exception.TaskCategoryNotFoundException;
import ch.cern.todo.core.application.exception.TaskRecordsMappedException;
import ch.cern.todo.core.application.query.dto.TaskCategoryProjection;
import ch.cern.todo.core.application.query.dto.CustomPage;
import ch.cern.todo.core.application.query.dto.SortDirection;
import ch.cern.todo.core.application.query.dto.TaskCategoryFilters;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static util.TestUtils.generateRandomCharacterString;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TaskCategoryController.class)
class TaskCategoryControllerTest {

    @MockBean
    private TaskCategoryService taskCategoryService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;

    //cannot use standalone setup as then validations are not working (due to validations post processor)
    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @ParameterizedTest
    @MethodSource("provideRequestsAndResponsesForCreate")
    void givenAddTaskCategoryRequest_whenCreate_returnAppropriateResponses(final AddTaskCategoryRequest addTaskCategoryRequest,
                                                                           final int statusCode,
                                                                           final Object response) throws Exception {
        //given
        final AddTaskCategoryCommand addTaskCategoryCommand = new AddTaskCategoryCommand(addTaskCategoryRequest.name(), addTaskCategoryRequest.description());

        //when
        switch (statusCode) {
            case 201:
                when(taskCategoryService.addTaskCategory(addTaskCategoryCommand))
                        .thenReturn(((GenericAddResourceResponse) response).id());
                break;
            case 409:
                when(taskCategoryService.addTaskCategory(addTaskCategoryCommand)).thenThrow(new DuplicateTaskCategoryException("Task category already exists"));
                break;
            default:
                break;
        }

        final RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1.0/categories")
                .content(objectMapper.writeValueAsString(addTaskCategoryRequest))
                .contentType(MediaType.APPLICATION_JSON);
        final MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        //then
        assertEquals(statusCode, result.getResponse().getStatus());
        if (statusCode == 400) {
            assertThat(
                    objectMapper.readValue(result.getResponse().getContentAsString(), ListErrorResponse.class).messages(),
                    containsInAnyOrder(((ListErrorResponse) response).messages().toArray())
            );
        } else {
            assertEquals(objectMapper.writeValueAsString(response), result.getResponse().getContentAsString());
        }
    }

    @ParameterizedTest
    @MethodSource("provideRequestsAndResponsesForGet")
    void givenGetTaskCategoriesRequest_whenGet_returnAppropriateResponses(final MultiValueMap<String, String> multiValueMap,
                                                                          final TaskCategoryFilters taskCategoryFilters,
                                                                          final CustomPage<TaskCategoryProjection> taskCategoryProjection,
                                                                          final int statusCode,
                                                                          final Object response) throws Exception {
        //when
        if (statusCode == 200) {
            if (taskCategoryFilters.name() != null) {
                when(taskCategoryService.getTaskCategoriesByName(taskCategoryFilters)).thenReturn(taskCategoryProjection);
            } else {
                when(taskCategoryService.getTaskCategories(taskCategoryFilters)).thenReturn(taskCategoryProjection);
            }
        }

        final RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1.0/categories")
                .queryParams(multiValueMap);

        final MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        //then
        assertEquals(statusCode, result.getResponse().getStatus());
        assertEquals(objectMapper.writeValueAsString(response), result.getResponse().getContentAsString());
    }

    @ParameterizedTest
    @MethodSource("provideRequestsAndResponsesForGetById")
    void givenId_whenGetById_returnAppropriateResponses(final Long id,
                                                        final TaskCategoryProjection taskCategoryProjection,
                                                        final int statusCode,
                                                        final Object response) throws Exception {
        //when
        if (statusCode == 200) {
            when(taskCategoryService.getTaskCategory(id)).thenReturn(Optional.of(taskCategoryProjection));
        }

        final RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1.0/categories/" + id);

        final MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        //then
        assertEquals(statusCode, result.getResponse().getStatus());
        if (statusCode == 200) {
            assertEquals(objectMapper.writeValueAsString(response), result.getResponse().getContentAsString());
        }
    }

    @ParameterizedTest
    @MethodSource("provideRequestsAndResponsesForUpdate")
    void givenId_whenUpdate_returnAppropriateResponses(final Long id,
                                                       final UpdateTaskCategoryRequest updateTaskCategoryRequest,
                                                       final int statusCode,
                                                       final Object response) throws Exception {
        final RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1.0/categories/" + id)
                .content(objectMapper.writeValueAsString(updateTaskCategoryRequest))
                .contentType(MediaType.APPLICATION_JSON);

        switch (statusCode) {
            case 404:
                doThrow(new TaskCategoryNotFoundException()).when(taskCategoryService)
                        .updateTaskCategory(new UpdateTaskCategoryCommand(id, updateTaskCategoryRequest.name(), updateTaskCategoryRequest.description()));
                break;
            case 409:
                doThrow(new DuplicateTaskCategoryException("Task category already exists")).when(taskCategoryService)
                        .updateTaskCategory(new UpdateTaskCategoryCommand(id, updateTaskCategoryRequest.name(), updateTaskCategoryRequest.description()));
                break;
            default:
                break;
        }

        final MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        //then
        assertEquals(statusCode, result.getResponse().getStatus());
        if (StringUtils.isNotBlank(result.getResponse().getContentAsString())) {
            switch (statusCode) {
                case 400:
                    assertThat(
                            objectMapper.readValue(result.getResponse().getContentAsString(), ListErrorResponse.class).messages(),
                            containsInAnyOrder(((ListErrorResponse) response).messages().toArray())
                    );
                    break;
                case 409:
                    assertEquals(objectMapper.writeValueAsString(response), result.getResponse().getContentAsString());
                    break;
                default:
                    break;
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideRequestsAndResponsesForDelete")
    void givenId_whenDelete_returnAppropriateResponses(final Long id,
                                                       final int statusCode,
                                                       final Object response) throws Exception {
        switch (statusCode) {
            case 409:
                doThrow(new TaskRecordsMappedException("Unable to delete as tasks are mapped to the category")).when(taskCategoryService).deleteTaskCategory(new DeleteTaskCategoryCommand(id));
                break;
            default:
                break;
        }

        final RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1.0/categories/" + id);
        final MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        //then
        assertEquals(statusCode, result.getResponse().getStatus());
        if(response != null) {
            assertEquals(objectMapper.writeValueAsString(response), result.getResponse().getContentAsString());
        }
    }

    private static Stream<Arguments> provideRequestsAndResponsesForCreate() {
        final AddTaskCategoryRequest validRequest = new AddTaskCategoryRequest("name", "description");
        final GenericAddResourceResponse validResponse = new GenericAddResourceResponse(1L);

        final AddTaskCategoryRequest invalidRequest = new AddTaskCategoryRequest(generateRandomCharacterString(101),
                generateRandomCharacterString(501));
        final ListErrorResponse invalidResponse = new ListErrorResponse(
                Set.of("Name must be smaller than 100", "Description must be smaller than 500"));

        final ErrorResponse duplicateResponse = new ErrorResponse("Task category already exists");

        return Stream.of(
                Arguments.of(validRequest, 201, validResponse),
                Arguments.of(invalidRequest, 400, invalidResponse),
                Arguments.of(validRequest, 409, duplicateResponse)
        );
    }

    private static Stream<Arguments> provideRequestsAndResponsesForGet() {
        //params without name
        final MultiValueMap<String, String> parametersWithoutName = new MultiValueMapAdapter<>(new HashMap<>() {{
            put("page", List.of("0"));
            put("size", List.of("5"));
            put("sort", List.of("ASC"));
        }});
        final TaskCategoryFilters taskCategoryFilters = new TaskCategoryFilters(null, 0, 5, SortDirection.ASC);
        final CustomPage<TaskCategoryProjection> taskCategoryProjection = new CustomPage<>(
                List.of(
                        new TaskCategoryProjection(1L, "name1", "description1"),
                        new TaskCategoryProjection(2L, "name2", "description2")
                ), 2, 1
        );
        final CommonPage<GetTaskCategoryResponse> validResponse = new CommonPage<>(0, 5, 2, 1,
                List.of(
                        new GetTaskCategoryResponse(1L, "name1", "description1"),
                        new GetTaskCategoryResponse(2L, "name2", "description2")
                ));
        //params without name end

        //params with name and page only
        final MultiValueMap<String, String> parametersWithName = new MultiValueMapAdapter<>(new HashMap<>() {{
            put("page", List.of("0"));
            put("name", List.of("name"));
        }});
        final TaskCategoryFilters taskCategoryFiltersWithName = new TaskCategoryFilters("name", 0, 10, SortDirection.DESC);
        final CustomPage<TaskCategoryProjection> singleTaskCategoryProjection = new CustomPage<>(
                List.of(
                        new TaskCategoryProjection(1L, "name", "description")
                ), 1, 1
        );
        final CommonPage<GetTaskCategoryResponse> validSingleItemResponse = new CommonPage<>(0, 10, 1, 1,
                List.of(
                        new GetTaskCategoryResponse(1L, "name", "description")
                ));
        //params with name and page only end

        //params with empty page
        final MultiValueMap<String, String> parametersWithNameForEmptyPage = new MultiValueMapAdapter<>(new HashMap<>() {{
            put("page", List.of("0"));
            put("name", List.of("name"));
        }});
        final TaskCategoryFilters taskCategoryFiltersForEmptyPage = new TaskCategoryFilters("name", 0, 10, SortDirection.DESC);
        final CustomPage<TaskCategoryProjection> emptyTaskCategoryProjection = new CustomPage<>(Collections.emptyList(), 0, 0);
        final CommonPage<GetTaskCategoryResponse> emptyPageResponse = new CommonPage<>(0, 10, 0, 0, Collections.emptyList());
        //params with empty page end

        //invalid params as page is missing
        final MultiValueMap<String, String> pageMissing = new MultiValueMapAdapter<>(new HashMap<>() {{
            put("size", List.of("5"));
            put("sort", List.of("ASC"));
            put("name", List.of("name"));
        }});
        final ListErrorResponse invalidResponse = new ListErrorResponse(
                Set.of("Page must be specified as request param"));
        //invalid params as page is missing end

        return Stream.of(
                Arguments.of(parametersWithoutName, taskCategoryFilters, taskCategoryProjection, 200, validResponse),
                Arguments.of(parametersWithName, taskCategoryFiltersWithName, singleTaskCategoryProjection, 200, validSingleItemResponse),
                Arguments.of(parametersWithNameForEmptyPage, taskCategoryFiltersForEmptyPage, emptyTaskCategoryProjection, 200, emptyPageResponse),
                Arguments.of(pageMissing, null, null, 400, invalidResponse)
        );
    }

    private static Stream<Arguments> provideRequestsAndResponsesForGetById() {
        final TaskCategoryProjection validProjection = new TaskCategoryProjection(1L, "name", "description");
        final GetTaskCategoryResponse validResponse = new GetTaskCategoryResponse(1L, "name", "description");

        return Stream.of(
                Arguments.of(1L, validProjection, 200, validResponse),
                Arguments.of(null, null, 400, null),
                Arguments.of(1L, null, 404, null)
        );
    }

    private static Stream<Arguments> provideRequestsAndResponsesForUpdate() {
        final UpdateTaskCategoryRequest validRequest = new UpdateTaskCategoryRequest("name", "description");

        final UpdateTaskCategoryRequest invalidRequest = new UpdateTaskCategoryRequest(generateRandomCharacterString(101), generateRandomCharacterString(501));
        final ListErrorResponse invalidResponse = new ListErrorResponse(
                Set.of("Name must be smaller than 100", "Description must be smaller than 500"));

        final ErrorResponse errorResponse = new ErrorResponse("Task category already exists");

        return Stream.of(
                Arguments.of(1L, validRequest, 204, null),
                Arguments.of(null, validRequest, 400, null),
                Arguments.of(1L, invalidRequest, 400, invalidResponse),
                Arguments.of(1L, validRequest, 404, null),
                Arguments.of(1L, validRequest, 409, errorResponse)
        );
    }

    private static Stream<Arguments> provideRequestsAndResponsesForDelete() {
        final ErrorResponse errorResponse = new ErrorResponse("Unable to delete as tasks are mapped to the category");

        return Stream.of(
                Arguments.of(1L, 204, null),
                Arguments.of(null, 400, null),
                Arguments.of(1L, 409, errorResponse)
        );
    }
}
