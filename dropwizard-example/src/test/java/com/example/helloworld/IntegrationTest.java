package com.example.helloworld;

import com.example.helloworld.api.Saying;
import com.example.helloworld.core.Person;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;

import static io.dropwizard.testing.ConfigOverride.config;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class IntegrationTest {
    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-example.yml");

    @TempDir
    static Path tempDir;
    static Supplier<String> CURRENT_LOG = () -> tempDir.resolve("application.log").toString();
    static Supplier<String> ARCHIVED_LOG = () -> tempDir.resolve("application-%d-%i.log.gz").toString();

    static final DropwizardAppExtension<HelloWorldConfiguration> APP = new DropwizardAppExtension<>(
            HelloWorldApplication.class, CONFIG_PATH,
            config("database.url", () -> "jdbc:h2:" + tempDir.resolve("database.h2")),
            config("logging.appenders[1].currentLogFilename", CURRENT_LOG),
            config("logging.appenders[1].archivedLogFilenamePattern", ARCHIVED_LOG)
    );

    @BeforeAll
    public static void migrateDb() throws Exception {
        APP.getApplication().run("db", "migrate", CONFIG_PATH);
    }

    @Test
    void testHelloWorld() throws Exception {
        final Optional<String> name = Optional.of("Dr. IntegrationTest");
        final Saying saying = APP.client().target("http://localhost:" + APP.getLocalPort() + "/hello-world")
                .queryParam("name", name.get())
                .request()
                .get(Saying.class);
        assertThat(saying.getContent()).isEqualTo(APP.getConfiguration().buildTemplate().render(name));
    }

    @Test
    void testPostPerson() throws Exception {
        final Person person = new Person("Dr. IntegrationTest", "Chief Wizard", 1525);
        final Person newPerson = postPerson(person);
        assertThat(newPerson.getFullName()).isEqualTo(person.getFullName());
        assertThat(newPerson.getJobTitle()).isEqualTo(person.getJobTitle());
    }

    @Test
    void testRenderingPersonFreemarker() throws Exception {
        testRenderingPerson("view_freemarker");
    }

    @Test
    void testRenderingPersonMustache() throws Exception {
        testRenderingPerson("view_mustache");
    }

    private void testRenderingPerson(String viewName) throws Exception {
        final Person person = new Person("Dr. IntegrationTest", "Chief Wizard", 1525);
        final Person newPerson = postPerson(person);
        final String url = "http://localhost:" + APP.getLocalPort() + "/people/" + newPerson.getId() + "/" + viewName;
        Response response = APP.client().target(url).request().get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    private Person postPerson(Person person) {
        return APP.client().target("http://localhost:" + APP.getLocalPort() + "/people")
                .request()
                .post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(Person.class);
    }

    @Test
    void testLogFileWritten() throws IOException {
        // The log file is using a size and time based policy, which used to silently
        // fail (and not write to a log file). This test ensures not only that the
        // log file exists, but also contains the log line that jetty prints on startup
        final Path logFile = Paths.get(CURRENT_LOG.get());
        assertThat(logFile).exists();
        final String logFileContent = new String(Files.readAllBytes(logFile), UTF_8);
        assertThat(logFileContent)
                .contains("0.0.0.0:" + APP.getLocalPort(), "Starting hello-world", "Started application", "Started admin")
                .doesNotContain("Exception", "ERROR", "FATAL");
    }
}
