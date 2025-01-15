/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package io.dropwizard.validation;

import org.jspecify.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities used for message interpolation.
 *
 * @author Guillaume Smet
 * @since 2.0.3
 */
public final class InterpolationHelper {

    /**
     * A constant representing the '&#123;' character.
     */
    public static final char BEGIN_TERM = '{';
    /**
     * A constant representing the '&#125;' character.
     */
    public static final char END_TERM = '}';
    /**
     * A constant representing the '&#36;' character.
     */
    public static final char EL_DESIGNATOR = '$';
    /**
     * A constant representing the '&#92;' character.
     */
    public static final char ESCAPE_CHARACTER = '\\';

    private static final Pattern ESCAPE_MESSAGE_PARAMETER_PATTERN = Pattern.compile("([\\" + ESCAPE_CHARACTER + BEGIN_TERM + END_TERM + EL_DESIGNATOR + "])");

    private InterpolationHelper() {
    }

    /**
     * Escapes a string with the {@link #ESCAPE_MESSAGE_PARAMETER_PATTERN}.
     *
     * @param messageParameter the string to escape
     * @return the escaped string or {@code null}, if the input was {@code null}
     */
    @Nullable
    public static String escapeMessageParameter(@Nullable String messageParameter) {
        if (messageParameter == null) {
            return null;
        }
        return ESCAPE_MESSAGE_PARAMETER_PATTERN.matcher(messageParameter).replaceAll(Matcher.quoteReplacement(String.valueOf(ESCAPE_CHARACTER)) + "$1");
    }
}
