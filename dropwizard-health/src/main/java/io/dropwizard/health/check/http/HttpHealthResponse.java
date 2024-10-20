package io.dropwizard.health.check.http;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public class HttpHealthResponse {
    private final int status;
    @NonNull
    private final String body;

    public HttpHealthResponse(final int status, @NonNull final String body) {
        this.status = status;
        this.body = Objects.requireNonNull(body);
    }

    public int getStatus() {
        return status;
    }

    @NonNull
    public String getBody() {
        return body;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HttpHealthResponse)) {
            return false;
        }
        final HttpHealthResponse that = (HttpHealthResponse) other;
        return status == that.status
            && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, body);
    }

    @Override
    public String toString() {
        return "HttpHealthResponse{" +
            "status=" + status +
            ", body='" + body + '\'' +
            '}';
    }
}
