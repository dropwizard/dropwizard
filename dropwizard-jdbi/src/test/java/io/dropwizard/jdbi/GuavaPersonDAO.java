package io.dropwizard.jdbi;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

public interface GuavaPersonDAO {
    @SqlQuery("SELECT name FROM people WHERE name = :name")
    String findByName(@Bind("name") Optional<String> name);

    @SqlQuery("SELECT name FROM people ORDER BY name ASC")
    ImmutableList<String> findAllNames();

    @SqlQuery("SELECT DISTINCT name FROM people")
    ImmutableSet<String> findAllUniqueNames();

    @SqlQuery("SELECT name FROM people WHERE email = :email ")
    @SingleValueResult(String.class)
    Optional<String> findByEmail(@Bind("email") String email);

    @SqlQuery("SELECT created_at FROM people WHERE created_at > :from ORDER BY created_at DESC LIMIT 1")
    DateTime getLatestCreatedAt(@Bind("from") DateTime from);

    @SqlQuery("SELECT created_at FROM people WHERE name = :name")
    @SingleValueResult(DateTime.class)
    Optional<DateTime> getCreatedAtByName(@Bind("name") String name);

    @SqlQuery("SELECT created_at FROM people WHERE email = :email")
    DateTime getCreatedAtByEmail(@Bind("email") String email);
}
