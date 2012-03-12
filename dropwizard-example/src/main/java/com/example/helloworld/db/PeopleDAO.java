package com.example.helloworld.db;

import com.example.helloworld.core.Person;
import com.google.common.collect.ImmutableList;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import org.skife.jdbi.v2.sqlobject.stringtemplate.ExternalizedSqlViaStringTemplate3;
import org.skife.jdbi.v2.tweak.BeanMapperFactory;

@ExternalizedSqlViaStringTemplate3
@RegisterMapperFactory(BeanMapperFactory.class)
public interface PeopleDAO {

    @SqlUpdate
    void createPeopleTable();

    @SqlQuery
    Person findById(@Bind("id") Long id);

    @SqlUpdate
    @GetGeneratedKeys
    long create(@BindBean Person person);

    @SqlQuery
    ImmutableList<Person> findAll();

}
