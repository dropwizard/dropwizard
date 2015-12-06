package io.dropwizard.configuration;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.Mark;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link ConfigurationException} for errors parsing a configuration file.
 */
public class ConfigurationParsingException extends ConfigurationException {
    private static final long serialVersionUID = 1L;

    static class Builder {
        private static final int MAX_SUGGESTIONS = 5;

        private String summary;
        private String detail = "";
        private List<JsonMappingException.Reference> fieldPath = Collections.emptyList();
        private int line = -1;
        private int column = -1;
        private Exception cause = null;
        private List<String> suggestions = new ArrayList<>();
        private String suggestionBase = null;
        private boolean suggestionsSorted = false;

        Builder(String summary) {
            this.summary = summary;
        }

        /**
         * Returns a brief message summarizing the error.
         *
         * @return a brief message summarizing the error.
         */
        public String getSummary() {
            return summary.trim();
        }

        /**
         * Returns a detailed description of the error.
         *
         * @return a detailed description of the error or the empty String if there is none.
         */
        public String getDetail() {
            return detail.trim();
        }

        /**
         * Determines if a detailed description of the error has been set.
         *
         * @return true if there is a detailed description of the error; false if there is not.
         */
        public boolean hasDetail() {
            return detail != null && !detail.isEmpty();
        }

        /**
         * Returns the path to the problematic JSON field, if there is one.
         *
         * @return a {@link List} with each element in the path in order, beginning at the root; or
         *         an empty list if there is no JSON field in the context of this error.
         */
        public List<JsonMappingException.Reference> getFieldPath() {
            return fieldPath;
        }

        /**
         * Determines if the path to a JSON field has been set.
         *
         * @return true if the path to a JSON field has been set for the error; false if no path has
         *         yet been set.
         */
        public boolean hasFieldPath() {
            return fieldPath != null && !fieldPath.isEmpty();
        }

        /**
         * Returns the line number of the source of the problem.
         * <p/>
         * Note: the line number is indexed from zero.
         *
         * @return the line number of the source of the problem, or -1 if unknown.
         */
        public int getLine() {
            return line;
        }

        /**
         * Returns the column number of the source of the problem.
         * <p/>
         * Note: the column number is indexed from zero.
         *
         * @return the column number of the source of the problem, or -1 if unknown.
         */
        public int getColumn() {
            return column;
        }

        /**
         * Determines if a location (line and column numbers) have been set.
         *
         * @return true if both a line and column number has been set; false if only one or neither
         *         have been set.
         */
        public boolean hasLocation() {
            return line > -1 && column > -1;
        }

        /**
         * Returns a list of suggestions.
         * <p/>
         * If a {@link #getSuggestionBase() suggestion-base} has been set, the suggestions will be
         * sorted according to the suggestion-base such that suggestions close to the base appear
         * first in the list.
         *
         * @return a list of suggestions, or the empty list if there are no suggestions available.
         */
        public List<String> getSuggestions() {

            if (suggestionsSorted || !hasSuggestionBase()) {
                return suggestions;
            }

            Collections.sort(suggestions, new LevenshteinComparator(getSuggestionBase()));
            suggestionsSorted = true;

            return suggestions;
        }

        /**
         * Determines whether suggestions are available.
         *
         * @return true if suggestions are available; false if they are not.
         */
        public boolean hasSuggestions() {
            return suggestions != null && !suggestions.isEmpty();
        }

        /**
         * Returns the base for ordering suggestions.
         * <p/>
         * Suggestions will be ordered such that suggestions closer to the base will appear first.
         *
         * @return the base for suggestions.
         */
        public String getSuggestionBase() {
            return suggestionBase;
        }

        /**
         * Determines whether a suggestion base is available.
         * <p/>
         * If no base is available, suggestions will not be sorted.
         *
         * @return true if a base is available for suggestions; false if there is none.
         */
        public boolean hasSuggestionBase() {
            return suggestionBase != null && !suggestionBase.isEmpty();
        }

        /**
         * Returns the {@link Exception} that encapsulates the problem itself.
         *
         * @return an Exception representing the cause of the problem, or null if there is none.
         */
        public Exception getCause() {
            return cause;
        }

        /**
         * Determines whether a cause has been set.
         *
         * @return true if there is a cause; false if there is none.
         */
        public boolean hasCause() {
            return cause != null;
        }

        Builder setCause(Exception cause) {
            this.cause = cause;
            return this;
        }

        Builder setDetail(String detail) {
            this.detail = detail;
            return this;
        }

        Builder setFieldPath(List<JsonMappingException.Reference> fieldPath) {
            this.fieldPath = fieldPath;
            return this;
        }

        Builder setLocation(JsonLocation location) {
            return location == null
                    ? this
                    : setLocation(location.getLineNr(), location.getColumnNr());
        }

