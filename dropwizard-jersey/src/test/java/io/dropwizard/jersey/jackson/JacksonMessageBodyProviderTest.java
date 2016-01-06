package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.Validated;
import org.hibernate.validator.constraints.NotEmpty;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SuppressWarnings({"serial", "unchecked"})
public class JacksonMessageBodyProviderTest {
    private static final Annotation[] NONE = new Annotation[0];

    public static class Example {
        @Min(0)
        @JsonProperty
        public int id;

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Example other = (Example) obj;
            return Objects.equals(this.id, other.id);
        }
    }

    public static class ListExample {
        @NotEmpty
        @Valid
        @JsonProperty
        public List<Example> examples;
    }

    public interface Partial1{}
    public interface Partial2{}

    public static class PartialExample {
        @Min(value = 0, groups = Partial1.class)
        @JsonProperty
        public int id;

        @NotNull(groups = Partial2.class)
        @JsonProperty
        public String text;
    }

    @JsonIgnoreType
    public static interface Ignorable {

    }

    @JsonIgnoreType(false)
    public static interface NonIgnorable extends Ignorable {

    }

    private final ObjectMapper mapper = spy(Jackson.newObjectMapper());
    private final JacksonMessageBodyProvider provider =
            new JacksonMessageBodyProvider(mapper);

    @Before
    public void setUp() throws Exception {
        assumeThat(Locale.getDefault().getLanguage(), is("en"));
    }

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
                                             new MultivaluedHashMap<>(),
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
            new MultivaluedHashMap<>(),
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
            new MultivaluedHashMap<>(),
            entity);

        assertThat(obj)
            .isInstanceOf(PartialExample.class);

        assertThat(((PartialExample) obj).id)
            .isEqualTo(1);
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
                              new MultivaluedHashMap<>(),
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
                         new MultivaluedHashMap<>(),
                         output);

        assertThat(output.toString())
                .isEqualTo("{\"id\":500}");
    }

    @Test
    public void returnsValidatedCollectionRequestEntities() throws Exception {
        testValidatedCollectionType(Collection.class,
                new TypeToken<Collection<Example>>() {}.getType());
    }

    @Test
    public void returnsValidatedSetRequestEntities() throws Exception {
        testValidatedCollectionType(Set.class,
                new TypeToken<Set<Example>>() {}.getType());
    }

    @Test
    public void returnsValidatedListRequestEntities() throws Exception {
        testValidatedCollectionType(List.class,
                new TypeToken<List<Example>>() {}.getType());
    }

    private void testValidatedCollectionType(Class<?> klass, Type type) throws IOException {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity = new ByteArrayInputStream("[{\"id\":1}, {\"id\":2}]".getBytes());

        final Object obj = provider.readFrom((Class<Object>) klass,
                type,
                new Annotation[]{ valid },
                MediaType.APPLICATION_JSON_TYPE,
                new MultivaluedHashMap<>(),
                entity);

        assertThat(obj)
                .isInstanceOf(klass);

        Iterator<Example> iterator = ((Iterable<Example>)obj).iterator();
        assertThat(iterator.next().id).isEqualTo(1);
        assertThat(iterator.next().id).isEqualTo(2);
    }

}
