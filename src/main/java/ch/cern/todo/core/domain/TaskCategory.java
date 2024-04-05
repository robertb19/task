package ch.cern.todo.core.domain;

import java.util.Objects;

public class TaskCategory {

    private Long id;

    private String name;

    private String description;

    public TaskCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public TaskCategory(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskCategory that = (TaskCategory) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
