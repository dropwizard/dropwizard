package io.dropwizard.quartz;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Thread pool factory for {@link QuartzBundle}s. For complete details on the configuration properties and
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
 * <tr>
 * <td>{@code driverDelegate}</td>
 * <td>null</td>
 * <td>org.quartz.jobStore.driverDelegateClass</td>
 * </tr>
 * <tr>
 * <td>{@code dataSource}</td>
 * <td>null</td>
 * <td>org.quartz.jobStore.dataSource</td>
 * </tr>
 * <tr>
 * <td>{@code tablePrefix}</td>
 * <td>null</td>
 * <td>org.quartz.jobStore.tablePrefix</td>
 * </tr>
 * <tr>
 * <td>{@code useProperties}</td>
 * <td>null</td>
 * <td>org.quartz.jobStore.useProperties</td>
 * </tr>
 * <tr>
 * <td>{@code clustered}</td>
 * <td>null</td>
 * <td>org.quartz.jobStore.isClustered</td>
 * </tr>
 * <tr>
 * <td>{@code clusterCheckinInterval}</td>
 * <td>null</td>
 * <td>org.quartz.jobStore.clusterCheckinInterval</td>
 * </tr>
 * <tr>
 * <td>{@code maxMisfiresToHandleAtATime}</td>
 * <td>null</td>
 * <td>org.quartz.jobStore.maxMisfiresToHandleAtATime</td>
 * </tr>
 * <tr>
 * <td>{@code dontSetAutoCommitFalse}</td>
 * <td>null</td>
 * <td>org.quartz.jobStore.dontSetAutoCommitFalse</td>
 * </tr>
 * <tr>
 * <td>{@code selectWithLockSQL}</td>
 * <td>null</td>
 * <td>org.quartz.jobStore.selectWithLockSQL</td>
 * </tr>
 * <tr>
 * <td>{@code txIsolationLevelSerializable}</td>
 * <td>null</td>
 * <td>org.quartz.jobStore.txIsolationLevelSerializable</td>
 * </tr>
 * <tr>
 * <td>{@code acquireTriggersWithinLock}</td>
 * <td>null</td>
 * <td>org.quartz.jobStore.acquireTriggersWithinLock</td>
 * </tr>
 * <tr>
 * <td>{@code lockHandlerClass}</td>
 * <td>null</td>
 * <td>org.quartz.jobStore.lockHandler.class</td>
 * </tr>
 * <tr>
 * <td>{@code driverDelegateInitString}</td>
 * <td>null</td>
 * <td>org.quartz.jobStore.driverDelegateInitString</td>
 * </tr>
 * </table>
 */
public class QuartzJDBCJobStoreTX
{
    //Maps to org.quartz.jobStore.class
    private static final String jobStoreClass = "org.quartz.impl.jdbcjobstore.JobStoreTX";

    @SuppressWarnings("UnusedDeclaration")
    public enum DriverDelegateClass
    {
        Sybase("org.quartz.impl.jdbcjobstore.SybaseDelegate"),
        Pointbase("org.quartz.impl.jdbcjobstore.PointbaseDelegate"),
        HSQLDB("org.quartz.impl.jdbcjobstore.HSQLDBDelegate"),
        DB2v8("org.quartz.impl.jdbcjobstore.DB2v8Delegate"),
        DB2v7("org.quartz.impl.jdbcjobstore.DB2v7Delegate"),
        DB2v6("org.quartz.impl.jdbcjobstore.DB2v6Delegate"),
        StdJDBC("org.quartz.impl.jdbcjobstore.StdJDBCDelegate"),
        MSSQL("org.quartz.impl.jdbcjobstore.MSSQLDelegate"),
        PostgreSQL("org.quartz.impl.jdbcjobstore.PostgreSQLDelegate"),
        WebLogic("org.quartz.impl.jdbcjobstore.WebLogicDelegate"),
        Oracle("org.quartz.impl.jdbcjobstore.oracle.OracleDelegate"),
        WebLogicOracle("org.quartz.impl.jdbcjobstore.oracle.weblogic.WebLogicOracleDelegate"),
        CloudscapeDelegate("org.quartz.impl.jdbcjobstore.CloudscapeDelegate");

        private final String dialect;

        private DriverDelegateClass(String dialect)
        {
            this.dialect = dialect;
        }

        public String getDialect()
        {
            return dialect;
        }
    }


    //Maps to org.quartz.jobStore.misfireThreshold
    private Integer misfireThreshold = 60000;

    //Maps to org.quartz.jobStore.driverDelegateClass;
    private DriverDelegateClass driverDelegate = DriverDelegateClass.StdJDBC;

