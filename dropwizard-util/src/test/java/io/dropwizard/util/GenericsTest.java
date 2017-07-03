package io.dropwizard.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
        });
    }

    private Class<?> klass;
    private Class<?> typeParameter;
    private Class<? super T> bound;
    private Class<?> boundTypeParameter;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public GenericsTest(Class<?> klass, Class<?> typeParameter, Class<? super T> bound, Class<?> boundTypeParameter, Class<? extends Exception> expectedException, String expectedMessage) {
        this.klass = klass;
        this.typeParameter = typeParameter;
        this.bound = bound;
        this.boundTypeParameter = boundTypeParameter;

        if (expectedException != null) {
            thrown.expect(expectedException);
            if (expectedMessage != null)
                thrown.expectMessage(expectedMessage);
        }
    }

    @Test
    public void testTypeParameter() {
        assertThat(Generics.getTypeParameter(klass)).isEqualTo(typeParameter);
    }
    
    @Test
    public void testBoundTypeParameter() {
        assertThat(Generics.getTypeParameter(klass, bound)).isEqualTo(boundTypeParameter);
    }
    
    public static class IntegerList extends ArrayList<Integer> { }
    public static class NumberList<V extends Number> extends ArrayList<V> { }
    public static class IntegerValueMap<K> extends HashMap<K, Integer> { }
}
