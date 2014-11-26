package io.dropwizard.quartz;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * A factory for {@link io.dropwizard.quartz.QuartzBundle}s. For complete details on the configuration properties and
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
 * <td>{@code instanceName}</td>
 * <td>Quartz Scheduler</td>
 * <td>org.quartz.scheduler.instanceName</td>
 * </tr>
 * <tr>
 * <td>{@code instanceId}</td>
 * <td>null</td>
 * <td>org.quartz.scheduler.instanceId</td>
 * </tr>
 * <tr>
 * <td>{@code instanceIdGenerator}</td>
 * <td>null</td>
 * <td>org.quartz.scheduler.instanceIdGenerator.class</td>
 * </tr>
 * <tr>
 * <td>{@code threadName}</td>
 * <td>null</td>
 * <td>org.quartz.scheduler.threadName</td>
 * </tr>
 * <tr>
 * <td>{@code makeSchedulerThreadDaemon}</td>
 * <td>null</td>
 * <td>org.quartz.scheduler.makeSchedulerThreadDaemon</td>
 * </tr>
 * <tr>
 * <td>{@code threadsInheritContextClassLoaderOfInitializer}</td>
 * <td>false</td>
 * <td>org.quartz.scheduler.threadsInheritContextClassLoaderOfInitializer</td>
 * </tr>
 * <tr>
 * <td>{@code idleWaitTime}</td>
 * <td>30000</td>
 * <td>org.quartz.scheduler.idleWaitTime</td>
 * </tr>
 * <tr>
 * <td>{@code dbFailureRetryInterval}</td>
 * <td>15000</td>
 * <td>org.quartz.scheduler.dbFailureRetryInterval</td>
 * </tr>
 * <tr>
 * <td>{@code classLoadHelperClass}</td>
 * <td>null</td>
 * <td>org.quartz.scheduler.classLoadHelper.class</td>
 * </tr>
 * <tr>
 * <td>{@code jobFactoryClass}</td>
 * <td>null</td>
 * <td>org.quartz.scheduler.jobFactory.class</td>
 * </tr>
 * <tr>
 * <td>{@code userTransactionURL}</td>
 * <td>null</td>
 * <td>org.quartz.scheduler.userTransactionURL</td>
 * </tr>
 * <tr>
 * <td>{@code wrapJobExecutionInUserTransaction}</td>
 * <td>false</td>
 * <td>org.quartz.scheduler.wrapJobExecutionInUserTransaction</td>
 * </tr>
 * <tr>
 * <td>{@code skipUpdateCheck}</td>
 * <td>false</td>
 * <td>org.quartz.scheduler.skipUpdateCheck</td>
 * </tr>
 * <tr>
 * <td>{@code batchTriggerAcquisitionMaxCount}</td>
 * <td>1</td>
 * <td>org.quartz.scheduler.batchTriggerAcquisitionMaxCount</td>
 * </tr>
 * <tr>
 * <td>{@code batchTriggerAcquisitionFireAheadTimeWindow}</td>
 * <td>0</td>
 * <td>org.quartz.scheduler.batchTriggerAcquisitionFireAheadTimeWindow</td>
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
 * <td><b>-</b></td>
 * <td>org.quartz.simpl.RAMJobStore</td>
 * <td>org.quartz.jobStore.class</td>
 * </tr>
 * </table>
 * <p/>
 * <b>NOTE:</b> The default configuration uses a RAMJobStore. For example on how to configure a JDBCJobStoreTX, please
 * see the sample documentation at {@link io.dropwizard.quartz.QuartzBundle}.
 */
public class QuartzFactory
{
    //
    //
    // NOTE:  Properties were extracted from the Quartz Scheduler Configuration Guide Version 2.2.1
    //        http://www.quartz-scheduler.org/generated/2.2.1/pdf/Quartz_Scheduler_Configuration_Guide.pdf
    //

    //
    // MAIN SCHEDULER CONFIGURATION SETTINGS
    //

    @NotNull
    //Maps to org.quartz.scheduler.instanceName
    private String instanceName = "Quartz Scheduler";

    //Maps to org.quartz.scheduler.instanceId
    private String instanceId = null;

    //Maps to org.quartz.scheduler.instanceIdGenerator.class
    private String instanceIdGenerator = null;

    //Maps to org.quartz.scheduler.threadName;
    private String threadName = null;

    //Maps to org.quartz.scheduler.makeSchedulerThreadDaemon
    private Boolean makeSchedulerThreadDaemon = false;

    //Maps to org.quartz.scheduler.threadsInheritContextClassLoaderOfInitializer
    private Boolean threadsInheritContextClassLoaderOfInitializer = false;

    @Min(5000)
    //Maps to org.quartz.scheduler.idleWaitTime
    private Integer idleWaitTime = 30000;

    @Min(0)
    //Maps to org.quartz.scheduler.dbFailureRetryInterval
    private Integer dbFailureRetryInterval = 15000;

    //Maps to org.quartz.scheduler.classLoadHelper.class
    private String classLoadHelperClass = null;

    //Maps to org.quartz.scheduler.jobFactory.class
    private String jobFactoryClass = null;

    //Maps to org.quartz.scheduler.userTransactionURL
    private String userTransactionURL = null;

    //Maps to org.quartz.scheduler.wrapJobExecutionInUserTransaction
    private Boolean wrapJobExecutionInUserTransaction = false;