        Builder setLocation(Mark mark) {
            return mark == null
                    ? this
                    : setLocation(mark.getLine(), mark.getColumn());
        }

        Builder setLocation(int line, int column) {
            this.line = line;
            this.column = column;
            return this;
        }

        Builder addSuggestion(String suggestion) {
            this.suggestionsSorted = false;
            this.suggestions.add(suggestion);
            return this;
        }

        Builder addSuggestions(Collection<String> suggestions) {
            this.suggestionsSorted = false;
            this.suggestions.addAll(suggestions);
            return this;
        }

        Builder setSuggestionBase(String base) {
            this.suggestionBase = base;
            this.suggestionsSorted = false;
            return this;
        }

        ConfigurationParsingException build(String path) {
            final StringBuilder sb = new StringBuilder(getSummary());
            if (hasFieldPath()) {
                sb.append(" at: ").append(buildPath(getFieldPath()));
            } else if (hasLocation()) {
                sb.append(" at line: ").append(getLine() + 1)
                        .append(", column: ").append(getColumn() + 1);
            }

            if (hasDetail()) {
                sb.append("; ").append(getDetail());
            }

            if (hasSuggestions()) {
                final List<String> suggestions = getSuggestions();
                sb.append(NEWLINE).append("    Did you mean?:").append(NEWLINE);
                final Iterator<String> it = suggestions.iterator();
                int i = 0;
                while (it.hasNext() && i < MAX_SUGGESTIONS) {
                    sb.append("      - ").append(it.next());
                    i++;
                    if (it.hasNext()) {
                        sb.append(NEWLINE);
                    }
                }

                final int total = suggestions.size();
                if (i < total) {
                    sb.append("        [").append(total - i).append(" more]");
                }
            }

            return hasCause()
                    ? new ConfigurationParsingException(path, sb.toString(), getCause())
                    : new ConfigurationParsingException(path, sb.toString());
        }

        private String buildPath(Iterable<JsonMappingException.Reference> path) {
            final StringBuilder sb = new StringBuilder();
            if (path != null) {
                final Iterator<JsonMappingException.Reference> it = path.iterator();
                while (it.hasNext()) {
                    final JsonMappingException.Reference reference = it.next();
                    final String name = reference.getFieldName();

                    // append either the field name or list index
                    if (name == null) {
                        sb.append('[').append(reference.getIndex()).append(']');
                    } else {
                        sb.append(name);
                    }

                    if (it.hasNext()) {
                        sb.append('.');
                    }
                }
            }
            return sb.toString();
        }

        protected static class LevenshteinComparator implements Comparator<String>, Serializable {
            private static final long serialVersionUID = 1L;

            private String base;

            public LevenshteinComparator(String base) {
                this.base = base;
            }

            /**
             * Compares two Strings with respect to the base String, by Levenshtein distance.
             * <p/>
             * The input that is the closest match to the base String will sort before the other.
             *
             * @param a an input to compare relative to the base.
             * @param b an input to compare relative to the base.
             *
             * @return -1 if {@code a} is closer to the base than {@code b}; 1 if {@code b} is
             *         closer to the base than {@code a}; 0 if both {@code a} and {@code b} are
             *         equally close to the base.
             */
            @Override
            public int compare(String a, String b) {

                // shortcuts
                if (a.equals(b)) {
                    return 0; // comparing the same value; don't bother
                } else if (a.equals(base)) {
                    return -1; // a is equal to the base, so it's always first
                } else if (b.equals(base)) {
                    return 1; // b is equal to the base, so it's always first
                }

                // determine which of the two is closer to the base and order it first
                return Integer.compare(StringUtils.getLevenshteinDistance(a, base),
                        StringUtils.getLevenshteinDistance(b, base));
            }

            private void writeObject(ObjectOutputStream stream) throws IOException {
                stream.defaultWriteObject();
            }

            private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
                stream.defaultReadObject();
            }
        }
    }

    /**
     * Create a mutable {@link Builder} to incrementally build a {@link ConfigurationParsingException}.
     *
     * @param brief the brief summary of the error.
     *
     * @return a mutable builder to incrementally build a {@link ConfigurationParsingException}.
     */
    static Builder builder(String brief) {
        return new Builder(brief);
    }

    /**
     * Creates a new ConfigurationParsingException for the given path with the given error.
     *
     * @param path   the bad configuration path
     * @param msg    the full error message
     */
    private ConfigurationParsingException(String path, String msg) {
        super(path, ImmutableSet.of(msg));
    }

    /**
     * Creates a new ConfigurationParsingException for the given path with the given error.
     *
     * @param path   the bad configuration path
     * @param msg    the full error message
     * @param cause  the cause of the parsing error.
     */
    private ConfigurationParsingException(String path, String msg, Throwable cause) {
        super(path, ImmutableSet.of(msg), cause);
    }

}
