package com.yammer.dropwizard.jersey.tests;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.core.util.StringKeyObjectValueIgnoreCaseMultivaluedMap;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.InvalidEntityException;
import com.yammer.dropwizard.validation.Validated;
import org.junit.Test;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class JacksonMessageBodyProviderTest {
    private static final Annotation[] NONE = new Annotation[0];

    public static class Example {
        @Min(0)
        @JsonProperty
        int id;
    }

    public interface Partial1{}
    public interface Partial2{}

    public static class PartialExample {
        @Min(value = 0, groups = Partial1.class)
        @JsonProperty
        int id;

        @NotNull(groups = Partial2.class)
        @JsonProperty
        String text;
    }

    @JsonIgnoreType
    public static interface Ignorable {

    }

    @JsonIgnoreType(false)
    public static interface NonIgnorable extends Ignorable {

    }

    private final ObjectMapper mapper = spy(new ObjectMapperFactory().build());
    private final JacksonMessageBodyProvider provider = new JacksonMessageBodyProvider(mapper);

    @Test
    public void readsDeserializableTypes() throws Exception {
        assertThat(provider.isReadable(Example.class, null, null, null))
                .isTrue();
    }

    @Test
    public void writesSerializableTypes() throws Exception {
        assertThat(provider.isWriteable(Example.class, null, null, null))
                .isTrue();
    }

    @Test
    public void doesNotWriteIgnoredTypes() throws Exception {
        assertThat(provider.isWriteable(Ignorable.class, null, null, null))
                .isFalse();
    }

    @Test
    public void writesUnIgnoredTypes() throws Exception {
        assertThat(provider.isWriteable(NonIgnorable.class, null, null, null))
                .isTrue();
    }

    @Test
    public void doesNotReadIgnoredTypes() throws Exception {
        assertThat(provider.isReadable(Ignorable.class, null, null, null))
                .isFalse();
    }

    @Test
    public void readsUnIgnoredTypes() throws Exception {
        assertThat(provider.isReadable(NonIgnorable.class, null, null, null))
                .isTrue();
    }

    @Test
    public void isChunked() throws Exception {
        assertThat(provider.getSize(null, null, null, null, null))
                .isEqualTo(-1);
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

        assertThat(obj)
                .isInstanceOf(Example.class);

        assertThat(((Example) obj).id)
                .isEqualTo(1);
    }

    @Test
    public void returnsPartialValidatedRequestEntities() throws Exception {
        final Validated valid = mock(Validated.class);
        doReturn(Validated.class).when(valid).annotationType();
        when(valid.value()).thenReturn(new Class<?>[]{Partial1.class, Partial2.class});

        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"id\":1,\"text\":\"hello Cemo\"}".getBytes());
        final Class<?> klass = PartialExample.class;

        final Object obj = provider.readFrom((Class<Object>) klass,
            PartialExample.class,
            new Annotation[]{valid},
            MediaType.APPLICATION_JSON_TYPE,
            new MultivaluedMapImpl(),
            entity);

        assertThat(obj)
            .isInstanceOf(PartialExample.class);

        assertThat(((PartialExample) obj).id)
            .isEqualTo(1);
    }

    @Test
    public void returnsPartialValidatedByGroupRequestEntities() throws Exception {
        final Validated valid = mock(Validated.class);
        doReturn(Validated.class).when(valid).annotationType();
        when(valid.value()).thenReturn(new Class<?>[]{Partial1.class});

        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"id\":1}".getBytes());
        final Class<?> klass = PartialExample.class;

        final Object obj = provider.readFrom((Class<Object>) klass,
            PartialExample.class,
            new Annotation[]{valid},
            MediaType.APPLICATION_JSON_TYPE,
            new MultivaluedMapImpl(),
            entity);

        assertThat(obj)
            .isInstanceOf(PartialExample.class);

        assertThat(((PartialExample) obj).id)
            .isEqualTo(1);
    }

    @Test
    public void throwsAnInvalidEntityExceptionForPartialValidatedRequestEntities() throws Exception {
        final Validated valid = mock(Validated.class);
        doReturn(Validated.class).when(valid).annotationType();
        when(valid.value()).thenReturn(new Class<?>[]{Partial1.class, Partial2.class});

        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"id\":1}".getBytes());

        try {
            final Class<?> klass = PartialExample.class;
            provider.readFrom((Class<Object>) klass,
                              PartialExample.class,
                              new Annotation[]{ valid },
                              MediaType.APPLICATION_JSON_TYPE,
                              new MultivaluedMapImpl(),
                              entity);
            failBecauseExceptionWasNotThrown(InvalidEntityException.class);
        } catch(InvalidEntityException e) {
            assertThat(e.getErrors())
                .containsOnly("text may not be null (was null)");
        }
    }

    @Test
    public void returnsValidatedRequestEntities() throws Exception {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"id\":1}".getBytes());
        final Class<?> klass = Example.class;

        final Object obj = provider.readFrom((Class<Object>) klass,
                                             Example.class,
                                             new Annotation[]{ valid },
                                             MediaType.APPLICATION_JSON_TYPE,
                                             new MultivaluedMapImpl(),
                                             entity);

        assertThat(obj)
                .isInstanceOf(Example.class);

        assertThat(((Example) obj).id)
                .isEqualTo(1);
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
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (InvalidEntityException e) {
            assertThat(e.getErrors())
                    .containsOnly("id must be greater than or equal to 0 (was -1)");
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
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (JsonProcessingException e) {
            assertThat(e.getMessage())
                    .startsWith("Unexpected character ('d' (code 100)): " +
                                        "was expecting comma to separate OBJECT entries\n");
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

        assertThat(output.toString())
                .isEqualTo("{\"id\":500}");
    }
}
