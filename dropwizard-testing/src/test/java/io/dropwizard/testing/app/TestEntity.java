package io.dropwizard.testing.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "test_entities")
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @NotNull
    @Column(name = "desc")
    @Nullable
    private String description;

    protected TestEntity() {
        // for Hibernate
    }

    public TestEntity(@Nullable String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return requireNonNull(description);
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
