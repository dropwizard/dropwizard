package io.dropwizard.client;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.VersionInfo;
import org.glassfish.jersey.apache.connector.LocalizationMessages;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.message.internal.Statuses;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;
import java.util.concurrent.Future;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Dropwizard Apache Connector.
 * <p/>
 * It's a custom version of Jersey's {@link org.glassfish.jersey.client.spi.Connector}
 * that uses Apache's {@link org.apache.http.client.HttpClient}
 * as an HTTP transport implementation.
 * <p/>
 * It uses a pre-configured HTTP client by {@link io.dropwizard.client.HttpClientBuilder}
 * rather then creates a client from the Jersey configuration.
 * <p/>
 * This approach affords to use the extended configuration of
 * the Apache HttpClient in Dropwizard with a fluent interface
 * of JerseyClient.
 */
public class DropwizardApacheConnector implements Connector {

    private static final String APACHE_HTTP_CLIENT_VERSION = VersionInfo.loadVersionInfo
            ("org.apache.http.client", DropwizardApacheConnector.class.getClassLoader())
            .getRelease();

    /**
     * Actual HTTP client
     */
    private final CloseableHttpClient client;

    /**
     * Should a chunked encoding be used in POST requests
     */
    private final boolean chunkedEncodingEnabled;

    public DropwizardApacheConnector(CloseableHttpClient client, boolean chunkedEncodingEnabled) {
        this.client = client;
        this.chunkedEncodingEnabled = chunkedEncodingEnabled;
    }

    @Override
    public ClientResponse apply(ClientRequest jerseyRequest) throws ProcessingException {
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
     * <p/>
     * Convert a method, URI, body, headers and override a user-agent if necessary
     *
     * @param jerseyRequest representation of an HTTP request in Jersey
     * @return a new {@link org.apache.http.client.methods.HttpUriRequest}
     */
    private HttpUriRequest buildApacheRequest(ClientRequest jerseyRequest) {
        RequestBuilder builder = RequestBuilder
                .create(jerseyRequest.getMethod())
                .setUri(jerseyRequest.getUri())
                .setEntity(getHttpEntity(jerseyRequest));
        for (String headerName : jerseyRequest.getHeaders().keySet()) {
            // Ignore user-agent because it's already configured in the Apache HTTP client
            if (headerName.equalsIgnoreCase(HttpHeaders.USER_AGENT)) {
                continue;
            }
            builder.addHeader(headerName, jerseyRequest.getHeaderString(headerName));
        }
        return builder.build();
    }

    /**
     * Get an Apache's {@link org.apache.http.HttpEntity}
     * from Jersey's {@link org.glassfish.jersey.client.ClientRequest}
     * <p/>
     * Create a custom HTTP entity, because Jersey doesn't provide
     * a request stream or a byte buffer.
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

    @Override
    public Future<?> apply(final ClientRequest request, final AsyncConnectorCallback callback) {
        // Simulate an asynchronous execution
        return MoreExecutors.newDirectExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.response(apply(request));
                } catch (Exception e) {
                    callback.failure(e);
                }
            }
        });
    }

    @Override
    public String getName() {
        return "Apache-HttpClient/" + APACHE_HTTP_CLIENT_VERSION;
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            throw new ProcessingException(LocalizationMessages.FAILED_TO_STOP_CLIENT(), e);
        }
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

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public long getContentLength() {
            return -1;
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {
            // Shouldn't be called
            throw new UnsupportedOperationException("Reading from the entity is not supported");
        }

        @Override
        public void writeTo(final OutputStream outputStream) throws IOException {
            clientRequest.setStreamProvider(new OutboundMessageContext.StreamProvider() {
                @Override
                public OutputStream getOutputStream(int contentLength) throws IOException {
                    return outputStream;
                }
            });
            clientRequest.writeEntity();
        }

        @Override
        public boolean isStreaming() {
            return false;
        }

    }

    /**
     * A custom {@link org.apache.http.entity.AbstractHttpEntity} that uses
     * a Jersey request as a content source.
     * <p/>
     * In contrast with {@link io.dropwizard.client.DropwizardApacheConnector.JerseyRequestHttpEntity},
     * it's buffered. We preliminarily dump the content to a buffer before processing.
     */
    private static class BufferedJerseyRequestHttpEntity extends AbstractHttpEntity {

        private static final int BUFFER_INITIAL_SIZE = 512;
        private byte[] buffer;

        private BufferedJerseyRequestHttpEntity(ClientRequest clientRequest) {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream(BUFFER_INITIAL_SIZE);
            clientRequest.setStreamProvider(new OutboundMessageContext.StreamProvider() {
                @Override
                public OutputStream getOutputStream(int contentLength) throws IOException {
                    return stream;
                }
            });
            try {
                clientRequest.writeEntity();
            } catch (IOException e) {
                throw new ProcessingException(LocalizationMessages.ERROR_BUFFERING_ENTITY(), e);
            }
            buffer = stream.toByteArray();
            setChunked(false);
        }

        @Override
        public boolean isRepeatable() {
            return true;
        }

        @Override
        public long getContentLength() {
            return buffer.length;
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {
            // Shouldn't be called
            throw new UnsupportedOperationException("Reading from the entity is not supported");
        }

        @Override
        public void writeTo(OutputStream outstream) throws IOException {
            outstream.write(buffer);
            outstream.flush();
        }

        @Override
        public boolean isStreaming() {
            return false;
        }
    }
}

