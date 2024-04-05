package ch.cern.todo.adapter.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TASK_CATEGORIES")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TaskCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="CATEGORY_ID")
    private Long id;

    @Column(name="CATEGORY_NAME")
    private String name;

    @Column(name="CATEGORY_DESCRIPTION")
    private String description;

    /*
    Equality in the context of a DB essentially means that they share the same identifier
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof TaskCategoryEntity))
            return false;

        final TaskCategoryEntity other = (TaskCategoryEntity) o;

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
