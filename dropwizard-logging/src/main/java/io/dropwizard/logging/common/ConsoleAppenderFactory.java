package io.dropwizard.logging.common;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.NotNull;

/**
 * An {@link AppenderFactory} implementation which provides an appender that writes events to the console.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code type}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>The appender type. Must be {@code console}.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code threshold}</td>
 *         <td>{@code ALL}</td>
 *         <td>The lowest level of events to print to the console.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code timeZone}</td>
 *         <td>{@code UTC}</td>
 *         <td>The time zone to which event timestamps will be converted.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code target}</td>
 *         <td>{@code stdout}</td>
 *         <td>
 *             The name of the standard stream to which events will be written.
 *             Can be {@code stdout} or {@code stderr}.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code logFormat}</td>
 *         <td>the default format</td>
 *         <td>
 *             The Logback pattern with which events will be formatted. See
 *             <a href="http://logback.qos.ch/manual/layouts.html#conversionWord">the Logback documentation</a>
 *             for details.
 *         </td>
 *     </tr>
 * </table>
 *
 * @see AbstractAppenderFactory
 */
@JsonTypeName("console")
public class ConsoleAppenderFactory<E extends DeferredProcessingAware> extends AbstractOutputStreamAppenderFactory<E> {
    @SuppressWarnings("UnusedDeclaration")
    public enum ConsoleStream {
        STDOUT("System.out"),
        STDERR("System.err");

        private final String value;

        ConsoleStream(String value) {
            this.value = value;
        }

        public String get() {
            return value;
        }
    }

    @NotNull
    private ConsoleStream target = ConsoleStream.STDOUT;

    @JsonProperty
    public ConsoleStream getTarget() {
        return target;
    }

    @JsonProperty
    public void setTarget(ConsoleStream target) {
        this.target = target;
    }

    @Override
    protected OutputStreamAppender<E> appender(LoggerContext context) {
        final ConsoleAppender<E> appender = new ConsoleAppender<>();
        appender.setName("console-appender");
        appender.setContext(context);
        appender.setTarget(target.get());
        return appender;
    }
}
