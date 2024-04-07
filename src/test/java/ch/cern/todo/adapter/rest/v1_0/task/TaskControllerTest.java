package ch.cern.todo.adapter.rest.v1_0.task;

import ch.cern.todo.adapter.rest.v1_0.request.ErrorResponse;
import ch.cern.todo.adapter.rest.v1_0.request.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.request.ListErrorResponse;
import ch.cern.todo.adapter.rest.v1_0.task.request.AddTaskRequest;
import ch.cern.todo.adapter.rest.v1_0.task.request.UpdateTaskRequest;
import ch.cern.todo.adapter.rest.v1_0.task.response.GetTaskResponse;
import ch.cern.todo.core.application.TaskService;
import ch.cern.todo.core.application.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.command.dto.UpdateTaskCommand;
import ch.cern.todo.core.application.exception.TaskCategoryNotFoundException;
import ch.cern.todo.core.application.exception.TaskNotFoundException;
import ch.cern.todo.core.application.query.dto.TaskCategoryProjection;
import ch.cern.todo.core.application.query.dto.TaskProjection;
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
import org.springframework.web.context.WebApplicationContext;
import util.TestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
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
                validProjection.category());

        return Stream.of(
                Arguments.of(1L, validProjection, 200, validResponse),
                Arguments.of(null, null, 400, null),
                Arguments.of(1L, null, 404, null)
        );
    }
}
