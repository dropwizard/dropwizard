package io.dropwizard.testing.junit5;

import io.dropwizard.testing.app.Person;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class ResourceExtensionMocksTest {
    @Mock
    private Person mockPerson;

    private final ResourceExtension resources = ResourceExtension.builder()
            .addProvider(this::createResource)
            .build();

    private TestResource createResource() {
        return new TestResource(mockPerson);
    }

    @Test
    void accessingMockPersonSucceeds() {
        when(mockPerson.getName()).thenReturn("Person-Name");

        assertThat(resources.target("test/name").request().get(String.class))
            .isEqualTo("Person-Name");
    }

    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public static class TestResource {
        private final Person person;

        public TestResource(Person person) {
            this.person = person;
        }

        @GET
        @Path("/name")
        public String getPersonName() {
            return person.getName();
        }
    }
}
