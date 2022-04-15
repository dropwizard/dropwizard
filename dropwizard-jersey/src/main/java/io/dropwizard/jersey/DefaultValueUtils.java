package io.dropwizard.jersey;

import org.jetbrains.annotations.Nullable;

import javax.ws.rs.DefaultValue;
import java.lang.annotation.Annotation;

public final class DefaultValueUtils {
    private DefaultValueUtils() {
    }

    /**
     * Returns the value of the {@link DefaultValue#value()} if found in annotations.
     *
     * @param annotations Array of annotations (can be null).
     * @return Value of {@link DefaultValue#value()} if found, otherwise null.
     * @since 2.0
     */
    @Nullable
    public static String getDefaultValue(final Annotation[] annotations) {
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (DefaultValue.class == annotation.annotationType()) {
                    return ((DefaultValue) annotation).value();
                }
            }
        }
        return null;
    }
}
