package com.yammer.dropwizard.db.tests;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface PersonDAO {
    @SqlQuery("SELECT name FROM people WHERE name = :name")
    public String findByName(@Bind("name") Optional<String> name);
}
