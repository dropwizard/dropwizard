package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@EnabledIf("isDefaultLocaleEnglish")
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
        public List<Example> examples = Collections.emptyList();
    }

    public interface Partial1 {
    }

    public interface Partial2 extends Default {
    }

    public static class PartialExample {
        @Min(value = 0, groups = Partial1.class)
        @JsonProperty
        public int id;

        @NotNull(groups = Partial2.class)
        @Nullable
        @JsonProperty
        public String text;
    }

    @JsonIgnoreType
    public interface Ignorable {

    }

    @JsonIgnoreType(false)
    public interface NonIgnorable extends Ignorable {

    }

    private final ObjectMapper mapper = spy(Jackson.newObjectMapper());
    private final JacksonMessageBodyProvider provider =
            new JacksonMessageBodyProvider(mapper);

    @Test
    void readsDeserializableTypes() {
        assertThat(provider.isReadable(Example.class, null, new Annotation[0], null))
                .isTrue();
    }

    @Test
    void writesSerializableTypes() {
        assertThat(provider.isWriteable(Example.class, null, new Annotation[0], null))
                .isTrue();
    }

    @Test
    void doesNotWriteIgnoredTypes() {
        assertThat(provider.isWriteable(Ignorable.class, null, new Annotation[0], null))
                .isFalse();
    }

    @Test
    void writesUnIgnoredTypes() {
        assertThat(provider.isWriteable(NonIgnorable.class, null, new Annotation[0], null))
                .isTrue();
    }

    @Test
    void doesNotReadIgnoredTypes() {
        assertThat(provider.isReadable(Ignorable.class, null, new Annotation[0], null))
                .isFalse();
    }

    @Test
    void readsUnIgnoredTypes() {
        assertThat(provider.isReadable(NonIgnorable.class, null, new Annotation[0], null))
                .isTrue();
    }

    @Test
    void isChunked() {
        assertThat(provider.getSize(null, null, null, new Annotation[0], null))
                .isEqualTo(-1);
    }

    @Test
    void deserializesRequestEntities() throws Exception {
        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"id\":1}".getBytes(StandardCharsets.UTF_8));
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
    void returnsPartialValidatedRequestEntities() throws Exception {
        final Validated valid = mock(Validated.class);
        doReturn(Validated.class).when(valid).annotationType();
        when(valid.value()).thenReturn(new Class<?>[]{Partial1.class, Partial2.class});

        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"id\":1,\"text\":\"hello Cemo\"}".getBytes(StandardCharsets.UTF_8));
        final Class<?> klass = PartialExample.class;

        final Object obj = provider.readFrom((Class<Object>) klass,
            PartialExample.class,
            new Annotation[]{valid},
            MediaType.APPLICATION_JSON_TYPE,
            new MultivaluedHashMap<>(),
            entity);

        assertThat(obj)
            .isInstanceOfSatisfying(PartialExample.class, partialExample ->
                assertThat(partialExample.id).isEqualTo(1));
    }

    @Test
    void returnsPartialValidatedByGroupRequestEntities() throws Exception {
        final Validated valid = mock(Validated.class);
        doReturn(Validated.class).when(valid).annotationType();
        when(valid.value()).thenReturn(new Class<?>[]{Partial1.class});

        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"id\":1}".getBytes(StandardCharsets.UTF_8));
        final Class<?> klass = PartialExample.class;

        final Object obj = provider.readFrom((Class<Object>) klass,
            PartialExample.class,
            new Annotation[]{valid},
            MediaType.APPLICATION_JSON_TYPE,
            new MultivaluedHashMap<>(),
            entity);

        assertThat(obj)
            .isInstanceOfSatisfying(PartialExample.class, partialExample ->
                assertThat(partialExample.id).isEqualTo(1));
    }

    @Test
    void throwsAJsonProcessingExceptionForMalformedRequestEntities() {
        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"id\":-1d".getBytes(StandardCharsets.UTF_8));
        final Class<?> klass = Example.class;

        assertThatExceptionOfType(JsonProcessingException.class)
            .isThrownBy(() -> provider.readFrom((Class<Object>) klass,
                              Example.class,
                              NONE,
                              MediaType.APPLICATION_JSON_TYPE,
                              new MultivaluedHashMap<>(),
                              entity))
            .withMessageStartingWith("Unexpected character ('d' (code 100)): " +
                                        "was expecting comma to separate Object entries\n");
    }

    @Test
    void serializesResponseEntities() throws Exception {
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

        assertThat(output)
                .hasToString("{\"id\":500}");
    }

    @Test
    void returnsValidatedCollectionRequestEntities() throws Exception {
        testValidatedCollectionType(Collection.class,
            new TypeReference<Collection<Example>>() {
            }.getType());
    }

    @Test
    void returnsValidatedSetRequestEntities() throws Exception {
        testValidatedCollectionType(Set.class,
            new TypeReference<Set<Example>>() {
            }.getType());
    }

    @Test
    void returnsValidatedListRequestEntities() throws Exception {
        testValidatedCollectionType(List.class,
            new TypeReference<List<Example>>() {
            }.getType());
    }

    private void testValidatedCollectionType(Class<?> klass, Type type) throws IOException {
        final Annotation valid = mock(Annotation.class);
        doReturn(Valid.class).when(valid).annotationType();

        final ByteArrayInputStream entity = new ByteArrayInputStream("[{\"id\":1}, {\"id\":2}]".getBytes(StandardCharsets.UTF_8));

        final Object obj = provider.readFrom((Class<Object>) klass,
            type,
            new Annotation[]{valid},
            MediaType.APPLICATION_JSON_TYPE,
            new MultivaluedHashMap<>(),
            entity);

        assertThat(obj).isInstanceOf(klass);
        assertThat((Iterable<Example>) obj)
            .extracting(item -> item.id)
            .contains(1, 2);
    }

    private static boolean isDefaultLocaleEnglish() {
        return "en".equals(Locale.getDefault().getLanguage());
    }
}
