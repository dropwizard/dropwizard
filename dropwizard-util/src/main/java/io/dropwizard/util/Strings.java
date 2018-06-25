package io.dropwizard.util;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Strings {
    private Strings() {
    }

    /**
     * Returns the given string if it is non-null; the empty string otherwise.
     *
     * @param string the string to test and possibly return
     * @return {@code string} itself if it is non-null; {@code ""} if it is null
     */
    public static String nullToEmpty(@Nullable String string) {
        return (string == null) ? "" : string;
    }

    /**
     * Returns the given string if it is nonempty; {@code null} otherwise.
     *
     * @param string the string to test and possibly return
     * @return {@code string} itself if it is nonempty; {@code null} if it is empty or null
     */
    @Nullable
    public static String emptyToNull(@Nullable String string) {
        return isNullOrEmpty(string) ? null : string;
    }

    /**
     * Returns {@code true} if the given string is null or is the empty string.
     *
     * <p>Consider normalizing your string references with {@link #nullToEmpty}. If you do, you can
     * use {@link String#isEmpty()} instead of this method, and you won't need special null-safe forms
     * of methods like {@link String#toUpperCase} either. Or, if you'd like to normalize "in the other
     * direction," converting empty strings to {@code null}, you can use {@link #emptyToNull}.
     *
     * @param string a string reference to check
     * @return {@code true} if the string is null or is the empty string
     */
    public static boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Returns a string consisting of a specific number of concatenated copies of an input string. For
     * example, {@code repeat("hey", 3)} returns the string {@code "heyheyhey"}.
     *
     * @param string any non-null string
     * @param count the number of times to repeat it; a nonnegative integer
     * @return a string containing {@code string} repeated {@code count} times (the empty string if
     *     {@code count} is zero)
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public static String repeat(String string, int count) {
        requireNonNull(string);

        if (count <= 1) {
            if (count < 0) {
                throw new IllegalArgumentException("invalid count: " + count);
            }
            return (count == 0) ? "" : string;
        }

        // IF YOU MODIFY THE CODE HERE, you must update StringsRepeatBenchmark
        final int len = string.length();
        final long longSize = (long) len * (long) count;
        final int size = (int) longSize;
        if (size != longSize) {
            throw new ArrayIndexOutOfBoundsException("Required array size too large: " + longSize);
        }

        final char[] array = new char[size];
        string.getChars(0, len, array, 0);
        int n;
        for (n = len; n < size - n; n <<= 1) {
            System.arraycopy(array, 0, array, n, n);
        }
        System.arraycopy(array, 0, array, n, size - n);
        return new String(array);
    }
}
