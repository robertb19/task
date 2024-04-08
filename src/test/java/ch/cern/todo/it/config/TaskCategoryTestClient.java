package ch.cern.todo.it.config;

import ch.cern.todo.adapter.rest.v1_0.request.CommonPage;
import ch.cern.todo.adapter.rest.v1_0.request.GenericAddResourceResponse;
import ch.cern.todo.adapter.rest.v1_0.task.category.request.AddTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.task.category.request.UpdateTaskCategoryRequest;
import ch.cern.todo.adapter.rest.v1_0.task.category.response.GetTaskCategoryResponse;
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
public class TaskCategoryTestClient {

	private final TestRestTemplate restTemplate;
	private final String host;

	public TaskCategoryTestClient(TestRestTemplate restTemplate, @LocalServerPort int port) {
		this.restTemplate = restTemplate;
		this.host = "http://localhost:" + port + "/todos";
	}

	public ResponseEntity<GenericAddResourceResponse> create(final AddTaskCategoryRequest addTaskCategoryRequest) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		return restTemplate.exchange(host + "/v1.0/categories", HttpMethod.POST, new HttpEntity<>(addTaskCategoryRequest, headers), GenericAddResourceResponse.class);
	}

	public ResponseEntity<GetTaskCategoryResponse> getById(final Long id) {
		return restTemplate.exchange(host + "/v1.0/categories/" + id, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), GetTaskCategoryResponse.class);
	}

	public ResponseEntity<CommonPage<GetTaskCategoryResponse>> get(final Optional<String> name, final Optional<String> sortDirection, final Optional<String> page, final Optional<String> pageSize) {
		UriBuilder uriBuilder = UriComponentsBuilder
				.fromHttpUrl(host + "/v1.0/categories");

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

		return restTemplate.exchange(uriBuilder.build(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), new ParameterizedTypeReference<>(){});
	}


	public ResponseEntity<Void> update(final Long id, final UpdateTaskCategoryRequest updateTaskCategoryRequest) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		return restTemplate.exchange(host + "/v1.0/categories/" + id, HttpMethod.PATCH, new HttpEntity<>(updateTaskCategoryRequest, headers), Void.class);
	}

	public ResponseEntity<Void> delete(final Long id) {
		return restTemplate.exchange(host + "/v1.0/categories/" + id, HttpMethod.DELETE, new HttpEntity<>(new HttpHeaders()), Void.class);
	}


}
