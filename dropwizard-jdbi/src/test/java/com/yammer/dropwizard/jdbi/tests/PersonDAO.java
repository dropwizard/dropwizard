package com.yammer.dropwizard.jdbi.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

public interface PersonDAO {
    @SqlQuery("SELECT name FROM people WHERE name = :name")
    public String findByName(@Bind("name") Optional<String> name);

    @SqlQuery("SELECT name FROM people ORDER BY name ASC")
    public ImmutableList<String> findAllNames();

    @SqlQuery("SELECT DISTINCT name FROM people")
    public ImmutableSet<String> findAllUniqueNames();

    @SqlQuery("SELECT name FROM people WHERE email = :email ")
    @SingleValueResult(String.class)
    public Optional<String> findByEmail(@Bind("email")String email);
}
