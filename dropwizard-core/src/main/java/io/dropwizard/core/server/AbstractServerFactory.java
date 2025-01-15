package io.dropwizard.core.server;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ResponseMeteredLevel;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.core.setup.AdminEnvironment;
import io.dropwizard.core.setup.ExceptionMapperBinder;
import io.dropwizard.jersey.filter.AllowedMethodsFilter;
import io.dropwizard.jersey.jackson.JacksonFeature;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jersey.validation.HibernateValidationBinder;
import io.dropwizard.jetty.GzipHandlerFactory;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.jetty.ZipExceptionHandlingServletFilter;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.metrics.jetty12.InstrumentedQueuedThreadPool;
import io.dropwizard.metrics.jetty12.ee10.InstrumentedEE10Handler;
import io.dropwizard.metrics.servlets.AdminServlet;
import io.dropwizard.metrics.servlets.HealthCheckServlet;
import io.dropwizard.metrics.servlets.MetricsServlet;
import io.dropwizard.request.logging.LogbackAccessRequestLog;
import io.dropwizard.request.logging.LogbackAccessRequestLogAwareHandler;
import io.dropwizard.request.logging.LogbackAccessRequestLogFactory;
import io.dropwizard.request.logging.RequestLogFactory;
import io.dropwizard.servlets.ThreadNameFilter;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import io.dropwizard.validation.ValidationMethod;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Servlet;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.GracefulHandler;
import org.eclipse.jetty.setuid.RLimit;
import org.eclipse.jetty.setuid.SetUIDListener;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.VirtualThreads;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import static com.codahale.metrics.annotation.ResponseMeteredLevel.COARSE;

/**
 * A base class for {@link ServerFactory} implementations.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code requestLog}</td>
 *         <td></td>
 *         <td>The {@link RequestLogFactory request log} configuration.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code gzip}</td>
 *         <td></td>
 *         <td>The {@link GzipHandlerFactory GZIP} configuration.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code responseMeteredLevel}</td>
 *         <td>COARSE</td>
 *         <td>The response metered level to decide what response code meters are included.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code metricPrefix}</td>
 *         <td></td>
 *         <td>The metricPrefix to use in the metric name for jetty metrics.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxThreads}</td>
 *         <td>1024</td>
 *         <td>The maximum number of threads to use for requests.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code minThreads}</td>
 *         <td>8</td>
 *         <td>The minimum number of threads to use for requests.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxQueuedRequests}</td>
 *         <td>1024</td>
 *         <td>The maximum number of requests to queue before blocking the acceptors.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code idleThreadTimeout}</td>
 *         <td>1 minute</td>
 *         <td>The amount of time a worker thread can be idle before being stopped.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code nofileSoftLimit}</td>
 *         <td>(none)</td>
 *         <td>
 *             The number of open file descriptors before a soft error is issued. <b>Requires Jetty's
 *             {@code libsetuid.so} on {@code java.library.path}.</b>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code nofileHardLimit}</td>
 *         <td>(none)</td>
 *         <td>
 *             The number of open file descriptors before a hard error is issued. <b>Requires Jetty's
 *             {@code libsetuid.so} on {@code java.library.path}.</b>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code gid}</td>
 *         <td>(none)</td>
 *         <td>
 *             The group ID to switch to once the connectors have started. <b>Requires Jetty's
 *             {@code libsetuid.so} on {@code java.library.path}.</b>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code uid}</td>
 *         <td>(none)</td>
 *         <td>
 *             The user ID to switch to once the connectors have started. <b>Requires Jetty's
 *             {@code libsetuid.so} on {@code java.library.path}.</b>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code user}</td>
 *         <td>(none)</td>
 *         <td>
 *             The username to switch to once the connectors have started. <b>Requires Jetty's
 *             {@code libsetuid.so} on {@code java.library.path}.</b>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code group}</td>
 *         <td>(none)</td>
 *         <td>
 *             The group to switch to once the connectors have started. <b>Requires Jetty's
 *             {@code libsetuid.so} on {@code java.library.path}.</b>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code umask}</td>
 *         <td>(none)</td>
 *         <td>
 *             The umask to switch to once the connectors have started. <b>Requires Jetty's
 *             {@code libsetuid.so} on {@code java.library.path}.</b>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code startsAsRoot}</td>
 *         <td>(none)</td>
 *         <td>
 *             Whether or not the Dropwizard application is started as a root user. <b>Requires
 *             Jetty's {@code libsetuid.so} on {@code java.library.path}.</b>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code registerDefaultExceptionMappers}</td>
 *         <td>true</td>
 *         <td>
 *            Whether or not the default Jersey ExceptionMappers should be registered.
 *            Set this to false if you want to register your own.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code shutdownGracePeriod}</td>
 *         <td>30 seconds</td>
 *         <td>
 *             The maximum time to wait for Jetty, and all Managed instances, to cleanly shutdown
 *             before forcibly terminating them.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code allowedMethods}</td>
 *         <td>GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH</td>
 *         <td>
 *             The set of allowed HTTP methods. Others will be rejected with a
 *             405 Method Not Allowed response.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code rootPath}</td>
 *         <td>/*</td>
 *         <td>
 *           The URL pattern relative to {@code applicationContextPath} from which the JAX-RS resources will be served.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code enableThreadNameFilter}</td>
 *         <td>true</td>
 *         <td>
 *           Whether or not to apply the {@code ThreadNameFilter} that adjusts thread names to include the request
 *           method and request URI.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code dumpAfterStart}</td>
 *         <td>true</td>
 *         <td>
 *           Whether or not to dump jetty diagnostics after start.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code dumpBeforeStop}</td>
 *         <td>true</td>
 *         <td>
 *           Whether or not to dump jetty diagnostics before stop.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code enableVirtualThreads}</td>
 *         <td>false</td>
 *         <td>
 *             Whether to use virtual threads for Jetty's thread pool.
 *         </td>
 *     </tr>
 * </table>
 *
 * @see DefaultServerFactory
 * @see SimpleServerFactory
 */
