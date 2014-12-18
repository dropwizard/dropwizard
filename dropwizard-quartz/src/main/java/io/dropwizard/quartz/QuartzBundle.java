package io.dropwizard.quartz;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Defines a Quartz Scheduler 2.2.1 bundle suitable for use within a Dropwizard application. The following is a sample
 * .yml configuration block for setting up the Quartz Scheduler. If a RAM Job Store is preferred, simply comment out
 * the <b>jdbcjobStoreTX</b> definition in the yml file.
 * <p/>
 * <pre>
 * quartzFactory:
 *   # Quartz Job Scheduler Logical Instance Name
 *   instanceName: MyQuartzScheduler
 *
 *   threadPool:
 *     # Number of threads to allocate in the Quartz thread pool
 *     threadCount: 5
 *
 *   jdbcjobStoreTX:
 *     driverDelegate: StdJDBC
 *     dataSource: MyQuartzDataSource
 *
 *     # Quartz properties specific to your JDBC Data Source:
 *     properties:
 *       org.quartz.dataSource.MyQuartzDataSource.driver : com.mysql.jdbc.Driver
 *       org.quartz.dataSource.MyQuartzDataSource.URL : jdbc:mysql://localhost/mydb?autoReconnect=true
 *       org.quartz.dataSource.MyQuartzDataSource.user : quartzuser
 *       org.quartz.dataSource.MyQuartzDataSource.password : secret
 * </pre>
 *
 * @param <T>
 */
