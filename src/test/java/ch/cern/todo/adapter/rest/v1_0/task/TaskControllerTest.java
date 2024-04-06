package ch.cern.todo.adapter.rest.v1_0.task;

import ch.cern.todo.adapter.rest.v1_0.request.ErrorResponse;
import ch.cern.todo.adapter.rest.v1_0.request.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.request.ListErrorResponse;
import ch.cern.todo.adapter.rest.v1_0.task.request.AddTaskRequest;
import ch.cern.todo.core.application.TaskService;
import ch.cern.todo.core.application.command.dto.AddTaskCommand;
import ch.cern.todo.core.application.command.dto.DeleteTaskCategoryCommand;
import ch.cern.todo.core.application.exception.TaskCategoryNotFoundException;
import ch.cern.todo.core.application.exception.TaskRecordsMappedException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private AddTaskCommand toAddTaskCommand(final AddTaskRequest addTaskRequest) {
        return new AddTaskCommand(addTaskRequest.name(),
                addTaskRequest.description(),
                addTaskRequest.deadline().atZone(clock.getZone()),
                addTaskRequest.categoryId());
    }

    private static Stream<Arguments> provideRequestsAndResponsesForDelete() {
        return Stream.of(
                Arguments.of(1L, 204),
                Arguments.of(null, 400)
        );
    }

}
