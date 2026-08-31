package io.dropwizard.request.logging;

import ch.qos.logback.access.common.AccessConstants;
import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.access.common.spi.ServerAdapter;
import ch.qos.logback.core.spi.SequenceNumberGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee10.servlet.ServletContextRequest;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.security.AuthenticationState;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Session;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class JettyAccessEvent implements IAccessEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(JettyAccessEvent.class);

    private final Request request;
    private final Response response;
    private final DropwizardJettyServerAdapter jettyServerAdapter;
    private final Charset requestCharset;
    private final Charset responseCharset;
    private final long sequenceNumber;
    private final long timeStamp;
    private final long elapsedTime;

    @Nullable
    private String threadName;

    private final Lazy<String> path = new Lazy<>(this::getPathInternal);
    private final NullableLazy<String> remoteAddr = new NullableLazy<>(this::getRemoteAddrInternal);
    private final NullableLazy<String> remoteUser = new NullableLazy<>(this::getRemoteUserInternal);
    private final Lazy<String> protocol = new Lazy<>(this::getProtocolInternal);
    private final Lazy<String> method = new Lazy<>(this::getMethodInternal);
    private final NullableLazy<String> serverName = new NullableLazy<>(this::getServerNameInternal);
    private final NullableLazy<String> sessionID = new NullableLazy<>(this::getSessionIDInternal);
    private final NullableLazy<String> queryString = new NullableLazy<>(this::getQueryStringInternal);
    private final Lazy<Map<String, String>> headers = new Lazy<>(this::getRequestHeaderMapInternal);
    private final Lazy<Map<String, String[]>> parameters = new Lazy<>(this::getRequestParameterMapInternal);
    private final Lazy<Map<String, Object>> attributes = new Lazy<>(this::buildAttributeMapInternal);
    private final Lazy<Map<String, String>> cookies = new Lazy<>(this::buildCookieMapInternal);
    private final Lazy<Long> contentLength = new Lazy<>(this::getContentLengthInternal);
    private final Lazy<Integer> statusCode = new Lazy<>(this::getStatusCodeInternal);
    private final Lazy<String> requestContent = new Lazy<>(this::getRequestContentInternal);
    private final Lazy<String> responseContent = new Lazy<>(this::getResponseContentInternal);
    private final Lazy<Integer> localPort = new Lazy<>(this::getLocalPortInternal);
    private final Lazy<Map<String, String>> responseHeaders = new Lazy<>(this::getResponseHeaderMapInternal);

    public JettyAccessEvent(Request request, Response response, SequenceNumberGenerator sequenceNumberGenerator) {
        this.request = request;
        this.response = response;
        this.jettyServerAdapter = new DropwizardJettyServerAdapter(request, response);
        this.requestCharset = resolveRequestCharset();
        this.responseCharset = resolveResponseCharset();
        if (sequenceNumberGenerator != null) {
            this.sequenceNumber = sequenceNumberGenerator.nextSequenceNumber();
        } else {
            this.sequenceNumber = 0;
        }
        this.timeStamp = System.currentTimeMillis();
        this.elapsedTime = this.timeStamp - Request.getTimeStamp(request);
    }

    private Charset resolveRequestCharset() {
        try {
            Charset cs = Request.getCharset(request);
            return cs != null ? cs : StandardCharsets.UTF_8;
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            return StandardCharsets.UTF_8;
        }
    }

    private Charset resolveResponseCharset() {
        try {
            String contentType = response.getHeaders().get(HttpHeader.CONTENT_TYPE);
            if (contentType == null) {
                return StandardCharsets.UTF_8;
            }
            String charsetName = MimeTypes.getCharsetFromContentType(contentType);
            return charsetName != null ? Charset.forName(charsetName) : StandardCharsets.UTF_8;
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            return StandardCharsets.UTF_8;
        }
    }

    /**
     * Returns the servlet-side request wrapper for consumers like AccessEventDiscriminator that call
     * getSession(false), getRemoteUser(), etc. through the standard servlet API.
     * <p>
     * Warning: Not async-freeze safe: Must be called only in a synchronous context. This returns a live reference to
     * Jetty's HttpServletRequest, which will return stale/wrong data (or possibly crash in a bad way) after Jetty
     * recycles the request.
     */
    @Override
    @Nullable
    public HttpServletRequest getRequest() {
        final ServletContextRequest servletContextRequest = Request.as(request, ServletContextRequest.class);
        if (servletContextRequest == null) {
            return null;
        }
        return servletContextRequest.getServletApiRequest();
    }

    /**
     * Returns the servlet-side response wrapper.
     * <p>
     * Warning: Not async-freeze safe: Must be called only in a synchronous context. This returns a live reference to
     * Jetty's HttpServletResponse, which will return stale/wrong data (or possibly crash in a bad way) after Jetty
     * recycles the request.
     */
    @Override
    @Nullable
    public HttpServletResponse getResponse() {
        final ServletContextRequest servletContextRequest = Request.as(request, ServletContextRequest.class);
        if (servletContextRequest == null) {
            return null;
        }
        return servletContextRequest.getServletContextResponse().getServletApiResponse();
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public long getElapsedTime() {
        return elapsedTime;
    }

    @Override
    public long getElapsedSeconds() {
        return getElapsedTime() / 1000;
    }

    @Override
    public String getRequestURI() {
        return path.get();
    }

    private String getPathInternal() {
        return request.getHttpURI().getPath();
    }

    @Override
    public String getRequestURL() {
        return "%s %s%s %s".formatted(getMethod(), getRequestURI(), getQueryString(), getProtocol());
    }

    @Override
    public String getRemoteHost() {
        return Optional.ofNullable(remoteAddr.get()).orElse("-");
    }

    private String getRemoteAddrInternal() {
        return Request.getRemoteAddr(request);
    }

    @Override
    public String getRemoteUser() {
        return Optional.ofNullable(remoteUser.get()).orElse("-");
    }

    @Nullable
    private String getRemoteUserInternal() {
        AuthenticationState authenticationState = AuthenticationState.getAuthenticationState(request);
        if (authenticationState != null && authenticationState.getUserPrincipal() != null) {
            return authenticationState.getUserPrincipal().getName();
        }
        return null;
    }

    @Override
    public String getProtocol() {
        return protocol.get();
    }

    private String getProtocolInternal() {
        return request.getConnectionMetaData().getProtocol();
    }

    @Override
    public String getMethod() {
        return method.get();
    }

    private String getMethodInternal() {
        return request.getMethod();
    }

    @Override
    public String getServerName() {
        return Optional.ofNullable(serverName.get()).orElse("-");
    }

    private String getServerNameInternal() {
        return Request.getServerName(request);
    }

    @Override
    public String getSessionID() {
        return Optional.ofNullable(sessionID.get()).orElse("-");
    }

    @Nullable
    private String getSessionIDInternal() {
        Session session =  request.getSession(false);
        if (session != null) {
            return session.getId();
        }
        return null;
    }

    @Override
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public String getThreadName() {
        return Optional.ofNullable(threadName).orElse("-");
    }

    @Override
    public String getQueryString() {
        return Optional.ofNullable(queryString.get()).orElse("-");
    }

    private String getQueryStringInternal() {
        return Optional.ofNullable(request.getHttpURI().getQuery())
            .map(query -> "?" + query)
            .orElse("");
    }

    @Override
    public String getRemoteAddr() {
        return Optional.ofNullable(remoteAddr.get()).orElse("-");
    }

    @Override
    public String getRequestHeader(String key) {
        return getRequestHeaderMap().getOrDefault(key, "-");
    }

    @Override
    public Enumeration<String> getRequestHeaderNames() {
        return Collections.enumeration(getRequestHeaderMap().keySet());
    }

    @Override
    public Map<String, String> getRequestHeaderMap() {
        return headers.get();
    }

    private Map<String, String> getRequestHeaderMapInternal() {
        return request.getHeaders()
            .stream()
            .collect(
                Collectors.groupingBy(
                    HttpField::getName,
                    () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                    Collectors.mapping(HttpField::getValue,
                        Collectors.joining(",")))
            );
    }

    @Override
    public Map<String, String[]> getRequestParameterMap() {
        return parameters.get();
    }

    private Map<String, String[]> getRequestParameterMapInternal() {
        try {
            return Request.getParameters(request).toStringArrayMap();
        } catch (Throwable t) {
            LOGGER.error("Error while retrieving request parameters", t);
            return Map.of();
        }
    }

    @Override
    @Nullable
    public String getAttribute(String key) {
        return Optional.ofNullable(attributes.get().get(key))
            .map(Object::toString)
            .orElse("-");
    }

    private Map<String, Object> buildAttributeMapInternal() {
        return request.getAttributeNameSet()
            .stream()
            .collect(Collectors.toMap(Function.identity(), request::getAttribute));
    }

    @Override
    public String[] getRequestParameter(String key) {
        return getRequestParameterMap().getOrDefault(key, new String[]{"-"});
    }

    @Override
    @Nullable
    public String getCookie(String key) {
        return cookies.get().getOrDefault(key, "-");
    }

    private Map<String, String> buildCookieMapInternal() {
        return Request.getCookies(request)
            .stream()
            .collect(Collectors.toMap(HttpCookie::getName, HttpCookie::getValue));
    }

    @Override
    public long getContentLength() {
        return contentLength.get();
    }

    private long getContentLengthInternal() {
        return Response.getContentBytesWritten(response);
    }

    @Override
    public int getStatusCode() {
        return statusCode.get();
    }

    private int getStatusCodeInternal() {
        return response.getStatus();
    }

    @Override
    public String getRequestContent() {
        return requestContent.get();
    }

    private String getRequestContentInternal() {
        // retrieve the byte array placed by TeeFilter, if present
        Object attributeValue = request.getAttribute(AccessConstants.LB_INPUT_BUFFER);
        if (!(attributeValue instanceof byte[] inputBuffer)) {
            return "";
        }
        return new String(inputBuffer, requestCharset);
    }

    @Override
    public String getResponseContent() {
        return responseContent.get();
    }

    private String getResponseContentInternal() {
        // retrieve the byte array placed by TeeFilter, if present
        Object attributeValue = request.getAttribute(AccessConstants.LB_OUTPUT_BUFFER);
        if (!(attributeValue instanceof byte[] outputBuffer)) {
            return "";
        }
        return new String(outputBuffer, responseCharset);
    }

    @Override
    public int getLocalPort() {
        return localPort.get();
    }

    private int getLocalPortInternal() {
        return Request.getLocalPort(request);
    }

    @Override
    public ServerAdapter getServerAdapter() {
        return jettyServerAdapter;
    }

    @Override
    public String getResponseHeader(String key) {
        return getResponseHeaderMap().getOrDefault(key, "-");
    }

    @Override
    public Map<String, String> getResponseHeaderMap() {
        return responseHeaders.get();
    }

    private Map<String, String> getResponseHeaderMapInternal() {
        return response.getHeaders()
            .stream()
            .collect(
                Collectors.groupingBy(
                    HttpField::getName,
                    () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                    Collectors.mapping(HttpField::getValue,
                        Collectors.joining(",")))
            );
    }

    @Override
    public List<String> getResponseHeaderNameList() {
        return getResponseHeaderMap().keySet().stream().toList();
    }

    @Override
    public void prepareForDeferredProcessing() {
        path.initialize();
        remoteAddr.initialize();
        remoteUser.initialize();
        protocol.initialize();
        method.initialize();
        serverName.initialize();
        sessionID.initialize();
        queryString.initialize();
        headers.initialize();
        parameters.initialize();
        attributes.initialize();
        cookies.initialize();
        contentLength.initialize();
        statusCode.initialize();
        requestContent.initialize();
        responseContent.initialize();
        localPort.initialize();
        responseHeaders.initialize();
    }

    private static final class Lazy<T> implements Supplier<T> {
        private final Supplier<T> supplier;
        @Nullable
        private T value;
        private boolean initialized = false;
        private final Object lock = new Object();

        public Lazy(@NonNull Supplier<@NonNull T> supplier) {
            this.supplier = supplier;
        }

        @Override
        @NonNull
        public T get() {
            synchronized (lock) {
                if (!initialized) {
                    value = supplier.get();
                    initialized = true;
                }
                return Objects.requireNonNull(value);
            }
        }

        public void initialize() {
            synchronized (lock) {
                if (!initialized) {
                    value = supplier.get();
                    initialized = true;
                }
            }
        }
    }

    private static final class NullableLazy<T> implements Supplier<T> {
        private final Supplier<T> supplier;
        @Nullable
        private T value;
        private boolean initialized = false;
        private final Object lock = new Object();

        public NullableLazy(@NonNull Supplier<@Nullable T> supplier) {
            this.supplier = supplier;
        }

        @Override
        @Nullable
        public T get() {
            synchronized (lock) {
                if (!initialized) {
                    value = supplier.get();
                    initialized = true;
                }
                return value;
            }
        }

        public void initialize() {
            synchronized (lock) {
                if (!initialized) {
                    value = supplier.get();
                    initialized = true;
                }
            }
        }
    }
}
