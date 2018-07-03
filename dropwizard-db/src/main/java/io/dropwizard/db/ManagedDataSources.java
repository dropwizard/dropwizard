package io.dropwizard.db;

import javax.validation.constraints.NotNull;

public class ManagedDataSources {
    @NotNull
    private final ManagedDataSource writeDataSource;

    @NotNull
    private final ManagedDataSource readDataSource;

    public ManagedDataSources(ManagedDataSource writeDataSource, ManagedDataSource readDataSource) {
        this.writeDataSource = writeDataSource;
        this.readDataSource = readDataSource;
    }

    public ManagedDataSource getWriteDataSource() {
        return writeDataSource;
    }

    public ManagedDataSource getReadDataSource() {
        return readDataSource;
    }

    public boolean hasSeparateReader() {
        return writeDataSource != readDataSource;
    }
}
