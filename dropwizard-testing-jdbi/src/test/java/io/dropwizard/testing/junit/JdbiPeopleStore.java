package io.dropwizard.testing.junit;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

/**
 * Test JDBI data access object.
 */
@RegisterMapper(PersonMapper.class)
public interface JdbiPeopleStore {

    @SqlQuery("SELECT * FROM People WHERE name = :name")
    Person fetchPerson(@Bind("name") String name);

    @SqlUpdate("INSERT INTO People (name, email) VALUES (:person.name, :person.email)")
    int addPerson(@BindBean("person") Person person);
}