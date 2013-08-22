package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import com.codahale.dropwizard.jersey.setup.JerseyEnvironment;
import com.codahale.dropwizard.jetty.GzipFilterFactory;
import com.codahale.dropwizard.jetty.MutableServletContextHandler;
import com.codahale.dropwizard.jetty.NonblockingServletHolder;
import com.codahale.dropwizard.jetty.RequestLogFactory;
import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.dropwizard.servlets.ThreadNameFilter;
import com.codahale.dropwizard.util.Duration;
import com.codahale.dropwizard.validation.MinDuration;
import com.codahale.dropwizard.validation.ValidationMethod;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.setuid.RLimit;
import org.eclipse.jetty.setuid.SetUIDListener;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.DispatcherType;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

// TODO: 5/15/13 <coda> -- add tests for AbstractServerFactory

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
 * </table>
 *
 * @see DefaultServerFactory
 * @see SimpleServerFactory
 */
public abstract class AbstractServerFactory implements ServerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerFactory.class);
    private static final Pattern WINDOWS_NEWLINE = Pattern.compile("\\r\\n?");

    @Min(1)
    private Integer nofileSoftLimit;

    @Min(1)
    private Integer nofileHardLimit;

    private Integer gid;

    private Integer uid;

    private String user;

    private String group;

    private String umask;

    private Boolean startsAsRoot;

    @JsonProperty
    public Integer getNofileSoftLimit() {
        return nofileSoftLimit;
    }

    @JsonProperty
    public void setNofileSoftLimit(Integer nofileSoftLimit) {
        this.nofileSoftLimit = nofileSoftLimit;
    }

    @JsonProperty
    public Integer getNofileHardLimit() {
        return nofileHardLimit;
    }

    @JsonProperty
    public void setNofileHardLimit(Integer nofileHardLimit) {
        this.nofileHardLimit = nofileHardLimit;
    }

    @JsonProperty
    public Integer getGid() {
        return gid;
    }

    @JsonProperty
    public void setGid(Integer gid) {
        this.gid = gid;
    }

    @JsonProperty
    public Integer getUid() {
        return uid;
    }

    @JsonProperty
    public void setUid(Integer uid) {
        this.uid = uid;
    }

    @JsonProperty
    public String getUser() {
        return user;
    }

    @JsonProperty
    public void setUser(String user) {
        this.user = user;
    }

    @JsonProperty
    public String getGroup() {
        return group;
    }

    @JsonProperty
    public void setGroup(String group) {
        this.group = group;
    }

    @JsonProperty
    public String getUmask() {
        return umask;
    }

    @JsonProperty
    public void setUmask(String umask) {
        this.umask = umask;
    }

    @JsonProperty
    public Boolean getStartsAsRoot() {
        return startsAsRoot;
    }

    @JsonProperty
    public void setStartsAsRoot(Boolean startsAsRoot) {
        this.startsAsRoot = startsAsRoot;
    }

    protected ServletContextHandler addAdminServlets(ServletContextHandler handler,
                                                     MetricRegistry metrics,
                                                     HealthCheckRegistry healthChecks) {
        handler.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY, metrics);
        handler.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, healthChecks);
        handler.addServlet(new NonblockingServletHolder(new AdminServlet()), "/*");
        return handler;
    }

    protected ServletContextHandler addJerseyServlet(ServletContextHandler handler,
                                                     ServletContainer jerseyContainer,
                                                     JerseyEnvironment jersey,
                                                     ObjectMapper objectMapper,
                                                     Validator validator) {
        if (jerseyContainer != null) {
            jersey.register(new JacksonMessageBodyProvider(objectMapper, validator));
            handler.addServlet(new NonblockingServletHolder(jerseyContainer), jersey.getUrlPattern());
        }
        return handler;
    }

    protected Server buildServer(LifecycleEnvironment lifecycle,
                                 ThreadPool threadPool) {
        final Server server = new Server(threadPool);
        server.addLifeCycleListener(buildSetUIDListener());
        lifecycle.attach(server);
        final ErrorHandler errorHandler = new ErrorHandler();
        errorHandler.setShowStacks(false);
        server.addBean(errorHandler);
        server.setStopAtShutdown(true);
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

    protected void printBanner(String name) {
        try {
            final String banner = WINDOWS_NEWLINE.matcher(Resources.toString(Resources.getResource("banner.txt"),
                                                                             Charsets.UTF_8))
                                                 .replaceAll("\n")
                                                 .replace("\n", String.format("%n"));
            LOGGER.info(String.format("Starting {}%n{}"), name, banner);
        } catch (IllegalArgumentException | IOException ignored) {
            // don't display the banner if there isn't one
            LOGGER.info("Starting {}", name);
        }
    }
}
