/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package io.dropwizard.validation;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities used for message interpolation.
 *
 * @author Guillaume Smet
 * @since 2.0.3
 */
public final class InterpolationHelper {

    public static final char BEGIN_TERM = '{';
    public static final char END_TERM = '}';
    public static final char EL_DESIGNATOR = '$';
    public static final char ESCAPE_CHARACTER = '\\';

    private static final Pattern ESCAPE_MESSAGE_PARAMETER_PATTERN = Pattern.compile("([\\" + ESCAPE_CHARACTER + BEGIN_TERM + END_TERM + EL_DESIGNATOR + "])");

    private InterpolationHelper() {
    }

    @Nullable
    public static String escapeMessageParameter(@Nullable String messageParameter) {
        if (messageParameter == null) {
            return null;
        }
        return ESCAPE_MESSAGE_PARAMETER_PATTERN.matcher(messageParameter).replaceAll(Matcher.quoteReplacement(String.valueOf(ESCAPE_CHARACTER)) + "$1");
    }
}
