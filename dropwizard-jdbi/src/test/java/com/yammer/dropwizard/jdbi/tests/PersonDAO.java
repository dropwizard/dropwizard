package com.yammer.dropwizard.jdbi.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface PersonDAO {
    @SqlQuery("SELECT name FROM people WHERE name = :name")
    public String findByName(@Bind("name") Optional<String> name);

    @SqlQuery("SELECT name FROM people ORDER BY name ASC")
    public ImmutableList<String> findAllNames();
}
