package com.yammer.dropwizard.jersey;

import com.google.common.collect.ImmutableList;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.core.util.StringKeyObjectValueIgnoreCaseMultivaluedMap;
import com.yammer.dropwizard.json.Json;
import com.yammer.dropwizard.validation.InvalidEntityException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnoreType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.Test;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.startsWith;
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

    @JsonIgnoreType
    public static interface Ignorable {

    }

    @JsonIgnoreType(false)
    public static interface NonIgnorable extends Ignorable {

    }

    private final Json json = spy(new Json());

    private final JacksonMessageBodyProvider provider = new JacksonMessageBodyProvider(json);

    @Test
    public void readsDeserializableTypes() throws Exception {
        assertThat(provider.isReadable(Example.class, null, null, null),
                   is(true));

        verify(json).canDeserialize(Example.class);
    }

    @Test
    public void writesSerializableTypes() throws Exception {
        assertThat(provider.isWriteable(Example.class, null, null, null),
                   is(true));

        verify(json).canSerialize(Example.class);
    }

    @Test
    public void doesNotWriteIgnoredTypes() throws Exception {
        assertThat(provider.isWriteable(Ignorable.class, null, null, null),
                   is(false));
    }

    @Test
    public void doesNotWriteDefaultIgnoredTypes() throws Exception {
        assertThat(provider.isWriteable(byte[].class, null, null, null),
                   is(false));
        assertThat(provider.isWriteable(String.class, null, null, null),
            is(false));

        for (final Class<?> cls : JacksonMessageBodyProvider.DEFAULT_IGNORE) {
            assertThat(provider.isWriteable(cls, null, null, null),
                is(false));
        }
    }

    @Test
    public void doesNotWriteNonJsonMediaTypes() throws Exception {
        assertThat(provider.isWriteable(Example.class, null, null, MediaType.APPLICATION_ATOM_XML_TYPE),
                   is(false));
        assertThat(provider.isWriteable(Example.class, null, null, MediaType.APPLICATION_OCTET_STREAM_TYPE),
            is(false));
        assertThat(provider.isWriteable(Example.class, null, null, MediaType.TEXT_XML_TYPE),
            is(false));
    }

    @Test
    public void writeJsonMediaTypes() throws Exception {
        assertThat(provider.isWriteable(Example.class, null, null, MediaType.APPLICATION_JSON_TYPE),
                   is(true));
        assertThat(provider.isWriteable(Example.class, null, null, MediaType.valueOf("application/foo+json")),
            is(true));
    }

    @Test
    public void writePossibleJsonMediaTypes() throws Exception {
        assertThat(provider.isWriteable(Example.class, null, null, null),
                   is(true));
        assertThat(provider.isWriteable(Example.class, null, null, MediaType.WILDCARD_TYPE),
            is(true));
        assertThat(provider.isWriteable(Example.class, null, null, MediaType.valueOf("application/*")),
            is(true));
    }

    @Test
    public void writesUnIgnoredTypes() throws Exception {
        assertThat(provider.isWriteable(NonIgnorable.class, null, null, null),
                   is(true));
    }

    @Test
    public void doesNotReadIgnoredTypes() throws Exception {
        assertThat(provider.isReadable(Ignorable.class, null, null, null),
                   is(false));
    }

    @Test
    public void readsUnIgnoredTypes() throws Exception {
        assertThat(provider.isReadable(NonIgnorable.class, null, null, null),
                   is(true));
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
    public void throwsAnInvalidEntityExceptionForInvalidRequestEntities() throws Exception {
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
        } catch (InvalidEntityException e) {
            assertThat(e.getErrors(),
                       is(ImmutableList.of("id must be greater than or equal to 0 (was -1)")));
        }
    }

    @Test
    public void throwsAJsonProcessingExceptionForMalformedRequestEntities() throws Exception {
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
        } catch (JsonProcessingException e) {
            assertThat(e.getMessage(),
                       startsWith("Unexpected character ('d' (code 100)): was expecting comma to separate OBJECT entries\n"));
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
