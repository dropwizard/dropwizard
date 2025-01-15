package io.dropwizard.client;

import io.dropwizard.util.DirectExecutorService;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.util.VersionInfo;
import org.jspecify.annotations.Nullable;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.message.internal.Statuses;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.List;

/**
 * Dropwizard Apache Connector.
 * <p>
 * It's a custom version of Jersey's {@link org.glassfish.jersey.client.spi.Connector}
 * that uses Apache's {@link org.apache.hc.client5.http.classic.HttpClient}
 * as an HTTP transport implementation.
 * </p>
 * <p>
 * It uses a pre-configured HTTP client by {@link io.dropwizard.client.HttpClientBuilder}
 * rather then creates a client from the Jersey configuration.
 * </p>
 * <p>
 * This approach affords to use the extended configuration of
 * the Apache HttpClient in Dropwizard with a fluent interface
 * of JerseyClient.
 * </p>
 */
public class DropwizardApacheConnector implements Connector {

    private static final String ERROR_BUFFERING_ENTITY = "Error buffering the entity.";

    private static final String APACHE_HTTP_CLIENT_VERSION = VersionInfo
            .loadVersionInfo("org.apache.hc.client5", DropwizardApacheConnector.class.getClassLoader())
            .getRelease();

    /**
     * Actual HTTP client
     */
    private final CloseableHttpClient client;
    /**
     * Default HttpUriRequestConfig
     */
    @Nullable
    private final RequestConfig defaultRequestConfig;

    /**
     * Should a chunked encoding be used in POST requests
     */
    private final boolean chunkedEncodingEnabled;

