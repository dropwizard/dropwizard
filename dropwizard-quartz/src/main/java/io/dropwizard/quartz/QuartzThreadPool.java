package io.dropwizard.quartz;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Thread pool factory for {@link io.dropwizard.quartz.QuartzBundle}s. For complete details on the configuration properties and
 * acceptable values, please refer to the
 * <a href="http://www.quartz-scheduler.org/generated/2.2.1/pdf/Quartz_Scheduler_Configuration_Guide.pdf">Quartz
 * Scheduler Configuration Guide, Version 2.2.1</a>.
 * <p/>
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 * <tr>
 * <td>Name</td>
 * <td>Default</td>
 * <td>Quartz Property Mapping</td>
 * </tr>
 * <tr>
 * <td>{@code threadPoolClass}</td>
 * <td>org.quartz.simpl.SimpleThreadPool</td>
 * <td>org.quartz.threadPool.class</td>
 * </tr>
 * <tr>
 * <td>{@code threadCount}</td>
 * <td>3</td>
 * <td>org.quartz.threadPool.threadCount</td>
 * </tr>
 * <tr>
 * <td>{@code threadPriority}</td>
 * <td>5 (NORMAL)</td>
 * <td>org.quartz.threadPool.threadPriority</td>
 * </tr>
 * <tr>
 * <td>{@code threadNamePrefix}</td>
 * <td>null</td>
 * <td>org.quartz.threadPool.threadNamePrefix</td>
 * </tr>
 * <tr>
 * <td>{@code makeThreadsDaemons}</td>
 * <td>false</td>
 * <td>org.quartz.threadPool.makeThreadsDaemons</td>
 * </tr>
 * </table>
 */
public class QuartzThreadPool
{
    @NotNull
    //Maps to org.quartz.threadPool.class
    private String threadPoolClass = "org.quartz.simpl.SimpleThreadPool";

    @Min(1)
    @Max(100)
    //Maps to org.quartz.threadPool.threadCount
    private Integer threadCount = 3;

    @Min(1)
    @Max(10)
    //Maps to org.quartz.threadPool.threadPriority
    private Integer threadPriority = 5;

    //Maps to org.quartz.threadPool.threadNamePrefix
    private String threadNamePrefix = null;

    //Maps to org.quartz.threadPool.makeThreadsDaemons
    private Boolean makeThreadsDaemons = false;

    public String getThreadPoolClass()
    {
        return threadPoolClass;
    }

    public void setThreadPoolClass(String threadPoolClass)
    {
        this.threadPoolClass = threadPoolClass;
    }

    public Integer getThreadCount()
    {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount)
    {
        this.threadCount = threadCount;
    }

    public Integer getThreadPriority()
    {
        return threadPriority;
    }

    public void setThreadPriority(Integer threadPriority)
    {
        this.threadPriority = threadPriority;
    }

    public String getThreadNamePrefix()
    {
        return threadNamePrefix;
    }

    public void setThreadNamePrefix(String threadNamePrefix)
    {
        this.threadNamePrefix = threadNamePrefix;
    }

    public Boolean getMakeThreadsDaemons()
    {
        return makeThreadsDaemons;
    }

    public void setMakeThreadsDaemons(Boolean makeThreadsDaemons)
    {
        this.makeThreadsDaemons = makeThreadsDaemons;
    }
}
