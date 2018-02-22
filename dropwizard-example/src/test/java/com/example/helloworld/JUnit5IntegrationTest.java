package com.example.helloworld;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.example.helloworld.api.Saying;
import com.example.helloworld.core.Person;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

@ExtendWith(DropwizardExtensionsSupport.class)
public class JUnit5IntegrationTest {
	
    private static final String TMP_FILE = createTempFile();
    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-example.yml");

	public static final DropwizardAppExtension<HelloWorldConfiguration> EXTENSION = new DropwizardAppExtension<>(
			HelloWorldApplication.class, CONFIG_PATH, 
			ConfigOverride.config("database.url", "jdbc:h2:" + TMP_FILE));


    private static String createTempFile() {
        try {
            return File.createTempFile("test-example", null).getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @BeforeAll
    public static void migrateDb() throws Exception {
    		EXTENSION.getApplication().run("db", "migrate", CONFIG_PATH);
    }
    
    @Test
    public void testHelloWorld() throws Exception {
        final Optional<String> name = Optional.of("Dr. IntegrationTest");
        final Saying saying = EXTENSION.client().target("http://localhost:" + EXTENSION.getLocalPort() + "/hello-world")
                .queryParam("name", name.get())
                .request()
                .get(Saying.class);
        assertThat(saying.getContent()).isEqualTo(EXTENSION.getConfiguration().buildTemplate().render(name));
    }

    @Test
    public void testPostPerson() throws Exception {
        final Person person = new Person("Dr. IntegrationTest", "Chief Wizard");
        final Person newPerson = EXTENSION.client().target("http://localhost:" + EXTENSION.getLocalPort() + "/people")
                .request()
                .post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(Person.class);
        assertThat(newPerson.getId()).isNotNull();
        assertThat(newPerson.getFullName()).isEqualTo(person.getFullName());
        assertThat(newPerson.getJobTitle()).isEqualTo(person.getJobTitle());
    }

    @Test
    public void testLogFileWritten() throws IOException {
        // The log file is using a size and time based policy, which used to silently
        // fail (and not write to a log file). This test ensures not only that the
        // log file exists, but also contains the log line that jetty prints on startup
        final Path log = Paths.get("./logs/application.log");
        assertThat(log).exists();
        final String actual = new String(Files.readAllBytes(log), UTF_8);
        assertThat(actual).contains("0.0.0.0:" + EXTENSION.getLocalPort());
    }

}