    public DropwizardApacheConnector(CloseableHttpClient client, @Nullable RequestConfig defaultRequestConfig,
                                     boolean chunkedEncodingEnabled) {
        this.client = client;
        this.defaultRequestConfig = defaultRequestConfig;
        this.chunkedEncodingEnabled = chunkedEncodingEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientResponse apply(ClientRequest jerseyRequest) {
        try {
            final HttpUriRequest apacheRequest = buildApacheRequest(jerseyRequest);
            final CloseableHttpResponse apacheResponse = client.execute(apacheRequest);

            final String reasonPhrase = apacheResponse.getReasonPhrase();
            final Response.StatusType status = Statuses.from(apacheResponse.getCode(), reasonPhrase == null ? "" : reasonPhrase);

            final ClientResponse jerseyResponse = new ClientResponse(status, jerseyRequest);
            for (Header header : apacheResponse.getHeaders()) {
                jerseyResponse.getHeaders().computeIfAbsent(header.getName(), k -> new ArrayList<>())
                    .add(header.getValue());
            }

            final HttpEntity httpEntity = apacheResponse.getEntity();
            jerseyResponse.setEntityStream(httpEntity != null ? httpEntity.getContent() :
                    new ByteArrayInputStream(new byte[0]));

            return jerseyResponse;
        } catch (Exception e) {
            throw new ProcessingException(e);
        }
    }

    /**
     * Build a new Apache's {@link HttpUriRequest}
     * from Jersey's {@link org.glassfish.jersey.client.ClientRequest}
     * <p>
     * Convert a method, URI, body, headers and override a user-agent if necessary
     * </p>
     *
     * @param jerseyRequest representation of an HTTP request in Jersey
     * @return a new {@link HttpUriRequest}
     */
    private HttpUriRequest buildApacheRequest(ClientRequest jerseyRequest) {
        HttpUriRequestBase base = new HttpUriRequestBase(jerseyRequest.getMethod(), jerseyRequest.getUri());
        base.setEntity(getHttpEntity(jerseyRequest));
        for (String headerName : jerseyRequest.getHeaders().keySet()) {
            base.addHeader(headerName, jerseyRequest.getHeaderString(headerName));
        }

        final Optional<RequestConfig> requestConfig = addJerseyRequestConfig(jerseyRequest);
        requestConfig.ifPresent(base::setConfig);

        return base;
    }

    private Optional<RequestConfig> addJerseyRequestConfig(ClientRequest clientRequest) {
        final Integer timeout = clientRequest.resolveProperty(ClientProperties.READ_TIMEOUT, Integer.class);
        final Integer connectTimeout = clientRequest.resolveProperty(ClientProperties.CONNECT_TIMEOUT, Integer.class);
        final Boolean followRedirects = clientRequest.resolveProperty(ClientProperties.FOLLOW_REDIRECTS, Boolean.class);

        if (timeout != null || connectTimeout != null || followRedirects != null) {
            final RequestConfig.Builder requestConfig = RequestConfig.copy(defaultRequestConfig);

            if (timeout != null) {
                requestConfig.setResponseTimeout(timeout, TimeUnit.MILLISECONDS);
            }

            if (connectTimeout != null) {
                requestConfig.setConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
            }

            if (followRedirects != null) {
                requestConfig.setRedirectsEnabled(followRedirects);
            }

            return Optional.of(requestConfig.build());
        }

        return Optional.empty();
    }

    /**
     * Get an Apache's {@link HttpEntity}
     * from Jersey's {@link org.glassfish.jersey.client.ClientRequest}
     * <p>
     * Create a custom HTTP entity, because Jersey doesn't provide
     * a request stream or a byte buffer.
     * </p>
     *
     * @param jerseyRequest representation of an HTTP request in Jersey
     * @return a correct {@link HttpEntity} implementation
     */
    @Nullable
    protected HttpEntity getHttpEntity(ClientRequest jerseyRequest) {
        if (jerseyRequest.getEntity() == null) {
            return null;
        }

        return chunkedEncodingEnabled ? new JerseyRequestHttpEntity(jerseyRequest) :
                new BufferedJerseyRequestHttpEntity(jerseyRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<?> apply(final ClientRequest request, final AsyncConnectorCallback callback) {
        // Simulate an asynchronous execution
        return new DirectExecutorService().submit(() -> {
            try {
                callback.response(apply(request));
            } catch (Exception e) {
                callback.failure(e);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Apache-HttpClient/" + APACHE_HTTP_CLIENT_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        // Should not close the client here, because it's managed by the Dropwizard environment
    }

    /**
     * A custom {@link org.apache.hc.core5.http.io.entity.AbstractHttpEntity} that uses
     * a Jersey request as a content source. It's chunked because we don't
     * know the content length beforehand.
     */
    private static class JerseyRequestHttpEntity extends AbstractHttpEntity {

        private final ClientRequest clientRequest;

        private JerseyRequestHttpEntity(ClientRequest clientRequest) {
            super(
                clientRequest.getMediaType().toString(),
                getEncoding(clientRequest),
                true
            );
            this.clientRequest = clientRequest;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isRepeatable() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getContentLength() {
            return -1;
        }

        /**
         * {@inheritDoc}
         * <p>
         * This method isn't supported at will throw an {@link java.lang.UnsupportedOperationException}
         * if invoked.
         * </p>
         */
        @Override
        public InputStream getContent() throws IOException {
            // Shouldn't be called
            throw new UnsupportedOperationException("Reading from the entity is not supported");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeTo(final OutputStream outputStream) throws IOException {
            clientRequest.setStreamProvider(contentLength -> outputStream);
            clientRequest.writeEntity();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isStreaming() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
        }
    }

    /**
     * A custom {@link AbstractHttpEntity} that uses
     * a Jersey request as a content source.
     * <p>
     * In contrast to {@link io.dropwizard.client.DropwizardApacheConnector.JerseyRequestHttpEntity}
     * its contents are buffered on initialization.
     * </p>
     */
    private static class BufferedJerseyRequestHttpEntity extends AbstractHttpEntity {

        private static final int BUFFER_INITIAL_SIZE = 512;
        private byte[] buffer;

        private BufferedJerseyRequestHttpEntity(ClientRequest clientRequest) {
            super(
                clientRequest.getMediaType().toString(),
                getEncoding(clientRequest),
                false
            );
            final ByteArrayOutputStream stream = new ByteArrayOutputStream(BUFFER_INITIAL_SIZE);
            clientRequest.setStreamProvider(contentLength -> stream);
            try {
                clientRequest.writeEntity();
            } catch (IOException e) {
                throw new ProcessingException(ERROR_BUFFERING_ENTITY, e);
            }
            buffer = stream.toByteArray();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isRepeatable() {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getContentLength() {
            return buffer.length;
        }

        /**
         * {@inheritDoc}
         * <p>
         * This method isn't supported at will throw an {@link java.lang.UnsupportedOperationException}
         * if invoked.
         * </p>
         */
        @Override
        public InputStream getContent() throws IOException {
            // Shouldn't be called
            throw new UnsupportedOperationException("Reading from the entity is not supported");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeTo(OutputStream outstream) throws IOException {
            outstream.write(buffer);
            outstream.flush();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isStreaming() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
        }
    }

    @Nullable
    private static String getEncoding(ClientRequest clientRequest) {
        List<String> contentEncoding = clientRequest.getRequestHeader(HttpHeaders.CONTENT_ENCODING);
        if (contentEncoding == null) {
            return null;
        }
        return contentEncoding.stream().findFirst().orElse(null);
    }
}