    //Maps to org.quartz.jobStore.dataSource
    private String dataSource = null;

    //Maps to org.quartz.jobStore.tablePrefix
    private String tablePrefix = null;

    //Maps to org.quartz.jobStore.useProperties
    private Boolean useProperties = null;

    //Maps to org.quartz.jobStore.isClustered
    private Boolean clustered = null;

    //Maps to org.quartz.jobStore.clusterCheckinInterval
    private Long clusterCheckinInterval = null;

    //Maps to org.quartz.jobStore.maxMisfiresToHandleAtATime
    private Integer maxMisfiresToHandleAtATime = null;

    //Maps to org.quartz.jobStore.dontSetAutoCommitFalse
    private Boolean dontSetAutoCommitFalse = null;

    //Maps to org.quartz.jobStore.selectWithLockSQL
    private String selectWithLockSQL = null;

    //Maps to org.quartz.jobStore.txIsolationLevelSerializable
    private Boolean txIsolationLevelSerializable = null;

    //Maps to org.quartz.jobStore.acquireTriggersWithinLock
    private Boolean acquireTriggersWithinLock = null;

    //Maps to org.quartz.jobStore.lockHandler.class
    private String lockHandlerClass = null;

    //Maps to org.quartz.jobStore.driverDelegateInitString
    private String driverDelegateInitString = null;

    @NotNull
    private Map<String, String> properties = Maps.newLinkedHashMap();

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

    public DriverDelegateClass getDriverDelegate()
    {
        return driverDelegate;
    }

    public void setDriverDelegate(DriverDelegateClass driverDelegate)
    {
        this.driverDelegate = driverDelegate;
    }

    public String getDataSource()
    {
        return dataSource;
    }

    public void setDataSource(String dataSource)
    {
        this.dataSource = dataSource;
    }

    public String getTablePrefix()
    {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix)
    {
        this.tablePrefix = tablePrefix;
    }

    public Boolean getUseProperties()
    {
        return useProperties;
    }

    public void setUseProperties(Boolean useProperties)
    {
        this.useProperties = useProperties;
    }

    public Boolean getClustered()
    {
        return clustered;
    }

    public void setClustered(Boolean clustered)
    {
        this.clustered = clustered;
    }

    public Long getClusterCheckinInterval()
    {
        return clusterCheckinInterval;
    }

    public void setClusterCheckinInterval(Long clusterCheckinInterval)
    {
        this.clusterCheckinInterval = clusterCheckinInterval;
    }

    public Integer getMaxMisfiresToHandleAtATime()
    {
        return maxMisfiresToHandleAtATime;
    }

    public void setMaxMisfiresToHandleAtATime(Integer maxMisfiresToHandleAtATime)
    {
        this.maxMisfiresToHandleAtATime = maxMisfiresToHandleAtATime;
    }

    public Boolean getDontSetAutoCommitFalse()
    {
        return dontSetAutoCommitFalse;
    }

    public void setDontSetAutoCommitFalse(Boolean dontSetAutoCommitFalse)
    {
        this.dontSetAutoCommitFalse = dontSetAutoCommitFalse;
    }

    public String getSelectWithLockSQL()
    {
        return selectWithLockSQL;
    }

    public void setSelectWithLockSQL(String selectWithLockSQL)
    {
        this.selectWithLockSQL = selectWithLockSQL;
    }

    public Boolean getTxIsolationLevelSerializable()
    {
        return txIsolationLevelSerializable;
    }

    public void setTxIsolationLevelSerializable(Boolean txIsolationLevelSerializable)
    {
        this.txIsolationLevelSerializable = txIsolationLevelSerializable;
    }

    public Boolean getAcquireTriggersWithinLock()
    {
        return acquireTriggersWithinLock;
    }

    public void setAcquireTriggersWithinLock(Boolean acquireTriggersWithinLock)
    {
        this.acquireTriggersWithinLock = acquireTriggersWithinLock;
    }

    public String getLockHandlerClass()
    {
        return lockHandlerClass;
    }

    public void setLockHandlerClass(String lockHandlerClass)
    {
        this.lockHandlerClass = lockHandlerClass;
    }

    public String getDriverDelegateInitString()
    {
        return driverDelegateInitString;
    }

    public void setDriverDelegateInitString(String driverDelegateInitString)
    {
        this.driverDelegateInitString = driverDelegateInitString;
    }

    @JsonProperty
    public Map<String, String> getProperties() {
        return properties;
    }

    @JsonProperty
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
