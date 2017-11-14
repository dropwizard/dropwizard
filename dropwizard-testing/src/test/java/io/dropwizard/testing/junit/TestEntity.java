package io.dropwizard.testing.junit;

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
class TestEntity {

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

    TestEntity(@Nullable String description) {
        this.description = description;
    }

    long getId() {
        return id;
    }

    String getDescription() {
        return requireNonNull(description);
    }

    void setDescription(final String description) {
        this.description = description;
    }
}
