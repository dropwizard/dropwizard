package com.example.helloworld.db;

import com.example.helloworld.core.Person;
import com.google.common.collect.ImmutableList;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.tweak.BeanMapperFactory;

@UseStringTemplate3StatementLocator
@RegisterMapperFactory(BeanMapperFactory.class)
public interface PeopleDAO {
    @SqlQuery
    Person findById(@Bind("id") Long id);

    @SqlUpdate
    @GetGeneratedKeys
    long create(@BindBean Person person);

    @SqlQuery
    ImmutableList<Person> findAll();

}
