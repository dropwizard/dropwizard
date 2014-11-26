package io.dropwizard.quartz;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * Defines a Quartz Scheduler 2.2.1 bundle suitable for use within a Dropwizard application. The following is a sample
 * .yml configuration block for setting up the Quartz Scheduler. If a RAM Job Store is preferred, simply comment out
 * the <b>jdbcjobStoreTX</b> definition in the yml file.
 * <p/>
 * <pre>
        quartzFactory:
          # Quartz Job Scheduler Logical Instance Name
          instanceName: MyQuartzScheduler

          threadPool:
            # Number of threads to allocate in the Quartz thread pool
            threadCount: 5

          jdbcjobStoreTX:
            driverDelegate: StdJDBC
            dataSource: MyQuartzDataSource

            # Quartz properties specific to your JDBC Data Source:
            properties:
              org.quartz.dataSource.MyQuartzDataSource.driver : com.mysql.jdbc.Driver
              org.quartz.dataSource.MyQuartzDataSource.URL : jdbc:mysql://localhost/mydb?autoReconnect=true
              org.quartz.dataSource.MyQuartzDataSource.user : quartzuser
              org.quartz.dataSource.MyQuartzDataSource.password : secret
 * </pre>
 * @param <T>
 */
public abstract class QuartzBundle<T extends Configuration> implements ConfiguredBundle<T>, QuartzConfiguration<T>
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
        properties.setProperty("org.quartz.scheduler.instanceName", quartzConfig.getInstanceName());
        addOptionalProperty("org.quartz.scheduler.instanceId", quartzConfig.getInstanceId(), properties);
        addOptionalProperty("org.quartz.scheduler.instanceIdGenerator.class", quartzConfig.getInstanceIdGenerator(), properties);
        addOptionalProperty("org.quartz.scheduler.threadName", quartzConfig.getThreadName(), properties);
        addOptionalProperty("org.quartz.scheduler.makeSchedulerThreadDaemon", quartzConfig.isMakeSchedulerThreadDaemon(), properties);
        addOptionalProperty("org.quartz.scheduler.threadsInheritContextClassLoaderOfInitializer", quartzConfig.isThreadsInheritContextClassLoaderOfInitializer(), properties);
        addOptionalProperty("org.quartz.scheduler.classLoadHelper.class", quartzConfig.getClassLoadHelperClass(), properties);
        properties.setProperty("org.quartz.scheduler.idleWaitTime", quartzConfig.getIdleWaitTime().toString());
        properties.setProperty("org.quartz.scheduler.dbFailureRetryInterval", quartzConfig.getDbFailureRetryInterval().toString());

        addOptionalProperty("org.quartz.scheduler.jobFactory.class", quartzConfig.getJobFactoryClass(), properties);

        addOptionalProperty("org.quartz.scheduler.userTransactionURL", quartzConfig.getUserTransactionURL(), properties);
        addOptionalProperty("org.quartz.scheduler.wrapJobExecutionInUserTransaction", quartzConfig.isWrapJobExecutionInUserTransaction(), properties);
        addOptionalProperty("org.quartz.scheduler.skipUpdateCheck", quartzConfig.isSkipUpdateCheck(), properties);
        addOptionalProperty("org.quartz.scheduler.batchTriggerAcquisitionMaxCount", quartzConfig.getBatchTriggerAcquisitionMaxCount(), properties);
        addOptionalProperty("org.quartz.scheduler.batchTriggerAcquisitionFireAheadTimeWindow", quartzConfig.getBatchTriggerAcquisitionFireAheadTimeWindow(), properties);

        //
        // THREAD POOL CONFIGURATION SETTINGS
        //

        properties.setProperty("org.quartz.threadPool.class", quartzConfig.getThreadPool().getThreadPoolClass());
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
            properties.setProperty("org.quartz.jobStore.class", quartzConfig.getJDBCJobStoreTX().getJobStoreClass());
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
            addOptionalProperty("org.quartz.jobStore.dataSource", dataSourceName, properties);

            Map<String, String> dataSourceConfigProperties = quartzConfig.getJDBCJobStoreTX().getProperties();
            for (String key : dataSourceConfigProperties.keySet())
            {
                properties.setProperty(key, dataSourceConfigProperties.get(key));
            }

        } else
        {
            properties.setProperty("org.quartz.jobStore.class", quartzConfig.getRamJobStore().getJobStoreClass());
            properties.setProperty("org.quartz.jobStore.misfireThreshold", quartzConfig.getRamJobStore().getMisfireThreshold().toString());
        }

        //
        // CUSTOM CONFIGURATION SETTINGS
        addCustomProperties(properties);

        LOG.debug("Instantiating a new Quartz StdSchedulerFactory...");
        schedulerFactory = new StdSchedulerFactory(properties);

        LOG.debug("Registering the Quartz Manager...");
        final QuartzManager managedQuartz = new QuartzManager(schedulerFactory.getScheduler());
        environment.lifecycle().manage(managedQuartz);
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
     * Access the scheduler factory configured by the bundle.
     *
     * @return Scheduler factory instance
     */
    public final SchedulerFactory getSchedulerFactory()
    {
        return schedulerFactory;
    }
}
