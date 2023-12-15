package com.example.helloworld.db;

import com.example.helloworld.core.Person;
import com.mysql.cj.conf.PropertyKey;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(DropwizardExtensionsSupport.class)
@DisabledForJreRange(min = JRE.JAVA_16)
class PersonDAOIntegrationTest {
    @Container
    private static final MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.24"));

    public DAOTestExtension daoTestRule = DAOTestExtension.newBuilder()
            .customizeConfiguration(c -> c.setProperty(AvailableSettings.DIALECT, MySQLDialect.class.getName()))
            .setDriver(MY_SQL_CONTAINER.getDriverClassName())
            .setUrl(MY_SQL_CONTAINER.getJdbcUrl())
            .setUsername(MY_SQL_CONTAINER.getUsername())
            .setPassword(MY_SQL_CONTAINER.getPassword())
            .setProperty(PropertyKey.tlsVersions.getKeyName(), "TLSv1.1,TLSv1.2,TLSv1.3")
            .addEntityClass(Person.class)
            .build();

    private PersonDAO personDAO;

    @BeforeEach
    void setUp() {
        personDAO = new PersonDAO(daoTestRule.getSessionFactory());
    }

    @Test
    void createPerson() {
        final Person jeff = daoTestRule.inTransaction(() -> personDAO.create(new Person("Jeff", "The plumber", 1995)));
        assertThat(jeff.getId()).isPositive();
        assertThat(jeff.getFullName()).isEqualTo("Jeff");
        assertThat(jeff.getJobTitle()).isEqualTo("The plumber");
        assertThat(jeff.getYearBorn()).isEqualTo(1995);
        assertThat(personDAO.findById(jeff.getId())).isEqualTo(Optional.of(jeff));
    }

    @Test
    void findAll() {
        daoTestRule.inTransaction(() -> {
            personDAO.create(new Person("Jeff", "The plumber", 1975));
            personDAO.create(new Person("Jim", "The cook", 1985));
            personDAO.create(new Person("Randy", "The watchman", 1995));
        });

        final List<Person> persons = personDAO.findAll();
        assertThat(persons).extracting("fullName").containsOnly("Jeff", "Jim", "Randy");
        assertThat(persons).extracting("jobTitle").containsOnly("The plumber", "The cook", "The watchman");
        assertThat(persons).extracting("yearBorn").containsOnly(1975, 1985, 1995);
    }

    @Test
    void handlesNullFullName() {
        assertThatExceptionOfType(ConstraintViolationException.class).isThrownBy(() ->
                daoTestRule.inTransaction(() -> personDAO.create(new Person(null, "The null", 0))));
    }
}
