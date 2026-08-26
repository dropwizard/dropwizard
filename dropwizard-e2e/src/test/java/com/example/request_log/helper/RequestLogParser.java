package com.example.request_log.helper;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestLogParser {
    public static class ExtractionPattern {
        private ExtractionPattern() {
        }

        public static final String REMOTE_HOST = "(?<remoteHost>\\S+)";
        // Field 2 is technically 'ident'. Never seen that used.
        // Using this spot for the "forwarding host" IP (e.g. load balancer) is a useful alternative convention.
        // Whatever people do, field name doesn't affect the test results.
        public static final String FORWARDING_HOST = "(?<forwardingHost>\\S+)";
        public static final String REMOTE_USER = "(?<remoteUser>\\S+)";
        public static final String TIMESTAMP = "\\[(?<timestamp>[^\\]]+)\\]";
        public static final String METHOD = "(?<method>\\S+)";
        public static final String URI = "(?<uri>\\S+)";
        public static final String PROTOCOL = "(?<protocol>\\S+)";
        public static final String STATUS = "(?<status>\\d{3})";
        public static final String BYTES = "(?<bytes>\\d+|-)";
        public static final String REFERER = "(?<referer>[^\"]*)";
        public static final String USER_AGENT = "(?<userAgent>[^\"]*)";
        public static final String DURATION_MS = "(?<durationMs>-?\\d+)";
    }

    private static final Pattern NAMED_GROUP_DECLARATION = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

    // A parsed log line. Fields are exposed via typed convenience getters where common, and via
    // get(String) for anything else. Names must match the (?<name>...) groups in the parser regex.
    public static class LogLine {
        private final String rawLine;
        private final Map<String, String> fields;

        public LogLine(String rawLine, Map<String, String> fields) {
            this.rawLine = rawLine;
            this.fields = fields;
        }

        public String getRawLine() {
            return rawLine;
        }

        // Returns the captured value for a named group. Throws if group does not exist
        public String get(String name) {
            if (!fields.containsKey(name)) {
                throw new IllegalArgumentException("No group '" + name + "' in parsed log line: " + rawLine
                    + " (available groups: " + fields.keySet() + ")");
            }
            return fields.get(name);
        }

        // Typed convenience getters for the common fields. These just delegate to get(String).
        public String getRemoteHost() {
            return get("remoteHost");
        }

        public String getForwardingHost() {
            return get("forwardingHost");
        }

        public String getRemoteUser() {
            return get("remoteUser");
        }

        public String getTimestamp() {
            return get("timestamp");
        }

        public String getMethod() {
            return get("method");
        }

        public String getUri() {
            return get("uri");
        }

        public String getProtocol() {
            return get("protocol");
        }

        public String getStatus() {
            return get("status");
        }

        public String getBytes() {
            return get("bytes");
        }

        public String getReferer() {
            return get("referer");
        }

        public String getUserAgent() {
            return get("userAgent");
        }

        public String getDurationMs() {
            return get("durationMs");
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LogLine logLine = (LogLine) o;
            return Objects.equals(rawLine, logLine.rawLine)
                && Objects.equals(fields, logLine.fields);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rawLine, fields);
        }

        @Override
        public String toString() {
            return "LogLine" + fields;
        }
    }

    private final Pattern requestLogPattern;
    private final Set<String> groupNames;

    public RequestLogParser(String requestLogRegex) {
        this.requestLogPattern = Pattern.compile(requestLogRegex);
        this.groupNames = extractGroupNames(requestLogRegex);
    }

    private static Set<String> extractGroupNames(String regex) {
        final Set<String> names = new LinkedHashSet<>();
        final Matcher m = NAMED_GROUP_DECLARATION.matcher(regex);
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }

    public LogLine parseLog(String line) {
        final Matcher m = requestLogPattern.matcher(line);
        assertThat(m).matches();
        final Map<String, String> fields = new LinkedHashMap<>();
        for (String name : groupNames) {
            fields.put(name, m.group(name));
        }
        return new LogLine(line, fields);
    }
}
