package io.dropwizard.testing.app;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

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