    //Maps to org.quartz.scheduler.skipUpdateCheck
    private Boolean skipUpdateCheck = false;

    //Maps to org.quartz.scheduler.batchTriggerAcquisitionMaxCount
    private Integer batchTriggerAcquisitionMaxCount = 1;

    //Maps to org.quartz.scheduler.batchTriggerAcquisitionFireAheadTimeWindow
    private Long batchTriggerAcquisitionFireAheadTimeWindow = 0l;

    @NotNull
    private QuartzThreadPool threadPool;

    private QuartzRAMJobStore ramJobStore = new QuartzRAMJobStore();

    private QuartzJDBCJobStoreTX jdbcJobStoreTX;

    public String getInstanceName()
    {
        return instanceName;
    }

    public void setInstanceName(String instanceName)
    {
        this.instanceName = instanceName;
    }

    public String getInstanceId()
    {
        return instanceId;
    }

    public void setInstanceId(String instanceId)
    {
        this.instanceId = instanceId;
    }

    public String getInstanceIdGenerator()
    {
        return instanceIdGenerator;
    }

    public void setInstanceIdGenerator(String instanceIdGenerator)
    {
        this.instanceIdGenerator = instanceIdGenerator;
    }

    public String getThreadName()
    {
        return threadName;
    }

    public void setThreadName(String threadName)
    {
        this.threadName = threadName;
    }

    public Boolean isMakeSchedulerThreadDaemon()
    {
        return makeSchedulerThreadDaemon;
    }

    public void setMakeSchedulerThreadDaemon(boolean makeSchedulerThreadDaemon)
    {
        this.makeSchedulerThreadDaemon = makeSchedulerThreadDaemon;
    }

    public Boolean isThreadsInheritContextClassLoaderOfInitializer()
    {
        return threadsInheritContextClassLoaderOfInitializer;
    }

    public void setThreadsInheritContextClassLoaderOfInitializer(boolean threadsInheritContextClassLoaderOfInitializer)
    {
        this.threadsInheritContextClassLoaderOfInitializer = threadsInheritContextClassLoaderOfInitializer;
    }

    public Integer getIdleWaitTime()
    {
        return idleWaitTime;
    }

    public void setIdleWaitTime(int idleWaitTime)
    {
        this.idleWaitTime = idleWaitTime;
    }

    public Integer getDbFailureRetryInterval()
    {
        return dbFailureRetryInterval;
    }

    public void setDbFailureRetryInterval(int dbFailureRetryInterval)
    {
        this.dbFailureRetryInterval = dbFailureRetryInterval;
    }

    public String getClassLoadHelperClass()
    {
        return classLoadHelperClass;
    }

    public void setClassLoadHelperClass(String classLoadHelperClass)
    {
        this.classLoadHelperClass = classLoadHelperClass;
    }

    public String getJobFactoryClass()
    {
        return jobFactoryClass;
    }

    public void setJobFactoryClass(String jobFactoryClass)
    {
        this.jobFactoryClass = jobFactoryClass;
    }

    public String getUserTransactionURL()
    {
        return userTransactionURL;
    }

    public void setUserTransactionURL(String userTransactionURL)
    {
        this.userTransactionURL = userTransactionURL;
    }

    public Boolean isWrapJobExecutionInUserTransaction()
    {
        return wrapJobExecutionInUserTransaction;
    }

    public void setWrapJobExecutionInUserTransaction(boolean wrapJobExecutionInUserTransaction)
    {
        this.wrapJobExecutionInUserTransaction = wrapJobExecutionInUserTransaction;
    }

    public Boolean isSkipUpdateCheck()
    {
        return skipUpdateCheck;
    }

    public void setSkipUpdateCheck(boolean skipUpdateCheck)
    {
        this.skipUpdateCheck = skipUpdateCheck;
    }

    public Integer getBatchTriggerAcquisitionMaxCount()
    {
        return batchTriggerAcquisitionMaxCount;
    }

    public void setBatchTriggerAcquisitionMaxCount(int batchTriggerAcquisitionMaxCount)
    {
        this.batchTriggerAcquisitionMaxCount = batchTriggerAcquisitionMaxCount;
    }

    public Long getBatchTriggerAcquisitionFireAheadTimeWindow()
    {
        return batchTriggerAcquisitionFireAheadTimeWindow;
    }

    public void setBatchTriggerAcquisitionFireAheadTimeWindow(long batchTriggerAcquisitionFireAheadTimeWindow)
    {
        this.batchTriggerAcquisitionFireAheadTimeWindow = batchTriggerAcquisitionFireAheadTimeWindow;
    }

    public QuartzThreadPool getThreadPool()
    {
        return threadPool;
    }

    public void setThreadPool(QuartzThreadPool threadPool)
    {
        this.threadPool = threadPool;
    }

    public QuartzRAMJobStore getRamJobStore()
    {
        return ramJobStore;
    }

    public void setRamJobStore(QuartzRAMJobStore ramJobStore)
    {
        this.ramJobStore = ramJobStore;
    }

    public QuartzJDBCJobStoreTX getJDBCJobStoreTX()
    {
        return jdbcJobStoreTX;
    }

    public void setJDBCJobStoreTX(QuartzJDBCJobStoreTX jobStoreTX)
    {
        this.jdbcJobStoreTX = jobStoreTX;
    }
}
