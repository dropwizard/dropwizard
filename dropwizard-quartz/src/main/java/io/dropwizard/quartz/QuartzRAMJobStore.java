package io.dropwizard.quartz;

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
 * <td>{@code misfireThreshold}</td>
 * <td>60000</td>
 * <td>org.quartz.jobStore.misfireThreshold</td>
 * </tr>
 * </table>
 */
public class QuartzRAMJobStore
{
    //Maps to org.quartz.jobStore.class
    private static final String jobStoreClass = "org.quartz.simpl.RAMJobStore";

    //Maps to org.quartz.jobStore.misfireThreshold
    private Integer misfireThreshold = 60000;

    public String getJobStoreClass()
    {
        return jobStoreClass;
    }

    public Integer getMisfireThreshold()
    {
        return misfireThreshold;
    }

    public void setMisfireThreshold(Integer misfireThreshold)
    {
        this.misfireThreshold = misfireThreshold;
    }
}
