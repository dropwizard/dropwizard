package io.dropwizard.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SuppressWarnings("serial")
public class GenericsTest<T> {

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(Object.class, Object.class, Object.class, Object.class, IllegalStateException.class, "Cannot figure out type parameterization for " + Object.class.getName() ),
            Arguments.of(null, null, null, null, NullPointerException.class, null ),
            Arguments.of(IntegerList.class, Integer.class, Number.class, Integer.class, null, null),
            Arguments.of(IntegerList.class, Integer.class, Integer.class, Integer.class, null, null),
            Arguments.of(NumberList.class, Number.class, Number.class, Number.class, null, null),
            Arguments.of(IntegerValueMap.class, Object.class, Number.class, Integer.class, null, null ),
            Arguments.of(ListOfStringSets.class, Set.class, Set.class, Set.class, null, null)
        );
    }


    @ParameterizedTest
    @MethodSource("data")
    public void testTypeParameter(Class<?> klass, Class<?> typeParameter, Class<? super T> bound, Class<?> boundTypeParameter,
                                  Class<? extends Exception> expectedException, String expectedMessage) {
        if (expectedException == null) {
            assertThat(Generics.getTypeParameter(klass)).isEqualTo(typeParameter);
        } else {
            assertThatExceptionOfType(expectedException).isThrownBy(() -> Generics.getTypeParameter(klass))
                .withMessage(expectedMessage);
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testBoundTypeParameter(Class<?> klass, Class<?> typeParameter, Class<? super T> bound, Class<?> boundTypeParameter,
                                       Class<? extends Exception> expectedException, String expectedMessage) {
        if (expectedException == null) {
            assertThat(Generics.getTypeParameter(klass, bound)).isEqualTo(boundTypeParameter);
        } else {
            assertThatExceptionOfType(expectedException).isThrownBy(() -> Generics.getTypeParameter(klass, bound))
                .withMessage(expectedMessage);
        }
    }

    public static class IntegerList extends ArrayList<Integer> { }
    public static class NumberList<V extends Number> extends ArrayList<V> { }
    public static class IntegerValueMap<K> extends HashMap<K, Integer> { }
    public static class ListOfStringSets extends ArrayList<Set<String>> { }
}
