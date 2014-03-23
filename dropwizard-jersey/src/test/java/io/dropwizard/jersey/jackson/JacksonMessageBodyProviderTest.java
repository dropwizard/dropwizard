package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.reflect.TypeToken;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.core.util.StringKeyObjectValueIgnoreCaseMultivaluedMap;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.ConstraintViolations;
import io.dropwizard.validation.Validated;
import org.hibernate.validator.constraints.NotEmpty;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

// TODO: 4/24/13 <coda> -- move JacksonMessageBodyProviderTest to JerseyTest

@SuppressWarnings("unchecked")
public class JacksonMessageBodyProviderTest {
    private static final Annotation[] NONE = new Annotation[0];

    public static class Example {
        @Min(0)
        @JsonProperty
        int id;

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            return Objects.equal(this.id, obj);
        }
    }

    public static class ListExample {
        @NotEmpty
        @Valid
        @JsonProperty
        List<Example> examples;
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

    private final ObjectMapper mapper = spy(Jackson.newObjectMapper());
    private final JacksonMessageBodyProvider provider =
            new JacksonMessageBodyProvider(mapper,
                                           Validation.buildDefaultValidatorFactory().getValidator());

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
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch(ConstraintViolationException e) {
            assertThat(ConstraintViolations.formatUntyped(e.getConstraintViolations()))
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
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException e) {
            assertThat(ConstraintViolations.formatUntyped(e.getConstraintViolations()))
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

    @Test(expected = ConstraintViolationException.class)
    public void throwsAConstraintViolationExceptionForEmptyRequestEntities() throws Exception {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final Class<?> klass = Example.class;
        provider.readFrom((Class<Object>) klass,
                Example.class,
                new Annotation[]{valid},
                MediaType.APPLICATION_JSON_TYPE,
                new MultivaluedMapImpl(),
                null);
    }

    @Test
    public void returnsValidatedArrayRequestEntities() throws Exception {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity = new ByteArrayInputStream("[{\"id\":1}, {\"id\":2}]".getBytes());
        final Class<?> klass = Example[].class;

        final Object obj = provider.readFrom((Class<Object>) klass,
                Example[].class,
                new Annotation[]{ valid },
                MediaType.APPLICATION_JSON_TYPE,
                new MultivaluedMapImpl(),
                entity);

        assertThat(obj)
                .isInstanceOf(Example[].class);

        assertThat(((Example[]) obj)[0].id)
                .isEqualTo(1);
        assertThat(((Example[]) obj)[1].id)
                .isEqualTo(2);
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

    @Test
    public void returnsValidatedMapRequestEntities() throws Exception {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"one\": {\"id\":1}, \"two\": {\"id\":2}}".getBytes());
        final Class<?> klass = Map.class;

        final Object obj = provider.readFrom((Class<Object>) klass,
                new TypeToken<Map<Object, Example>>() {}.getType(),
                new Annotation[]{ valid },
                MediaType.APPLICATION_JSON_TYPE,
                new MultivaluedMapImpl(),
                entity);

        assertThat(obj)
                .isInstanceOf(Map.class);

        Map<Object, Example> map = (Map<Object, Example>) obj;
        assertThat(map.get("one").id).isEqualTo(1);
        assertThat(map.get("two").id).isEqualTo(2);
    }

    private void testValidatedCollectionType(Class<?> klass, Type type) throws IOException {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity = new ByteArrayInputStream("[{\"id\":1}, {\"id\":2}]".getBytes());

        final Object obj = provider.readFrom((Class<Object>) klass,
                type,
                new Annotation[]{ valid },
                MediaType.APPLICATION_JSON_TYPE,
                new MultivaluedMapImpl(),
                entity);

        assertThat(obj)
                .isInstanceOf(klass);

        Iterator<Example> iterator = ((Iterable<Example>)obj).iterator();
        assertThat(iterator.next().id).isEqualTo(1);
        assertThat(iterator.next().id).isEqualTo(2);
    }

    @Test
    public void throwsAnInvalidEntityExceptionForInvalidCollectionRequestEntities() throws Exception {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity = new ByteArrayInputStream("[{\"id\":-1}, {\"id\":-2}]".getBytes());

        try {
            final Class<?> klass = Example.class;
            provider.readFrom((Class<Object>) klass,
                    new TypeToken<Collection<Example>>() {}.getType(),
                    new Annotation[]{ valid },
                    MediaType.APPLICATION_JSON_TYPE,
                    new MultivaluedMapImpl(),
                    entity);
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException e) {
            assertThat(ConstraintViolations.formatUntyped(e.getConstraintViolations()))
                    .contains("id must be greater than or equal to 0 (was -1)",
                            "id must be greater than or equal to 0 (was -2)");
        }
    }

    @Test
    public void throwsASingleInvalidEntityExceptionForInvalidCollectionRequestEntities() throws Exception {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity = new ByteArrayInputStream("[{\"id\":1}, {\"id\":-2}]".getBytes());

        try {
            final Class<?> klass = Example.class;
            provider.readFrom((Class<Object>) klass,
                    new TypeToken<Collection<Example>>() {}.getType(),
                    new Annotation[]{ valid },
                    MediaType.APPLICATION_JSON_TYPE,
                    new MultivaluedMapImpl(),
                    entity);
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException e) {
            assertThat(ConstraintViolations.formatUntyped(e.getConstraintViolations()))
                    .contains("id must be greater than or equal to 0 (was -2)");
        }
    }

    @Test
    public void throwsAnInvalidEntityExceptionForInvalidSetRequestEntities() throws Exception {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity = new ByteArrayInputStream("[{\"id\":-1}, {\"id\":-2}]".getBytes());

        try {
            final Class<?> klass = Example.class;
            provider.readFrom((Class<Object>) klass,
                    new TypeToken<Set<Example>>() {}.getType(),
                    new Annotation[]{ valid },
                    MediaType.APPLICATION_JSON_TYPE,
                    new MultivaluedMapImpl(),
                    entity);
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException e) {
            assertThat(ConstraintViolations.formatUntyped(e.getConstraintViolations()))
                    .contains("id must be greater than or equal to 0 (was -1)",
                            "id must be greater than or equal to 0 (was -2)");
        }
    }

    @Test
    public void throwsAnInvalidEntityExceptionForInvalidListRequestEntities() throws Exception {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity = new ByteArrayInputStream("[{\"id\":-1}, {\"id\":-2}]".getBytes());

        try {
            final Class<?> klass = Example.class;
            provider.readFrom((Class<Object>) klass,
                    new TypeToken<List<Example>>() {}.getType(),
                    new Annotation[]{ valid },
                    MediaType.APPLICATION_JSON_TYPE,
                    new MultivaluedMapImpl(),
                    entity);
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException e) {
            assertThat(ConstraintViolations.formatUntyped(e.getConstraintViolations()))
                    .containsOnly("id must be greater than or equal to 0 (was -1)",
                            "id must be greater than or equal to 0 (was -2)");
        }
    }

    @Test
    public void throwsAnInvalidEntityExceptionForInvalidMapRequestEntities() throws Exception {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"one\": {\"id\":-1}, \"two\": {\"id\":-2}}".getBytes());

        try {
            final Class<?> klass = Example.class;
            provider.readFrom((Class<Object>) klass,
                    new TypeToken<Map<Object, Example>>() {}.getType(),
                    new Annotation[]{ valid },
                    MediaType.APPLICATION_JSON_TYPE,
                    new MultivaluedMapImpl(),
                    entity);
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException e) {
            assertThat(ConstraintViolations.formatUntyped(e.getConstraintViolations()))
                    .contains("id must be greater than or equal to 0 (was -1)",
                            "id must be greater than or equal to 0 (was -2)");
        }
    }

    @Test
    public void returnsValidatedEmbeddedListRequestEntities() throws IOException {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity =
                new ByteArrayInputStream("[ {\"examples\": [ {\"id\":1 } ] } ]".getBytes());
        Class<?> klass = List.class;

        final Object obj = provider.readFrom((Class<Object>) klass,
                new TypeToken<List<ListExample>>() {}.getType(),
                new Annotation[]{ valid },
                MediaType.APPLICATION_JSON_TYPE,
                new MultivaluedMapImpl(),
                entity);

        assertThat(obj)
                .isInstanceOf(klass);

        Iterator<ListExample> iterator = ((Iterable<ListExample>)obj).iterator();
        assertThat(iterator.next().examples.get(0).id).isEqualTo(1);
    }

    @Test
    public void throwsAnInvalidEntityExceptionForInvalidEmbeddedListRequestEntities() throws Exception {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity =
                new ByteArrayInputStream("[ {\"examples\": [ {\"id\":1 } ] }, { } ]".getBytes());

        try {
            final Class<?> klass = List.class;
            provider.readFrom((Class<Object>) klass,
                    new TypeToken<List<ListExample>>() {}.getType(),
                    new Annotation[]{ valid },
                    MediaType.APPLICATION_JSON_TYPE,
                    new MultivaluedMapImpl(),
                    entity);
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException e) {
            assertThat(ConstraintViolations.formatUntyped(e.getConstraintViolations()))
                    .containsOnly("examples may not be empty (was null)");
        }
    }

}
