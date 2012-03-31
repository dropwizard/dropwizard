package com.yammer.dropwizard.jersey;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.core.util.StringKeyObjectValueIgnoreCaseMultivaluedMap;
import com.yammer.dropwizard.json.Json;
import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.Test;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class JacksonMessageBodyProviderTest {
    private static final Annotation[] NONE = new Annotation[0];

    public static class Example {
        @Min(0)
        @JsonProperty
        int id;
    }

    private final Json json = spy(new Json());

    private final JacksonMessageBodyProvider provider = new JacksonMessageBodyProvider(json);

    @Test
    public void readsDeserializableTypes() throws Exception {
        assertThat(provider.isReadable(String.class, null, null, null),
                   is(true));

        verify(json).canDeserialize(String.class);
    }

    @Test
    public void writesSerializableTypes() throws Exception {
        assertThat(provider.isWriteable(String.class, null, null, null),
                   is(true));

        verify(json).canSerialize(String.class);
    }

    @Test
    public void isChunked() throws Exception {
        assertThat(provider.getSize(null, null, null, null, null),
                   is(-1L));
    }

    @Test
    public void deserializesRequestEntities() throws Exception {
        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"id\":1}".getBytes());
        final Class<?> klass = Example.class;

        final Object obj = provider.readFrom((Class<Object>) klass,
                                             Example.class,
                                             NONE,
                                             MediaType.APPLICATION_JSON_TYPE,
                                             new MultivaluedMapImpl(),
                                             entity);

        assertThat(obj,
                   is(instanceOf(Example.class)));

        assertThat(((Example) obj).id,
                   is(1));
    }

    @Test
    public void returnsValidatedRequestEntities() throws Exception {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"id\":1}".getBytes());
        final Class<?> klass = Example.class;

        final Object obj = provider.readFrom((Class<Object>) klass,
                                             Example.class,
                                             new Annotation[] { valid },
                                             MediaType.APPLICATION_JSON_TYPE,
                                             new MultivaluedMapImpl(),
                                             entity);

        assertThat(obj,
                   is(instanceOf(Example.class)));

        assertThat(((Example) obj).id,
                   is(1));
    }

    @Test
    public void returnsA422ForInvalidRequestEntities() throws Exception {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"id\":-1}".getBytes());

        try {
            final Class<?> klass = Example.class;
            provider.readFrom((Class<Object>) klass,
                              Example.class,
                              new Annotation[]{ valid },
                              MediaType.APPLICATION_JSON_TYPE,
                              new MultivaluedMapImpl(),
                              entity);
            fail("should have thrown a WebApplicationException but didn't");
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(),
                       is(422));

            assertThat((String) e.getResponse().getEntity(),
                       is("The request entity had the following errors:\n" +
                                  "  * id must be greater than or equal to 0 (was -1)\n"));
        }
    }

    @Test
    public void returnsA400ForMalformedRequestEntities() throws Exception {
        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"id\":-1d".getBytes());

        try {
            final Class<?> klass = Example.class;
            provider.readFrom((Class<Object>) klass,
                              Example.class,
                              NONE,
                              MediaType.APPLICATION_JSON_TYPE,
                              new MultivaluedMapImpl(),
                              entity);
            fail("should have thrown a WebApplicationException but didn't");
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(),
                       is(400));
        }
    }

    @Test
    public void serializesResponseEntities() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final Example example = new Example();
        example.id = 500;

        provider.writeTo(example,
                         Example.class,
                         Example.class,
                         NONE,
                         MediaType.APPLICATION_JSON_TYPE,
                         new StringKeyObjectValueIgnoreCaseMultivaluedMap(),
                         output);

        assertThat(output.toString(),
                   is("{\"id\":500}"));
    }
}
