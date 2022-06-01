package io.dropwizard.db;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.util.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DataSourceConfigurationTest {

    @Test
    void testFullConfiguration() throws Exception {
        DataSourceFactory ds = getDataSourceFactory("yaml/full_db_pool.yml");

        assertThat(ds.getDriverClass()).isEqualTo("org.postgresql.Driver");
        assertThat(ds.getUser()).isEqualTo("pg-user");
        assertThat(ds.getUrl()).isEqualTo("jdbc:postgresql://db.example.com/db-prod");
        assertThat(ds.getPassword()).isEqualTo("iAMs00perSecrEET");
        assertThat(ds.getProperties()).containsEntry("charSet", "UTF-8");
        assertThat(ds.getMaxWaitForConnection()).isEqualTo(Duration.seconds(1));
        assertThat(ds.getValidationQuery()).isEqualTo(Optional.of("/* MyService Health Check */ SELECT 1"));
        assertThat(ds.getMinSize()).isEqualTo(8);
        assertThat(ds.getInitialSize()).isEqualTo(15);
        assertThat(ds.getMaxSize()).isEqualTo(32);
        assertThat(ds.getCheckConnectionWhileIdle()).isFalse();
        assertThat(ds.getEvictionInterval()).isEqualTo(Duration.seconds(10));
        assertThat(ds.getMinIdleTime()).isEqualTo(Duration.minutes(1));
        assertThat(ds.getValidationInterval()).isEqualTo(Duration.minutes(1));
        assertThat(ds.isAutoCommentsEnabled()).isFalse();
        assertThat(ds.getReadOnlyByDefault()).isFalse();
        assertThat(ds.isRemoveAbandoned()).isTrue();
        assertThat(ds.getRemoveAbandonedTimeout()).isEqualTo(Duration.seconds(15L));
        assertThat(ds.getAbandonWhenPercentageFull()).isEqualTo(75);
        assertThat(ds.isAlternateUsernamesAllowed()).isTrue();
        assertThat(ds.getCommitOnReturn()).isTrue();
        assertThat(ds.getRollbackOnReturn()).isTrue();
        assertThat(ds.getAutoCommitByDefault()).isFalse();
        assertThat(ds.getDefaultCatalog()).isEqualTo("test_catalog");
        assertThat(ds.getDefaultTransactionIsolation())
                .isEqualTo(DataSourceFactory.TransactionIsolation.READ_COMMITTED);
        assertThat(ds.getUseFairQueue()).isFalse();
        assertThat(ds.getInitializationQuery()).isEqualTo("insert into connections_log(ts) values (now())");
        assertThat(ds.getLogAbandonedConnections()).isTrue();
        assertThat(ds.getLogValidationErrors()).isTrue();
        assertThat(ds.getMaxConnectionAge()).isEqualTo(Optional.of(Duration.hours(1)));
        assertThat(ds.getCheckConnectionOnBorrow()).isTrue();
        assertThat(ds.getCheckConnectionOnConnect()).isFalse();
        assertThat(ds.getCheckConnectionOnReturn()).isTrue();
        assertThat(ds.getValidationQueryTimeout()).isEqualTo(Optional.of(Duration.seconds(3)));
        assertThat(ds.getValidatorClassName()).isEqualTo(Optional.of("io.dropwizard.db.CustomConnectionValidator"));
        assertThat(ds.getJdbcInterceptors()).isEqualTo(Optional.of("StatementFinalizer;SlowQueryReport"));
        assertThat(ds.isIgnoreExceptionOnPreLoad()).isTrue();
    }

    @Test
    void testMinimalConfiguration() throws Exception {
        DataSourceFactory ds = getDataSourceFactory("yaml/minimal_db_pool.yml");

        assertThat(ds.getDriverClass()).isEqualTo("org.postgresql.Driver");
        assertThat(ds.getUser()).isEqualTo("pg-user");
        assertThat(ds.getUrl()).isEqualTo("jdbc:postgresql://db.example.com/db-prod");
        assertThat(ds.getPassword()).isEqualTo("iAMs00perSecrEET");
        assertThat(ds.getProperties()).isEmpty();
        assertThat(ds.getMaxWaitForConnection()).isEqualTo(Duration.seconds(30));
        assertThat(ds.getValidationQuery()).isEqualTo(Optional.of("/* Health Check */ SELECT 1"));
        assertThat(ds.getMinSize()).isEqualTo(10);
        assertThat(ds.getInitialSize()).isEqualTo(10);
        assertThat(ds.getMaxSize()).isEqualTo(100);
        assertThat(ds.getCheckConnectionWhileIdle()).isTrue();
        assertThat(ds.getEvictionInterval()).isEqualTo(Duration.seconds(5));
        assertThat(ds.getMinIdleTime()).isEqualTo(Duration.minutes(1));
        assertThat(ds.getValidationInterval()).isEqualTo(Duration.seconds(30));
        assertThat(ds.isAutoCommentsEnabled()).isTrue();
        assertThat(ds.getReadOnlyByDefault()).isNull();
        assertThat(ds.isRemoveAbandoned()).isFalse();
        assertThat(ds.getRemoveAbandonedTimeout()).isEqualTo(Duration.seconds(60L));
        assertThat(ds.getAbandonWhenPercentageFull()).isZero();
        assertThat(ds.isAlternateUsernamesAllowed()).isFalse();
        assertThat(ds.getCommitOnReturn()).isFalse();
        assertThat(ds.getRollbackOnReturn()).isFalse();
        assertThat(ds.getAutoCommitByDefault()).isNull();
        assertThat(ds.getDefaultCatalog()).isNull();
        assertThat(ds.getDefaultTransactionIsolation()).isEqualTo(DataSourceFactory.TransactionIsolation.DEFAULT);
        assertThat(ds.getUseFairQueue()).isTrue();
        assertThat(ds.getInitializationQuery()).isNull();
        assertThat(ds.getLogAbandonedConnections()).isFalse();
        assertThat(ds.getLogValidationErrors()).isFalse();
        assertThat(ds.getMaxConnectionAge()).isNotPresent();
        assertThat(ds.getCheckConnectionOnBorrow()).isFalse();
        assertThat(ds.getCheckConnectionOnConnect()).isTrue();
        assertThat(ds.getCheckConnectionOnReturn()).isFalse();
        assertThat(ds.getValidationQueryTimeout()).isNotPresent();
        assertThat(ds.isIgnoreExceptionOnPreLoad()).isFalse();
    }

    @Test
    void testInlineUserPasswordConfiguration() throws Exception {
        DataSourceFactory ds = getDataSourceFactory("yaml/inline_user_pass_db_pool.yml");

        assertThat(ds.getDriverClass()).isEqualTo("org.postgresql.Driver");
        assertThat(ds.getUrl()).isEqualTo("jdbc:postgresql://db.example.com/db-prod?user=scott&password=tiger");
        assertThat(ds.getUser()).isNull();
        assertThat(ds.getPassword()).isNull();
    }

    @Test
    void testInitialSizeZeroIsAllowed() throws Exception {
        assertThat(getDataSourceFactory("yaml/empty_initial_pool.yml").getInitialSize())
                .isZero();
    }

    @Test
    void testEmptyDriverClassIsAllowed() throws Exception {
        assertThat(getDataSourceFactory("yaml/empty_driver_class_db_pool.yml").getDriverClass())
                .isNull();
    }

    private DataSourceFactory getDataSourceFactory(String resourceName) throws Exception {
        return new YamlConfigurationFactory<>(
                        DataSourceFactory.class, Validators.newValidator(), Jackson.newObjectMapper(), "dw")
                .build(new ResourceConfigurationSourceProvider(), resourceName);
    }
}
