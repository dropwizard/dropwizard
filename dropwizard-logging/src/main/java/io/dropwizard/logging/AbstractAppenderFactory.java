package io.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * A base implementation of {@link AppenderFactory}.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code threshold}</td>
 *         <td>ALL</td>
 *         <td>The minimum event level the appender will handle.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code queueSize}</td>
 *         <td>{@link AsyncAppenderBase}</td>
 *         <td>The maximum capacity of the blocking queue.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code discardingThreshold}</td>
 *         <td>{@link AsyncAppenderBase}</td>
 *         <td>
 *             By default, when the blocking queue has 20% capacity remaining,
 *             it will drop events of level TRACE, DEBUG and INFO, keeping only
 *             events of level WARN and ERROR. To keep all events, set discardingThreshold to 0.
 *         </td>
 *     </tr>
 * </table>
 */
public abstract class AbstractAppenderFactory<E extends DeferredProcessingAware> implements AppenderFactory<E> {
    @NotNull
    protected Level threshold = Level.ALL;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int queueSize = AsyncAppenderBase.DEFAULT_QUEUE_SIZE;

    private int discardingThreshold = -1;

    @JsonProperty
    public int getQueueSize() {
        return queueSize;
    }

    @JsonProperty
    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    @JsonProperty
    public int getDiscardingThreshold() {
        return discardingThreshold;
    }

    @JsonProperty
    public void setDiscardingThreshold(int discardingThreshold) {
        this.discardingThreshold = discardingThreshold;
    }

    @JsonProperty
    public Level getThreshold() {
        return threshold;
    }

    @JsonProperty
    public void setThreshold(Level threshold) {
        this.threshold = threshold;
    }

    protected Appender<E> wrapAsync(Appender<E> appender, AsyncAppenderFactory<E> asyncAppenderFactory) {
        final AsyncAppenderBase<E> asyncAppender = asyncAppenderFactory.build();

        asyncAppender.setContext(appender.getContext());
        asyncAppender.setQueueSize(queueSize);
        asyncAppender.setDiscardingThreshold(discardingThreshold);
        asyncAppender.addAppender(appender);
        asyncAppender.start();

        return asyncAppender;
    }
}
