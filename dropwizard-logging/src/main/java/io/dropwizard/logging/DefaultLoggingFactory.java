package io.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jmx.JMXConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.util.StatusPrinter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.logback.InstrumentedAppender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.management.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonTypeName("default")
public class DefaultLoggingFactory implements LoggingFactory {
	private static final ReentrantLock MBEAN_REGISTRATION_LOCK = new ReentrantLock();
	private static final ReentrantLock CONFIGURE_LOGGING_LEVEL_LOCK = new ReentrantLock();

	@NotNull
	private Level level = Level.INFO;

	@NotNull
	private ImmutableMap<String, DefaultLoggerFactory> loggers = ImmutableMap
			.of();

	@Valid
	@NotNull
	private ImmutableList<AppenderFactory> appenders = ImmutableList
			.<AppenderFactory> of(new ConsoleAppenderFactory());

	@JsonIgnore
	private final LoggerContext loggerContext;

	@JsonIgnore
	private final PrintStream configurationErrorsStream;

	public DefaultLoggingFactory() {
		this(LoggingUtil.getLoggerContext(), System.err);
	}

	@VisibleForTesting
	DefaultLoggingFactory(LoggerContext loggerContext,
			PrintStream configurationErrorsStream) {
		this.loggerContext = checkNotNull(loggerContext);
		this.configurationErrorsStream = checkNotNull(configurationErrorsStream);
	}

	@VisibleForTesting
	LoggerContext getLoggerContext() {
		return loggerContext;
	}

	@VisibleForTesting
	PrintStream getConfigurationErrorsStream() {
		return configurationErrorsStream;
	}

	@JsonProperty
	public Level getLevel() {
		return level;
	}

	@JsonProperty
	public void setLevel(Level level) {
		this.level = level;
	}

	@JsonProperty
	public ImmutableMap<String, DefaultLoggerFactory> getLoggers() {
		return loggers;
	}

	@JsonProperty
	public void setLoggers(Map<String, DefaultLoggerFactory> loggers) {
		this.loggers = ImmutableMap.copyOf(loggers);
	}

	@JsonProperty
	public ImmutableList<AppenderFactory> getAppenders() {
		return appenders;
	}

	@JsonProperty
	public void setAppenders(List<AppenderFactory> appenders) {
		this.appenders = ImmutableList.copyOf(appenders);
	}

	public void configure(MetricRegistry metricRegistry, String name) {
		LoggingUtil.hijackJDKLogging();

		CONFIGURE_LOGGING_LEVEL_LOCK.lock();
		final Logger root;
		try {
			root = configureLoggers(name);
		} finally {
			CONFIGURE_LOGGING_LEVEL_LOCK.unlock();
		}

		for (AppenderFactory output : appenders) {
			root.addAppender(output.build(loggerContext, name, null));
		}

		StatusPrinter.setPrintStream(configurationErrorsStream);
		try {
			StatusPrinter.printIfErrorsOccured(loggerContext);
		} finally {
			StatusPrinter.setPrintStream(System.out);
		}

		final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		MBEAN_REGISTRATION_LOCK.lock();
		try {
			final ObjectName objectName = new ObjectName(
					"io.dropwizard:type=Logging");
			if (!server.isRegistered(objectName)) {
				server.registerMBean(new JMXConfigurator(loggerContext, server,
						objectName), objectName);
			}
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException
				| NotCompliantMBeanException | MBeanRegistrationException e) {
			throw new RuntimeException(e);
		} finally {
			MBEAN_REGISTRATION_LOCK.unlock();
		}

		configureInstrumentation(root, metricRegistry);
	}

	public void stop() {
		loggerContext.stop();
	}

	private void configureInstrumentation(Logger root,
			MetricRegistry metricRegistry) {
		final InstrumentedAppender appender = new InstrumentedAppender(
				metricRegistry);
		appender.setContext(loggerContext);
		appender.start();
		root.addAppender(appender);
	}

	private Logger configureLoggers(String name) {
		final Logger root = loggerContext
				.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		loggerContext.reset();

		final LevelChangePropagator propagator = new LevelChangePropagator();
		propagator.setContext(loggerContext);
		propagator.setResetJUL(true);

		loggerContext.addListener(propagator);

		root.setLevel(level);

		for (Entry<String, DefaultLoggerFactory> entry : loggers.entrySet()) {
			Logger logger = loggerContext.getLogger(entry.getKey());
			logger.setLevel(entry.getValue().getLevel());
			for (AppenderFactory appender : entry.getValue().getAppenders()) {
				Appender<ILoggingEvent> newAppender = appender.build(loggerContext, name, null);
				logger.addAppender(newAppender);
			}

		}
		return root;
	}
}
