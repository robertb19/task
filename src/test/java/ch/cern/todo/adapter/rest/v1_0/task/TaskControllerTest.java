package ch.cern.todo.adapter.rest.v1_0.task;

import ch.cern.todo.adapter.rest.v1_0.request.CommonPage;
import ch.cern.todo.adapter.rest.v1_0.request.ErrorResponse;
import ch.cern.todo.adapter.rest.v1_0.request.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.request.ListErrorResponse;
import ch.cern.todo.adapter.rest.v1_0.task.category.response.GetTaskCategoryResponse;
import ch.cern.todo.adapter.rest.v1_0.task.request.AddTaskRequest;
import ch.cern.todo.adapter.rest.v1_0.task.request.UpdateTaskRequest;
import ch.cern.todo.adapter.rest.v1_0.task.response.GetTaskResponse;
import ch.cern.todo.core.application.TaskService;
import ch.cern.todo.core.application.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.command.dto.UpdateTaskCommand;
import ch.cern.todo.core.application.exception.TaskCategoryNotFoundException;
import ch.cern.todo.core.application.exception.TaskNotFoundException;
import ch.cern.todo.core.application.query.dto.*;
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
import util.TestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static util.TestUtils.generateRandomCharacterString;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @MockBean
    private TaskService taskService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = TestUtils.objectMapperWithTimeModule();

    @Autowired
    private Clock clock;

    @Autowired
    private WebApplicationContext webApplicationContext;

    //cannot use standalone setup as then validations are not working (due to validations post processor)
    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @ParameterizedTest
    @MethodSource("provideRequestsAndResponsesForCreate")
    void givenAddTaskRequest_whenCreate_returnAppropriateResponses(final AddTaskRequest addTaskRequest,
                                                                           final int statusCode,
                                                                           final Object response,
                                                                           final boolean isThrowingExceptionInController) throws Exception {
        //when
        switch (statusCode) {
            case 201:
                final AddTaskCommand addTaskCommand = toAddTaskCommand(addTaskRequest);
                when(taskService.addTask(addTaskCommand))
                        .thenReturn(((GenericAddResourceResponse) response).id());
                break;
            case 400:
                if(isThrowingExceptionInController) {
                    final AddTaskCommand notFoundCategoryIdCommand = toAddTaskCommand(addTaskRequest);
                    when(taskService.addTask(notFoundCategoryIdCommand)).thenThrow(new TaskCategoryNotFoundException());
                }
                break;
            default:
                break;
        }

        final RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1.0/tasks")
                .content(objectMapper.writeValueAsString(addTaskRequest))
                .contentType(MediaType.APPLICATION_JSON);
        final MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        //then
        assertEquals(statusCode, result.getResponse().getStatus());
        if (statusCode == 400) {
            if(isThrowingExceptionInController) {
                assertEquals(objectMapper.writeValueAsString(response), result.getResponse().getContentAsString());
            } else {
                assertThat(
                        objectMapper.readValue(result.getResponse().getContentAsString(), ListErrorResponse.class).messages(),
                        containsInAnyOrder(((ListErrorResponse) response).messages().toArray())
                );
            }
        } else {
            assertEquals(objectMapper.writeValueAsString(response), result.getResponse().getContentAsString());
        }
    }

    @ParameterizedTest
    @MethodSource("provideRequestsAndResponsesForDelete")
    void givenId_whenDelete_returnAppropriateResponses(final Long id,
                                                       final int statusCode) throws Exception {
        final RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1.0/tasks/" + id);
        final MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        //then
        assertEquals(statusCode, result.getResponse().getStatus());
    }

    @ParameterizedTest
    @MethodSource("provideRequestsAndResponsesForUpdate")
    void givenId_whenUpdate_returnAppropriateResponses(final Long id,
                                                       final UpdateTaskRequest updateTaskRequest,
                                                       final int statusCode,
                                                       final Object response,
                                                       final boolean isThrowingExceptionInController) throws Exception {
        final RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1.0/tasks/" + id)
                .content(objectMapper.writeValueAsString(updateTaskRequest))
                .contentType(MediaType.APPLICATION_JSON);

        switch (statusCode) {
            case 400:
                if(isThrowingExceptionInController) {
                    doThrow(new TaskCategoryNotFoundException()).when(taskService)
                            .updateTask(toUpdateTaskCommand(id, updateTaskRequest));
                }
                break;
            case 404:
                if(isThrowingExceptionInController) {
                    doThrow(new TaskNotFoundException()).when(taskService)
                            .updateTask(toUpdateTaskCommand(id, updateTaskRequest));
                }
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
                    if(isThrowingExceptionInController) {
                        assertEquals(objectMapper.writeValueAsString(response), result.getResponse().getContentAsString());
                    } else {
                        assertThat(
                                objectMapper.readValue(result.getResponse().getContentAsString(), ListErrorResponse.class).messages(),
                                containsInAnyOrder(((ListErrorResponse) response).messages().toArray())
                        );
                    }
                default:
                    break;
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideRequestsAndResponsesForGetById")
    void givenId_whenGetById_returnAppropriateResponses(final Long id,
                                                        final TaskProjection taskProjection,
                                                        final int statusCode,
                                                        final Object response) throws Exception {
        //when
        if (statusCode == 200) {
            when(taskService.getTask(id)).thenReturn(Optional.of(taskProjection));
        }

        final RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1.0/tasks/" + id);

        final MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        //then
        assertEquals(statusCode, result.getResponse().getStatus());
        if (statusCode == 200) {
            assertEquals(objectMapper.writeValueAsString(response), result.getResponse().getContentAsString());
        }
    }

    @ParameterizedTest
    @MethodSource("provideRequestsAndResponsesForGet")
    void givenGetTaskCategoriesRequest_whenGet_returnAppropriateResponses(final MultiValueMap<String, String> multiValueMap,
                                                                          final TaskFilters taskFilters,
                                                                          final CustomPage<TaskProjection> taskProjectionCustomPage,
                                                                          final int statusCode,
                                                                          final Object response) throws Exception {
        //when
        if (statusCode == 200) {
            when(taskService.getTasks(taskFilters)).thenReturn(taskProjectionCustomPage);
        }

        final RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1.0/tasks")
                .queryParams(multiValueMap);

        final MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        //then
        assertEquals(statusCode, result.getResponse().getStatus());
        if(response != null) {
            assertEquals(objectMapper.writeValueAsString(response), result.getResponse().getContentAsString());
        }
    }

    private static Stream<Arguments> provideRequestsAndResponsesForCreate() {
        final AddTaskRequest validRequest = new AddTaskRequest("name", "description", Instant.now().plusSeconds(1000L), 1L);
        final GenericAddResourceResponse validResponse = new GenericAddResourceResponse(1L);

        final AddTaskRequest invalidRequest = new AddTaskRequest(
                generateRandomCharacterString(101),
                generateRandomCharacterString(501),
                Instant.now().minusSeconds(1000L),
                1L);
        final ListErrorResponse invalidResponse = new ListErrorResponse(
                Set.of("Name must be smaller than 100",
                        "Description must be smaller than 500",
                        "Deadline must be in the future"));

        final AddTaskRequest nullParametersRequest = new AddTaskRequest(
                null,
                null,
                null,
                null);
        final ListErrorResponse nullParametersResponse = new ListErrorResponse(
                Set.of("Name must be specified in request",
                        "Deadline must be specified in request",
                        "Category ID must be specified in request"));

        final ErrorResponse taskCategoryNotFoundResponse = new ErrorResponse("Task category does not exist");



        return Stream.of(
                Arguments.of(validRequest, 201, validResponse, false),
                Arguments.of(invalidRequest, 400, invalidResponse, false),
                Arguments.of(nullParametersRequest, 400, nullParametersResponse, false),
                Arguments.of(validRequest, 400, taskCategoryNotFoundResponse, true)
        );
    }

    private static Stream<Arguments> provideRequestsAndResponsesForDelete() {
        return Stream.of(
                Arguments.of(1L, 204),
                Arguments.of(null, 400)
        );
    }

    private static Stream<Arguments> provideRequestsAndResponsesForUpdate() {
        final Instant instant = Instant.now().plusSeconds(1000L);
        final UpdateTaskRequest validRequest = new UpdateTaskRequest("name", "description", instant, 2L);

        final UpdateTaskRequest invalidRequest = new UpdateTaskRequest(generateRandomCharacterString(101), generateRandomCharacterString(501), instant.minusSeconds(1000L), null);
        final ListErrorResponse invalidResponse = new ListErrorResponse(
                Set.of("Name must be smaller than 100", "Description must be smaller than 500", "Deadline must be in the future"));

        final ErrorResponse taskCategoryDoesNotExistResponse = new ErrorResponse("Task category does not exist");

        return Stream.of(
                Arguments.of(1L, validRequest, 204, null, false),
                Arguments.of(null, validRequest, 400, null, false),
                Arguments.of(1L, invalidRequest, 400, invalidResponse, false),
                Arguments.of(1L, validRequest, 400, taskCategoryDoesNotExistResponse, true),
                Arguments.of(1L, validRequest, 404, null, true)
        );
    }

    private AddTaskCommand toAddTaskCommand(final AddTaskRequest addTaskRequest) {
        return new AddTaskCommand(addTaskRequest.name(),
                addTaskRequest.description(),
                addTaskRequest.deadline().atZone(clock.getZone()),
                addTaskRequest.categoryId());
    }

    private UpdateTaskCommand toUpdateTaskCommand(final Long id,final UpdateTaskRequest updateTaskRequest) {
        return new UpdateTaskCommand(
                id,
                updateTaskRequest.name(),
                updateTaskRequest.description(),
                updateTaskRequest.deadline().atZone(clock.getZone()),
                updateTaskRequest.categoryId());
    }

    private static Stream<Arguments> provideRequestsAndResponsesForGetById() {
        final Instant now = Instant.now();
        final TaskCategoryProjection taskCategoryProjection = new TaskCategoryProjection(1L, "name", "description");
        final TaskProjection validProjection = new TaskProjection(
                1L,
                "name",
                "description",
                now.atZone(ZoneId.of("UTC")),
                taskCategoryProjection);
        final GetTaskResponse validResponse = new GetTaskResponse(validProjection.id(),
                validProjection.name(),
                validProjection.description(),
                validProjection.deadline(),
                new GetTaskCategoryResponse(validProjection.category().id(), validProjection.category().name(), validProjection.category().description()));

        return Stream.of(
                Arguments.of(1L, validProjection, 200, validResponse),
                Arguments.of(null, null, 400, null),
                Arguments.of(1L, null, 404, null)
        );
    }

    private static Stream<Arguments> provideRequestsAndResponsesForGet() {
        final Instant instant = Instant.ofEpochSecond(1712512060);
        final ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Z"));

        //all params
        final MultiValueMap<String, String> allParameters = new MultiValueMapAdapter<>(new HashMap<>() {{
            put("page", List.of("0"));
            put("size", List.of("5"));
            put("name", List.of("name"));
            put("sort", List.of("ASC"));
            put("deadlineDate", List.of(String.valueOf(Long.valueOf(instant.getEpochSecond()))));
            put("deadlineMode", List.of("AFTER"));
            put("category", List.of("1"));
        }});
        final TaskFilters taskFilters = new TaskFilters("name", 1L, zonedDateTime, DeadlineMode.AFTER, 0, 5, SortDirection.ASC);
        final TaskCategoryProjection taskCategoryProjection = new TaskCategoryProjection(1L, "name", "description");
        final GetTaskCategoryResponse taskCategoryResponse = new GetTaskCategoryResponse(1L, "name", "description");
        final CustomPage<TaskProjection> taskProjection = new CustomPage<>(
                List.of(
                        new TaskProjection(1L, "name", "description", zonedDateTime, taskCategoryProjection)
                ), 1, 1
        );
        final CommonPage<GetTaskResponse> validResponse = new CommonPage<>(0, 5, 1, 1,
                List.of(
                        new GetTaskResponse(1L, "name", "description", zonedDateTime, taskCategoryResponse)
                ));
        //all params

        //params all non default
        final MultiValueMap<String, String> allNonDefaultParameters = new MultiValueMapAdapter<>(new HashMap<>() {{
            put("page", List.of("0"));
        }});
        final TaskFilters allNonDefaultFilters = new TaskFilters(null, null, null, DeadlineMode.AFTER, 0, 10, SortDirection.DESC);
        final CommonPage<GetTaskResponse> validNonDefaultParametersResponse = new CommonPage<>(0, 10, 1, 1,
                List.of(
                        new GetTaskResponse(1L, "name", "description", zonedDateTime, taskCategoryResponse)
                ));
        //params all non default

        //params with empty page
        final MultiValueMap<String, String> parametersWithNameForEmptyPage = new MultiValueMapAdapter<>(new HashMap<>() {{
            put("page", List.of("0"));
        }});
        final TaskFilters emptyPageFilters = new TaskFilters(null, null, null, DeadlineMode.AFTER, 0, 10, SortDirection.DESC);
        final CustomPage<TaskProjection> emptyTaskProjection = new CustomPage<>(Collections.emptyList(), 0, 0);
        final CommonPage<GetTaskResponse> emptyPageResponse = new CommonPage<>(0, 10, 0, 0, Collections.emptyList());
        //params with empty page end

        //invalid params as page is missing
        final MultiValueMap<String, String> pageMissing = new MultiValueMapAdapter<>(new HashMap<>() {{
            put("size", List.of("5"));
            put("name", List.of("name"));
            put("sort", List.of("ASC"));
            put("deadlineDate", List.of(String.valueOf(Long.valueOf(instant.getEpochSecond()))));
            put("deadlineMode", List.of("AFTER"));
            put("category", List.of("1"));
        }});
        final ListErrorResponse invalidResponse = new ListErrorResponse(
                Set.of("Page must be specified as request param"));
        //invalid params as page is missing end

        //invalid params as specified deadline mode in invalid
        final MultiValueMap<String, String> deadlineModeInvalid = new MultiValueMapAdapter<>(new HashMap<>() {{
            put("page", List.of("0"));
            put("deadlineMode", List.of("INVALID"));
        }});
        //invalid params as page is missing end

        //invalid params as specified sort direction is invalid
        final MultiValueMap<String, String> sortDirectionInvalid = new MultiValueMapAdapter<>(new HashMap<>() {{
            put("page", List.of("0"));
            put("sort", List.of("INVALID"));
        }});
        //invalid params as page is missing end

        return Stream.of(
                Arguments.of(allParameters, taskFilters, taskProjection, 200, validResponse),
                Arguments.of(allNonDefaultParameters, allNonDefaultFilters, taskProjection, 200, validNonDefaultParametersResponse),
                Arguments.of(parametersWithNameForEmptyPage, emptyPageFilters, emptyTaskProjection, 200, emptyPageResponse),
                Arguments.of(pageMissing, null, null, 400, invalidResponse),
                Arguments.of(deadlineModeInvalid, null, null, 400, null),
                Arguments.of(sortDirectionInvalid, null, null, 400, null)
        );
    }
}
