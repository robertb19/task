package ch.cern.todo.core.domain;

import java.time.ZonedDateTime;
import java.util.Objects;

public class Task {

    private Long id;

    private String name;

    private String description;

    private ZonedDateTime deadline;

    private Long taskCategoryId;

    public Task(String name, String description, ZonedDateTime deadline, Long taskCategoryId) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.taskCategoryId = taskCategoryId;
    }

    public Task(Long id, String name, String description, ZonedDateTime deadline, Long taskCategoryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.taskCategoryId = taskCategoryId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(ZonedDateTime deadline) {
        this.deadline = deadline;
    }

    public Long getTaskCategoryId() {
        return taskCategoryId;
    }

    public void setTaskCategoryId(Long taskCategoryId) {
        this.taskCategoryId = taskCategoryId;
    }

    //in this case a task can share all parameters and be different from a domain perspective, only the ID defines whether its the same
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task that = (Task) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