public abstract class AbstractServerFactory implements ServerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerFactory.class);

    @Valid
    @Nullable
    private RequestLogFactory<?> requestLog;

    @Valid
    @NotNull
    private GzipHandlerFactory gzip = new GzipHandlerFactory();

    @Valid
    @NotNull
    private ResponseMeteredLevel responseMeteredLevel = COARSE;

    @Nullable
    private String metricPrefix = null;

    @Min(4)
    private int maxThreads = 1024;

    @Min(1)
    private int minThreads = 8;

    private int maxQueuedRequests = 1024;

    @MinDuration(1)
    private Duration idleThreadTimeout = Duration.minutes(1);

    @Min(1)
    @Nullable
    private Integer nofileSoftLimit;

    @Min(1)
    @Nullable
    private Integer nofileHardLimit;

    @Nullable
    private Integer gid;

    @Nullable
    private Integer uid;

    @Nullable
    private String user;

    @Nullable
    private String group;

    @Nullable
    private String umask;

    @Nullable
    private Boolean startsAsRoot;

    private Boolean registerDefaultExceptionMappers = Boolean.TRUE;

    private Boolean detailedJsonProcessingExceptionMapper = Boolean.FALSE;

    private Duration shutdownGracePeriod = Duration.seconds(30);

    @NotNull
    private Set<String> allowedMethods = AllowedMethodsFilter.DEFAULT_ALLOWED_METHODS;

    private Optional<String> jerseyRootPath = Optional.empty();

    private boolean enableThreadNameFilter = true;

    private boolean dumpAfterStart = false;

    private boolean dumpBeforeStop = false;

    private boolean enableVirtualThreads = false;

    @JsonIgnore
    @ValidationMethod(message = "must have a smaller minThreads than maxThreads")
    public boolean isThreadPoolSizedCorrectly() {
        return minThreads <= maxThreads;
    }

    @JsonProperty("requestLog")
    public synchronized RequestLogFactory<?> getRequestLogFactory() {
        if (requestLog == null) {
            // Lazy init to avoid a hard dependency to logback
            requestLog = new LogbackAccessRequestLogFactory();
        }
        return requestLog;
    }

    @JsonProperty("requestLog")
    public synchronized void setRequestLogFactory(RequestLogFactory<?> requestLog) {
        this.requestLog = requestLog;
    }

    @JsonProperty("gzip")
    public GzipHandlerFactory getGzipFilterFactory() {
        return gzip;
    }

    @JsonProperty("gzip")
    public void setGzipFilterFactory(GzipHandlerFactory gzip) {
        this.gzip = gzip;
    }

    @JsonProperty("responseMeteredLevel")
    public ResponseMeteredLevel getResponseMeteredLevel() {
        return responseMeteredLevel;
    }

    @JsonProperty("metricPrefix")
    @Nullable
    public String getMetricPrefix() {
        return metricPrefix;
    }

    @JsonProperty
    public int getMaxThreads() {
        return maxThreads;
    }

    @JsonProperty
    public void setMaxThreads(int count) {
        this.maxThreads = count;
    }

    @JsonProperty
    public int getMinThreads() {
        return minThreads;
    }

    @JsonProperty
    public void setMinThreads(int count) {
        this.minThreads = count;
    }

    @JsonProperty
    public int getMaxQueuedRequests() {
        return maxQueuedRequests;
    }

    @JsonProperty
    public void setMaxQueuedRequests(int maxQueuedRequests) {
        this.maxQueuedRequests = maxQueuedRequests;
    }

    @JsonProperty
    public Duration getIdleThreadTimeout() {
        return idleThreadTimeout;
    }

    @JsonProperty
    public void setIdleThreadTimeout(Duration idleThreadTimeout) {
        this.idleThreadTimeout = idleThreadTimeout;
    }

    @JsonProperty
    @Nullable
    public Integer getNofileSoftLimit() {
        return nofileSoftLimit;
    }

    @JsonProperty
    public void setNofileSoftLimit(Integer nofileSoftLimit) {
        this.nofileSoftLimit = nofileSoftLimit;
    }

    @JsonProperty
    @Nullable
    public Integer getNofileHardLimit() {
        return nofileHardLimit;
    }

    @JsonProperty
    public void setNofileHardLimit(Integer nofileHardLimit) {
        this.nofileHardLimit = nofileHardLimit;
    }

    @JsonProperty
    @Nullable
    public Integer getGid() {
        return gid;
    }

    @JsonProperty
    public void setGid(Integer gid) {
        this.gid = gid;
    }

    @JsonProperty
    @Nullable
    public Integer getUid() {
        return uid;
    }

    @JsonProperty
    public void setUid(Integer uid) {
        this.uid = uid;
    }

    @JsonProperty
    @Nullable
    public String getUser() {
        return user;
    }

    @JsonProperty
    public void setUser(String user) {
        this.user = user;
    }

    @JsonProperty
    @Nullable
    public String getGroup() {
        return group;
    }

    @JsonProperty
    public void setGroup(String group) {
        this.group = group;
    }

    @JsonProperty
    @Nullable
    public String getUmask() {
        return umask;
    }

    @JsonProperty
    public void setUmask(String umask) {
        this.umask = umask;
    }

    @JsonProperty
    @Nullable
    public Boolean getStartsAsRoot() {
        return startsAsRoot;
    }

    @JsonProperty
    public void setStartsAsRoot(Boolean startsAsRoot) {
        this.startsAsRoot = startsAsRoot;
    }

    public Boolean getRegisterDefaultExceptionMappers() {
        return registerDefaultExceptionMappers;
    }

    @JsonProperty
    public void setRegisterDefaultExceptionMappers(Boolean registerDefaultExceptionMappers) {
        this.registerDefaultExceptionMappers = registerDefaultExceptionMappers;
    }

    public Boolean getDetailedJsonProcessingExceptionMapper() {
        return detailedJsonProcessingExceptionMapper;
    }

    @JsonProperty
    public void setDetailedJsonProcessingExceptionMapper(Boolean detailedJsonProcessingExceptionMapper) {
        this.detailedJsonProcessingExceptionMapper = detailedJsonProcessingExceptionMapper;
    }

    @JsonProperty
    public Duration getShutdownGracePeriod() {
        return shutdownGracePeriod;
    }

    @JsonProperty
    public void setShutdownGracePeriod(Duration shutdownGracePeriod) {
        this.shutdownGracePeriod = shutdownGracePeriod;
    }

    @JsonProperty
    public Set<String> getAllowedMethods() {
        return allowedMethods;
    }

    @JsonProperty
    public void setAllowedMethods(Set<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    @JsonProperty("rootPath")
    public Optional<String> getJerseyRootPath() {
        return jerseyRootPath;
    }

    @JsonProperty("rootPath")
    public void setJerseyRootPath(String jerseyRootPath) {
        this.jerseyRootPath = Optional.ofNullable(jerseyRootPath);
    }

    @JsonProperty
    public boolean getEnableThreadNameFilter() {
        return enableThreadNameFilter;
    }

    @JsonProperty
    public void setEnableThreadNameFilter(boolean enableThreadNameFilter) {
        this.enableThreadNameFilter = enableThreadNameFilter;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public boolean getDumpAfterStart() {
        return dumpAfterStart;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public void setDumpAfterStart(boolean dumpAfterStart) {
        this.dumpAfterStart = dumpAfterStart;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public boolean getDumpBeforeStop() {
        return dumpBeforeStop;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public void setDumpBeforeStop(boolean dumpBeforeStop) {
        this.dumpBeforeStop = dumpBeforeStop;
    }

    @JsonProperty
    public boolean isEnableVirtualThreads() {
        return enableVirtualThreads;
    }

    @JsonProperty
    public void setEnableVirtualThreads(boolean enableVirtualThreads) {
        this.enableVirtualThreads = enableVirtualThreads;
    }

    protected Handler createAdminServlet(Server server,
                                         MutableServletContextHandler handler,
                                         MetricRegistry metrics,
                                         HealthCheckRegistry healthChecks,
                                         AdminEnvironment admin) {
        configureSessionsAndSecurity(handler, server);
        handler.setServer(server);
        handler.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY, metrics);
        handler.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, healthChecks);
        handler.getServletContext().setAttribute(AdminServlet.HEALTHCHECK_ENABLED_PARAM_KEY, admin.isHealthCheckServletEnabled());
        handler.addServlet(AdminServlet.class, "/*");
        final String allowedMethodsParam = String.join(",", allowedMethods);
        handler.addFilter(AllowedMethodsFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST))
                .setInitParameter(AllowedMethodsFilter.ALLOWED_METHODS_PARAM, allowedMethodsParam);
        return handler;
    }

    private void configureSessionsAndSecurity(MutableServletContextHandler handler, Server server) {
        handler.setServer(server);
        if (handler.isSecurityEnabled()) {
            handler.getSecurityHandler().setServer(server);
        }
        if (handler.isSessionsEnabled()) {
            handler.getSessionHandler().setServer(server);
        }
    }

    protected Handler createAppServlet(Server server,
                                       JerseyEnvironment jersey,
                                       ObjectMapper objectMapper,
                                       Validator validator,
                                       MutableServletContextHandler handler,
                                       @Nullable Servlet jerseyContainer,
                                       MetricRegistry metricRegistry) {
        configureSessionsAndSecurity(handler, server);
        final String allowedMethodsParam = String.join(",", allowedMethods);
        handler.addFilter(AllowedMethodsFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST))
                .setInitParameter(AllowedMethodsFilter.ALLOWED_METHODS_PARAM, allowedMethodsParam);
        handler.addFilter(ZipExceptionHandlingServletFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        if (enableThreadNameFilter) {
            handler.addFilter(ThreadNameFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        }
        if (jerseyContainer != null) {
            jerseyRootPath.ifPresent(jersey::setUrlPattern);
            jersey.register(new JacksonFeature(objectMapper));
            jersey.register(new HibernateValidationBinder(validator));
            if (registerDefaultExceptionMappers == null || registerDefaultExceptionMappers) {
                jersey.register(new ExceptionMapperBinder(detailedJsonProcessingExceptionMapper));
            }
            handler.addServlet(new ServletHolder("jersey", jerseyContainer), jersey.getUrlPattern());
        }
        @SuppressWarnings("NullAway")
        final InstrumentedEE10Handler instrumented = new InstrumentedEE10Handler(metricRegistry, metricPrefix, responseMeteredLevel);
        instrumented.setServer(server);
        instrumented.setHandler(handler);
        return instrumented;
    }

    protected ThreadPool createThreadPool(MetricRegistry metricRegistry) {
        final BlockingQueue<Runnable> queue = new BlockingArrayQueue<>(minThreads, maxThreads, maxQueuedRequests);
        final InstrumentedQueuedThreadPool threadPool =
                new InstrumentedQueuedThreadPool(metricRegistry, maxThreads, minThreads,
                    (int) idleThreadTimeout.toMilliseconds(), queue);
        if (enableVirtualThreads) {
            threadPool.setVirtualThreadsExecutor(getVirtualThreadsExecutorService());
        }
        threadPool.setName("dw");
        return threadPool;
    }

    protected ExecutorService getVirtualThreadsExecutorService() {
        try {
            return (ExecutorService) Executors.class
                .getDeclaredMethod("newVirtualThreadPerTaskExecutor")
                .invoke(null);
        } catch (InvocationTargetException invocationTargetException) {
            throw new IllegalStateException("Error while obtaining a virtual thread executor", invocationTargetException.getCause());
        } catch (Exception exception) {
            throw new IllegalStateException("Error while obtaining a virtual thread executor", exception);
        }
    }

    protected Server buildServer(LifecycleEnvironment lifecycle,
                                 ThreadPool threadPool) {
        final Server server = new Server(threadPool);
        server.addEventListener(buildSetUIDListener());
        lifecycle.attach(server);
        final ErrorHandler errorHandler = new ErrorHandler();
        errorHandler.setShowStacks(false);
        server.setErrorHandler(errorHandler);
        server.setStopAtShutdown(true);
        server.setStopTimeout(shutdownGracePeriod.toMilliseconds());
        server.setDumpAfterStart(dumpAfterStart);
        server.setDumpBeforeStop(dumpBeforeStop);
        return server;
    }

    protected SetUIDListener buildSetUIDListener() {
        final SetUIDListener listener = new SetUIDListener();

        if (startsAsRoot != null) {
            listener.setStartServerAsPrivileged(startsAsRoot);
        }

        if (gid != null) {
            listener.setGid(gid);
        }

        if (uid != null) {
            listener.setUid(uid);
        }

        if (user != null) {
            listener.setUsername(user);
        }

        if (group != null) {
            listener.setGroupname(group);
        }

        if (nofileHardLimit != null || nofileSoftLimit != null) {
            final RLimit rlimit = new RLimit();
            if (nofileHardLimit != null) {
                rlimit.setHard(nofileHardLimit);
            }

            if (nofileSoftLimit != null) {
                rlimit.setSoft(nofileSoftLimit);
            }

            listener.setRLimitNoFiles(rlimit);
        }

        if (umask != null) {
            listener.setUmaskOctal(umask);
        }

        return listener;
    }

    protected void addRequestLog(Server server, String name, MutableServletContextHandler servletContextHandler) {
        if (getRequestLogFactory().isEnabled()) {
            RequestLog log = getRequestLogFactory().build(name);
            if (log instanceof LogbackAccessRequestLog) {
                servletContextHandler.insertHandler(new LogbackAccessRequestLogAwareHandler());
            }
            server.setRequestLog(log);
        }
    }

    protected Handler addGracefulHandler(Handler handler) {
        final GracefulHandler gracefulHandler = new GracefulHandler();
        gracefulHandler.setHandler(handler);
        return gracefulHandler;
    }

    protected Handler buildGzipHandler(Handler handler) {
        return gzip.isEnabled() ? gzip.build(handler) : handler;
    }

    @SuppressWarnings("Slf4jFormatShouldBeConst")
    protected void printBanner(String name) {
        String msg = "Starting " + name;
        try (final InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("banner.txt")) {
            if (resourceStream != null) {
                try (final InputStreamReader inputStreamReader = new InputStreamReader(resourceStream);
                     final BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    final String banner = bufferedReader
                        .lines()
                        .collect(Collectors.joining(System.lineSeparator()));
                    msg = String.format("Starting %s%n%s", name, banner);
                }
            }
        } catch (IllegalArgumentException | IOException ignored) {
        }
        LOGGER.info(msg);
    }
}
