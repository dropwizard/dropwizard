package io.dropwizard.jdbi;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PersonDAO {
    @SqlQuery("SELECT name FROM people WHERE name = :name")
    String findByName(@Bind("name") Optional<String> name);

    @SqlQuery("SELECT name FROM people ORDER BY name ASC")
    List<String> findAllNames();

    @SqlQuery("SELECT DISTINCT name FROM people")
    Set<String> findAllUniqueNames();

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
