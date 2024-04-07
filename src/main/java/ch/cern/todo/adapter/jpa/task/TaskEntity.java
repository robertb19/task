package ch.cern.todo.adapter.jpa.task;

import ch.cern.todo.adapter.jpa.task.category.TaskCategoryEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "TASKS")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="TASK_ID")
    private Long id;

    @Column(name="TASK_NAME")
    private String name;

    @Column(name="TASK_DESCRIPTION")
    private String description;

    @Column(name="DEADLINE")
    private ZonedDateTime deadline;

    @ManyToOne
    @JoinColumn(name="CATEGORY_ID")
    private TaskCategoryEntity category;

    public TaskEntity(String name, String description, ZonedDateTime deadline) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof TaskEntity))
            return false;

        final TaskEntity other = (TaskEntity) o;

        return id != null &&
                id.equals(other.getId());
    }

    /*
    I'm aware that this will hash to one bucket in HashMaps, however Hibernate shouldn't manage huge associations
    (direct queries should be used instead) regardless and since we do not have a good natural key (as we can change our category name),
    we have to go with a trade-off
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
