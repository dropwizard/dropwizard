package com.yammer.dropwizard.db.migrations;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.util.Generics;

public abstract class MigrationsBundle<T extends Configuration> extends Bundle implements ConfigurationStrategy<T> {
    /*
Maintenance Commands
 calculateCheckSum <id>    Calculates and prints a checksum for the changeset
                           with the given id in the format filepath::id::author.
 clearCheckSums            Removes all saved checksums from database log.
                           Useful for 'MD5Sum Check Failed' errors
 changelogSync             Mark all changes as executed in the database
 changelogSyncSQL          Writes SQL to mark all changes as executed
                           in the database to STDOUT
 markNextChangeSetRan      Mark the next change changes as executed
                           in the database
 markNextChangeSetRanSQL   Writes SQL to mark the next change
                           as executed in the database to STDOUT
 listLocks                 Lists who currently has locks on the
                           database changelog
 releaseLocks              Releases all locks on the database changelog
     */

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(Bootstrap<?> bootstrap) {
        final Class<T> configClass = (Class<T>) Generics.getTypeParameter(getClass());
        bootstrap.addCommand(new DbCommand<T>(this, configClass));
    }
}