public abstract class QuartzBundle<T extends Configuration> implements ConfiguredBundle<T>, QuartzConfiguration<T>, SchedulerFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(QuartzBundle.class);

    private SchedulerFactory schedulerFactory;

    private void addOptionalProperty(String propertyName, Object value, Properties properties)
    {
        if (value != null)
        {
            properties.setProperty(propertyName, value.toString());
        }
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception
    {
        final QuartzFactory quartzConfig = getQuartzFactory(configuration);

        LOG.debug("Populating Quartz properties from the QuartzFactory instance...");
        Properties properties = new Properties();

        // Main Scheduler Configuration
        properties.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, quartzConfig.getInstanceName());
        addOptionalProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID, quartzConfig.getInstanceId(), properties);
        addOptionalProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID_GENERATOR_CLASS, quartzConfig.getInstanceIdGenerator(), properties);
        addOptionalProperty(StdSchedulerFactory.PROP_SCHED_THREAD_NAME, quartzConfig.getThreadName(), properties);
        addOptionalProperty(StdSchedulerFactory.PROP_SCHED_MAKE_SCHEDULER_THREAD_DAEMON, quartzConfig.isMakeSchedulerThreadDaemon(), properties);
        addOptionalProperty(StdSchedulerFactory.PROP_SCHED_SCHEDULER_THREADS_INHERIT_CONTEXT_CLASS_LOADER_OF_INITIALIZING_THREAD, quartzConfig.isThreadsInheritContextClassLoaderOfInitializer(), properties);
        addOptionalProperty(StdSchedulerFactory.PROP_SCHED_CLASS_LOAD_HELPER_CLASS, quartzConfig.getClassLoadHelperClass(), properties);
        properties.setProperty(StdSchedulerFactory.PROP_SCHED_IDLE_WAIT_TIME, quartzConfig.getIdleWaitTime().toString());
        properties.setProperty(StdSchedulerFactory.PROP_SCHED_DB_FAILURE_RETRY_INTERVAL, quartzConfig.getDbFailureRetryInterval().toString());

        addOptionalProperty(StdSchedulerFactory.PROP_SCHED_JOB_FACTORY_CLASS, quartzConfig.getJobFactoryClass(), properties);

        addOptionalProperty(StdSchedulerFactory.PROP_SCHED_USER_TX_URL, quartzConfig.getUserTransactionURL(), properties);
        addOptionalProperty(StdSchedulerFactory.PROP_SCHED_WRAP_JOB_IN_USER_TX, quartzConfig.isWrapJobExecutionInUserTransaction(), properties);
        addOptionalProperty(StdSchedulerFactory.PROP_SCHED_SKIP_UPDATE_CHECK, quartzConfig.isSkipUpdateCheck(), properties);
        addOptionalProperty(StdSchedulerFactory.PROP_SCHED_MAX_BATCH_SIZE, quartzConfig.getBatchTriggerAcquisitionMaxCount(), properties);
        addOptionalProperty(StdSchedulerFactory.PROP_SCHED_BATCH_TIME_WINDOW, quartzConfig.getBatchTriggerAcquisitionFireAheadTimeWindow(), properties);

        //
        // THREAD POOL CONFIGURATION SETTINGS
        //

        properties.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, quartzConfig.getThreadPool().getThreadPoolClass());

        //
        // SimpleThreadPool specific configuration properties
        //
        properties.setProperty("org.quartz.threadPool.threadCount", quartzConfig.getThreadPool().getThreadCount().toString());
        properties.setProperty("org.quartz.threadPool.threadPriority", quartzConfig.getThreadPool().getThreadPriority().toString());
        properties.setProperty("org.quartz.threadPool.makeThreadsDaemons", quartzConfig.getThreadPool().getMakeThreadsDaemons().toString());
        addOptionalProperty("org.quartz.threadPool.threadNamePrefix", quartzConfig.getThreadPool().getThreadNamePrefix(), properties);

        //
        // JOB STORE CONFIGURATION SETTINGS
        //

        if (quartzConfig.getRamJobStore() == null && quartzConfig.getJDBCJobStoreTX() == null)
        {
            throw new IllegalStateException("Either the RAM Job Store or the JDBC Job Store TX must be configured");
        }

        if (quartzConfig.getJDBCJobStoreTX() != null)
        {
            properties.setProperty(StdSchedulerFactory.PROP_JOB_STORE_CLASS, quartzConfig.getJDBCJobStoreTX().getJobStoreClass());
            properties.setProperty("org.quartz.jobStore.misfireThreshold", quartzConfig.getJDBCJobStoreTX().getMisfireThreshold().toString());
            addOptionalProperty("org.quartz.jobStore.driverDelegateClass", quartzConfig.getJDBCJobStoreTX().getDriverDelegate().getDialect(), properties);

            addOptionalProperty("org.quartz.jobStore.tablePrefix", quartzConfig.getJDBCJobStoreTX().getTablePrefix(), properties);
            addOptionalProperty("org.quartz.jobStore.useProperties", quartzConfig.getJDBCJobStoreTX().getUseProperties(), properties);
            addOptionalProperty("org.quartz.jobStore.isClustered", quartzConfig.getJDBCJobStoreTX().getClustered(), properties);
            addOptionalProperty("org.quartz.jobStore.clusterCheckinInterval", quartzConfig.getJDBCJobStoreTX().getClusterCheckinInterval(), properties);
            addOptionalProperty("org.quartz.jobStore.maxMisfiresToHandleAtATime", quartzConfig.getJDBCJobStoreTX().getMaxMisfiresToHandleAtATime(), properties);
            addOptionalProperty("org.quartz.jobStore.dontSetAutoCommitFalse", quartzConfig.getJDBCJobStoreTX().getDontSetAutoCommitFalse(), properties);
            addOptionalProperty("org.quartz.jobStore.selectWithLockSQL", quartzConfig.getJDBCJobStoreTX().getSelectWithLockSQL(), properties);
            addOptionalProperty("org.quartz.jobStore.txIsolationLevelSerializable", quartzConfig.getJDBCJobStoreTX().getTxIsolationLevelSerializable(), properties);
            addOptionalProperty("org.quartz.jobStore.acquireTriggersWithinLock", quartzConfig.getJDBCJobStoreTX().getAcquireTriggersWithinLock(), properties);
            addOptionalProperty("org.quartz.jobStore.lockHandler.class", quartzConfig.getJDBCJobStoreTX().getLockHandlerClass(), properties);
            addOptionalProperty("org.quartz.jobStore.driverDelegateInitString", quartzConfig.getJDBCJobStoreTX().getDriverDelegateInitString(), properties);

            String dataSourceName = quartzConfig.getJDBCJobStoreTX().getDataSource();
            if (dataSourceName == null)
            {
                throw new SchedulerConfigException("JDBC Job Store must define a non-null org.quartz.jobStore.dataSource");
            } else
            {
                properties.setProperty("org.quartz.jobStore.dataSource", dataSourceName);
            }

            Map<String, String> dataSourceConfigProperties = quartzConfig.getJDBCJobStoreTX().getProperties();
            for (Map.Entry<String, String> entry : dataSourceConfigProperties.entrySet())
            {
                properties.setProperty(entry.getKey(), entry.getValue());
            }

        } else
        {
            properties.setProperty(StdSchedulerFactory.PROP_JOB_STORE_CLASS, quartzConfig.getRamJobStore().getJobStoreClass());
            properties.setProperty("org.quartz.jobStore.misfireThreshold", quartzConfig.getRamJobStore().getMisfireThreshold().toString());
        }

        //
        // CUSTOM CONFIGURATION SETTINGS
        addCustomProperties(properties);

        LOG.debug("Instantiating a new Quartz SchedulerFactory...");
        schedulerFactory = createSchedulerFactory(properties);

        Scheduler scheduler = schedulerFactory.getScheduler();

        LOG.debug("Registering the Dropwizard Quartz Manager...");
        final QuartzManager managedQuartz = new QuartzManager(scheduler);
        environment.lifecycle().manage(managedQuartz);

        LOG.debug("Registering Quartz Health Checks...");
        environment.healthChecks().register("quartz", new QuartzHealthCheck(schedulerFactory));

        LOG.debug("Registering Quartz Tasks...");
        environment.admin().addTask(new QuartzListJobsTask(schedulerFactory, environment.getObjectMapper()));
        environment.admin().addTask(new QuartzSchedulerTask(schedulerFactory));
    }

    /**
     * Default implementation creates an instance of {@link org.quartz.impl.StdSchedulerFactory}. If your unique
     * implementation requires a {@link org.quartz.impl.DirectSchedulerFactory} or a truly custom
     * implementation of {@link org.quartz.SchedulerFactory}, then override this method.
     *
     * @param properties Configuration parameters are extracted from Dropwizard configuration
     * @return Non-null instance of a {@link org.quartz.SchedulerFactory} implementation
     * @throws SchedulerException
     */
    protected SchedulerFactory createSchedulerFactory(Properties properties) throws SchedulerException
    {
        return new StdSchedulerFactory(properties);
    }

    /**
     * Call back that allows the concrete class an easy mechanism for adding in any arbitrary name-value keys of type
     * <b>org.quartz.context.key.SOME_KEY</b>, custom thread pool properties, job store properties, global listeners,
     * etc.
     *
     * @param properties Properties defined in the Quartz factory section of the .yml file
     */
    protected void addCustomProperties(Properties properties)
    {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Scheduler getScheduler() throws SchedulerException
    {
        return schedulerFactory.getScheduler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Scheduler getScheduler(String schedName) throws SchedulerException
    {
        return schedulerFactory.getScheduler(schedName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Scheduler> getAllSchedulers() throws SchedulerException
    {
        return schedulerFactory.getAllSchedulers();
    }
}
