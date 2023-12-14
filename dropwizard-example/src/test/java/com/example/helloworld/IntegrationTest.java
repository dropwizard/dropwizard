package com.example.helloworld;

import com.example.helloworld.api.Saying;
import com.example.helloworld.core.Person;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(DropwizardExtensionsSupport.class)
class IntegrationTest {
    private static final String CONFIG = "test-example.yml";

    @TempDir
    static Path tempDir;
    static Supplier<String> CURRENT_LOG = () -> tempDir.resolve("application.log").toString();
    static Supplier<String> ARCHIVED_LOG = () -> tempDir.resolve("application-%d-%i.log.gz").toString();

    static final DropwizardAppExtension<HelloWorldConfiguration> APP = new DropwizardAppExtension<>(
            HelloWorldApplication.class, CONFIG,
            new ResourceConfigurationSourceProvider(),
            config("database.url", () -> "jdbc:h2:" + tempDir.resolve("database.h2")),
            config("logging.appenders[1].currentLogFilename", CURRENT_LOG),
            config("logging.appenders[1].archivedLogFilenamePattern", ARCHIVED_LOG)
    );

    @BeforeAll
    public static void migrateDb() throws Exception {
        APP.getApplication().run("db", "migrate", resourceFilePath(CONFIG));
    }

    @Test
    void testHelloWorld() {
        final Optional<String> name = Optional.of("Dr. IntegrationTest");
        final Saying saying = APP.client().target("http://localhost:" + APP.getLocalPort() + "/hello-world")
            .queryParam("name", name.get())
            .request()
            .get(Saying.class);
        assertThat(saying.getContent()).isEqualTo(APP.getConfiguration().buildTemplate().render(name));
    }

    @Nested
    class DateParameterTests {
        @Test
        void validDateParameter() {
            final String date = APP.client().target("http://localhost:" + APP.getLocalPort() + "/hello-world/date")
                .queryParam("date", "2022-01-20")
                .request()
                .get(String.class);
            assertThat(date).isEqualTo("2022-01-20");
        }

        @ParameterizedTest
        @ValueSource(strings = {"null", "abc", "0"})
        void invalidDateParameter(String value) {
            assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> APP.client().target("http://localhost:" + APP.getLocalPort() + "/hello-world/date")
                    .queryParam("date", value)
                    .request()
                    .get(String.class));
        }

        @Test
        void noDateParameter() {
            final String date = APP.client().target("http://localhost:" + APP.getLocalPort() + "/hello-world/date")
                .request()
                .get(String.class);
            assertThat(date).isEmpty();
        }
    }

    @Test
    void testPostPerson() {
        final Person person = new Person("Dr. IntegrationTest", "Chief Wizard", 1525);
        final Person newPerson = postPerson(person);
        assertThat(newPerson.getFullName()).isEqualTo(person.getFullName());
        assertThat(newPerson.getJobTitle()).isEqualTo(person.getJobTitle());
    }

    @ParameterizedTest
    @ValueSource(strings={"view_freemarker", "view_mustache"})
    void testRenderingPerson(String viewName) {
        final Person person = new Person("Dr. IntegrationTest", "Chief Wizard", 1525);
        final Person newPerson = postPerson(person);
        final String url = "http://localhost:" + APP.getLocalPort() + "/people/" + newPerson.getId() + "/" + viewName;
        Response response = APP.client().target(url).request().get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    private Person postPerson(Person person) {
        return APP.client().target("http://localhost:" + APP.getLocalPort() + "/people")
                .property(ClientProperties.CONNECT_TIMEOUT, 0)
                .request()
                .post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(Person.class);
    }

    @Test
    void testLogFileWritten() {
        // The log file is using a size and time based policy, which used to silently
        // fail (and not write to a log file). This test ensures not only that the
        // log file exists, but also contains the log line that jetty prints on startup
        assertThat(new File(CURRENT_LOG.get()))
            .exists()
            .content()
            .contains("0.0.0.0:" + APP.getLocalPort(), "Starting hello-world", "Started application", "Started admin")
            .doesNotContain("Exception", "ERROR", "FATAL");
    }

    @Test
    void healthCheckShouldSucceed() {
        final Response healthCheckResponse =
                APP.client().target("http://localhost:" + APP.getLocalPort() + "/health-check")
                .request()
                .get();

        assertThat(healthCheckResponse)
                .extracting(Response::getStatus)
                .isEqualTo(Response.Status.OK.getStatusCode());
    }
}
