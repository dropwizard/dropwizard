package io.dropwizard.client;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.VersionInfo;
import org.glassfish.jersey.apache.connector.LocalizationMessages;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.message.internal.Statuses;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Dropwizard Apache Connector.
 * <p>
 * It's a custom version of Jersey's {@link org.glassfish.jersey.client.spi.Connector}
 * that uses Apache's {@link org.apache.http.client.HttpClient}
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

    private static final String APACHE_HTTP_CLIENT_VERSION = VersionInfo
            .loadVersionInfo("org.apache.http.client", DropwizardApacheConnector.class.getClassLoader())
            .getRelease();

    /**
     * Actual HTTP client
     */
    private final CloseableHttpClient client;
    /**
     * Default HttpUriRequestConfig
     */
    private final RequestConfig defaultRequestConfig;

    /**
     * Should a chunked encoding be used in POST requests
     */
    private final boolean chunkedEncodingEnabled;

    public DropwizardApacheConnector(CloseableHttpClient client, RequestConfig defaultRequestConfig,
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

            final StatusLine statusLine = apacheResponse.getStatusLine();
            final Response.StatusType status = Statuses.from(statusLine.getStatusCode(),
                    firstNonNull(statusLine.getReasonPhrase(), ""));

            final ClientResponse jerseyResponse = new ClientResponse(status, jerseyRequest);
            for (Header header : apacheResponse.getAllHeaders()) {
                final List<String> headerValues = jerseyResponse.getHeaders().get(header.getName());
                if (headerValues == null) {
                    jerseyResponse.getHeaders().put(header.getName(), Lists.newArrayList(header.getValue()));
                } else {
                    headerValues.add(header.getValue());
                }
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
     * Build a new Apache's {@link org.apache.http.client.methods.HttpUriRequest}
     * from Jersey's {@link org.glassfish.jersey.client.ClientRequest}
     * <p>
     * Convert a method, URI, body, headers and override a user-agent if necessary
     * </p>
     *
     * @param jerseyRequest representation of an HTTP request in Jersey
     * @return a new {@link org.apache.http.client.methods.HttpUriRequest}
     */
    private HttpUriRequest buildApacheRequest(ClientRequest jerseyRequest) {
        final RequestBuilder builder = RequestBuilder
                .create(jerseyRequest.getMethod())
                .setUri(jerseyRequest.getUri())
                .setEntity(getHttpEntity(jerseyRequest));
        for (String headerName : jerseyRequest.getHeaders().keySet()) {
            builder.addHeader(headerName, jerseyRequest.getHeaderString(headerName));
        }

        final Optional<RequestConfig> requestConfig = addJerseyRequestConfig(jerseyRequest);
        requestConfig.ifPresent(builder::setConfig);

        return builder.build();
    }

    private Optional<RequestConfig> addJerseyRequestConfig(ClientRequest clientRequest) {
        final Integer timeout = clientRequest.resolveProperty(ClientProperties.READ_TIMEOUT, Integer.class);
        final Integer connectTimeout = clientRequest.resolveProperty(ClientProperties.CONNECT_TIMEOUT, Integer.class);
        final Boolean followRedirects = clientRequest.resolveProperty(ClientProperties.FOLLOW_REDIRECTS, Boolean.class);

        if (timeout != null || connectTimeout != null || followRedirects != null) {
            final RequestConfig.Builder requestConfig = RequestConfig.copy(defaultRequestConfig);

            if (timeout != null) {
                requestConfig.setSocketTimeout(timeout);
            }

            if (connectTimeout != null) {
                requestConfig.setConnectTimeout(connectTimeout);
            }

            if (followRedirects != null) {
                requestConfig.setRedirectsEnabled(followRedirects);
            }

            return Optional.of(requestConfig.build());
        }

        return Optional.empty();
    }

    /**
     * Get an Apache's {@link org.apache.http.HttpEntity}
     * from Jersey's {@link org.glassfish.jersey.client.ClientRequest}
     * <p>
     * Create a custom HTTP entity, because Jersey doesn't provide
     * a request stream or a byte buffer.
     * </p>
     *
     * @param jerseyRequest representation of an HTTP request in Jersey
     * @return a correct {@link org.apache.http.HttpEntity} implementation
     */
    private HttpEntity getHttpEntity(ClientRequest jerseyRequest) {
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
        return MoreExecutors.newDirectExecutorService().submit((Runnable) () -> {
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
     * A custom {@link org.apache.http.entity.AbstractHttpEntity} that uses
     * a Jersey request as a content source. It's chunked because we don't
     * know the content length beforehand.
     */
    private static class JerseyRequestHttpEntity extends AbstractHttpEntity {

        private ClientRequest clientRequest;

        private JerseyRequestHttpEntity(ClientRequest clientRequest) {
            this.clientRequest = clientRequest;
            setChunked(true);
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

    }

    /**
     * A custom {@link org.apache.http.entity.AbstractHttpEntity} that uses
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
            final ByteArrayOutputStream stream = new ByteArrayOutputStream(BUFFER_INITIAL_SIZE);
            clientRequest.setStreamProvider(contentLength -> stream);
            try {
                clientRequest.writeEntity();
            } catch (IOException e) {
                throw new ProcessingException(LocalizationMessages.ERROR_BUFFERING_ENTITY(), e);
            }
            buffer = stream.toByteArray();
            setChunked(false);
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
    }
}

