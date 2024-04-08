package ch.cern.todo.it.config;

import ch.cern.todo.adapter.rest.v1_0.request.CommonPage;
import ch.cern.todo.adapter.rest.v1_0.request.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.task.request.AddTaskRequest;
import ch.cern.todo.adapter.rest.v1_0.task.request.UpdateTaskRequest;
import ch.cern.todo.adapter.rest.v1_0.task.response.GetTaskResponse;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@Lazy
@TestComponent
public class TaskTestClient {

	private final TestRestTemplate restTemplate;
	private final String host;

	public TaskTestClient(TestRestTemplate restTemplate, @LocalServerPort int port) {
		this.restTemplate = restTemplate;
		this.host = "http://localhost:" + port + "/todos";
	}

	public ResponseEntity<GenericAddResourceResponse> create(final AddTaskRequest addTaskRequest) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		return restTemplate.exchange(host + "/v1.0/tasks", HttpMethod.POST, new HttpEntity<>(addTaskRequest, headers), GenericAddResourceResponse.class);
	}

	public ResponseEntity<GetTaskResponse> getById(final Long id) {
		return restTemplate.exchange(host + "/v1.0/tasks/" + id, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), GetTaskResponse.class);
	}

	public ResponseEntity<CommonPage<GetTaskResponse>> get(final Optional<String> name,
														   final Optional<String> sortDirection,
														   final Optional<String> page,
														   final Optional<String> pageSize,
														   final Optional<String> deadline,
														   final Optional<String> deadlineMode,
														   final Optional<String> categoryId) {
		UriBuilder uriBuilder = UriComponentsBuilder
				.fromHttpUrl(host + "/v1.0/tasks");

		if(name.isPresent()) {
			uriBuilder = uriBuilder.queryParam("name", name.get());
		}

		if(sortDirection.isPresent()) {
			uriBuilder = uriBuilder.queryParam("sort", sortDirection.get());
		}

		if(page.isPresent()) {
			uriBuilder = uriBuilder.queryParam("page", page.get());
		}

		if(pageSize.isPresent()) {
			uriBuilder = uriBuilder.queryParam("size", pageSize.get());
		}

		if(deadline.isPresent()) {
			uriBuilder = uriBuilder.queryParam("deadlineDate", deadline.get());
		}

		if(deadlineMode.isPresent()) {
			uriBuilder = uriBuilder.queryParam("deadlineMode", deadlineMode.get());
		}

		if(categoryId.isPresent()) {
			uriBuilder = uriBuilder.queryParam("category", categoryId.get());
		}

		return restTemplate.exchange(uriBuilder.build(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), new ParameterizedTypeReference<>(){});
	}

	public ResponseEntity<Void> update(final Long id, final UpdateTaskRequest updateTaskRequest) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		return restTemplate.exchange(host + "/v1.0/tasks/" + id, HttpMethod.PATCH, new HttpEntity<>(updateTaskRequest, headers), Void.class);
	}

	public ResponseEntity<Void> delete(final Long id) {
		return restTemplate.exchange(host + "/v1.0/tasks/" + id, HttpMethod.DELETE, new HttpEntity<>(new HttpHeaders()), Void.class);
	}


}
