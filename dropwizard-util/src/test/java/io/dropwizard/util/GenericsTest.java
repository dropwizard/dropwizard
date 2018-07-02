package io.dropwizard.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@SuppressWarnings("serial")
@RunWith(Parameterized.class)
public class GenericsTest<T> {

    @Parameters(name = "Test {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {Object.class, Object.class, Object.class, Object.class, IllegalStateException.class, "Cannot figure out type parameterization for " + Object.class.getName() },
            {null, null, null, null, NullPointerException.class, null },
            {IntegerList.class, Integer.class, Number.class, Integer.class, null, null },
            {IntegerList.class, Integer.class, Integer.class, Integer.class, null, null },
            {NumberList.class, Number.class, Number.class, Number.class, null, null },
            {IntegerValueMap.class, Object.class, Number.class, Integer.class, null, null },
            {ListOfStringSets.class, Set.class, Set.class, Set.class, null, null },
        });
    }

    private Class<?> klass;
    private Class<?> typeParameter;
    private Class<? super T> bound;
    private Class<?> boundTypeParameter;
    private Class<? extends Exception> expectedException;
    private String expectedMessage;

    public GenericsTest(Class<?> klass, Class<?> typeParameter, Class<? super T> bound, Class<?> boundTypeParameter,
                        Class<? extends Exception> expectedException, String expectedMessage) {
        this.klass = klass;
        this.typeParameter = typeParameter;
        this.bound = bound;
        this.boundTypeParameter = boundTypeParameter;
        this.expectedException = expectedException;
        this.expectedMessage = expectedMessage;
    }

    @Test
    public void testTypeParameter() {
        if (expectedException == null) {
            assertThat(Generics.getTypeParameter(klass)).isEqualTo(typeParameter);
        } else {
            assertThatExceptionOfType(expectedException).isThrownBy(() -> Generics.getTypeParameter(klass))
                .withMessage(expectedMessage);
        }
    }

    @Test
    public void testBoundTypeParameter() {
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
