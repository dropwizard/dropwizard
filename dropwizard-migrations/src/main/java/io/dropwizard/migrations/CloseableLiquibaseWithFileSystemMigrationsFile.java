package io.dropwizard.migrations;

import io.dropwizard.db.ManagedDataSource;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CloseableLiquibaseWithFileSystemMigrationsFile extends CloseableLiquibase implements AutoCloseable {
    CloseableLiquibaseWithFileSystemMigrationsFile(
        ManagedDataSource dataSource,
        Database database,
        String file
    ) throws LiquibaseException, SQLException {
        this(dataSource,
            database,
            file,
            FileSystems.getDefault());
    }

    CloseableLiquibaseWithFileSystemMigrationsFile(
        ManagedDataSource dataSource,
        Database database,
        String file,
        FileSystem fileSystem
    ) throws LiquibaseException, SQLException {
        super(file,
            new CompositeResourceAccessor(directoryResourceAccessors(fileSystem)),
            database,
            dataSource);
    }

    private static Collection<ResourceAccessor> directoryResourceAccessors(FileSystem fileSystem) {
        return StreamSupport.stream(fileSystem.getRootDirectories().spliterator(), false)
                .map(path -> {
                    try {
                        return new DirectoryResourceAccessor(path);
                    } catch (IOException e) {
                        return null;
                    }
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public CloseableLiquibaseWithFileSystemMigrationsFile(
        ManagedDataSource dataSource,
        String file
    ) throws LiquibaseException, SQLException {
        this(dataSource,
            DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection())),
            file);
    }
}
