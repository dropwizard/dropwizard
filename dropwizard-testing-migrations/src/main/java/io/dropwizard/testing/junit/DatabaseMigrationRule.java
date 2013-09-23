package io.dropwizard.testing.junit;

import com.google.common.base.Strings;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.rules.ExternalResource;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseMigrationRule extends ExternalResource {

    private static final String DEFAULT_CHANGELOG_FILENAME = "migrations.xml";
    private final String url;
    private final String username;
    private final String password;
    private final String changeLogFile;
    private Liquibase liquibase;

    public DatabaseMigrationRule(String url, String username, String password) {
        this(url, username, password, null);
    }

    public DatabaseMigrationRule(String url, String username, String password, String changeLogFile) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.changeLogFile = Strings.isNullOrEmpty(changeLogFile) ? DEFAULT_CHANGELOG_FILENAME : changeLogFile;
    }

    @Override
    protected void before() throws Throwable {
        Connection connection = DriverManager.getConnection(url, username, password);
        liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
        liquibase.update(null);
    }

    @Override
    protected void after() {
        try {
            liquibase.dropAll();
        } catch (DatabaseException | LockException e) {
            // Wrap checked exceptions and rethrow to fail fast and not cause unexpected results in the next test.
            throw new RuntimeException(e);
        }
    }
}
