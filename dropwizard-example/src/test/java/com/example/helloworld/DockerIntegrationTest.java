package com.example.helloworld;

import com.example.helloworld.api.Saying;
import com.example.helloworld.core.Person;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(DropwizardExtensionsSupport.class)
@DisabledForJreRange(min = JRE.JAVA_16)
public class DockerIntegrationTest {
    @Container
    private static final MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.24"));

    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-docker-example.yml");

    public static final DropwizardAppExtension<HelloWorldConfiguration> APP = new DropwizardAppExtension<>(
            HelloWorldApplication.class, CONFIG_PATH,
            ConfigOverride.config("database.url", MY_SQL_CONTAINER::getJdbcUrl),
            ConfigOverride.config("database.user", MY_SQL_CONTAINER::getUsername),
            ConfigOverride.config("database.password", MY_SQL_CONTAINER::getPassword),
            ConfigOverride.config("database.properties.enabledTLSProtocols", "TLSv1.1,TLSv1.2,TLSv1.3")
    );

    @BeforeAll
    public static void migrateDb() throws Exception {
        APP.getApplication().run("db", "migrate", CONFIG_PATH);
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

    @Test
    void testPostPerson() {
        final Person person = new Person("Dr. IntegrationTest", "Chief Wizard", 1525);
        final Person newPerson = postPerson(person);
        assertThat(newPerson.getFullName()).isEqualTo(person.getFullName());
        assertThat(newPerson.getJobTitle()).isEqualTo(person.getJobTitle());
    }

    @Test
    void testRenderingPersonFreemarker() {
        testRenderingPerson("view_freemarker");
    }

    @Test
    void testRenderingPersonMustache() {
        testRenderingPerson("view_mustache");
    }

    private void testRenderingPerson(String viewName) {
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
        final Path log = Paths.get("./logs/application.log");
        assertThat(log).exists();
        final String actual = new String(Files.readAllBytes(log), UTF_8);
        assertThat(actual).contains("0.0.0.0:" + APP.getLocalPort());
    }
}
