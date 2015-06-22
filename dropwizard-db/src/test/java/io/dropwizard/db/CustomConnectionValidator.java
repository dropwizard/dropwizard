package io.dropwizard.db;

import org.apache.tomcat.jdbc.pool.Validator;

import java.sql.Connection;

public class CustomConnectionValidator implements Validator {

    // It's used only once, so static access should be fine
    static volatile boolean loaded;

    @Override
    public boolean validate(Connection connection, int validateAction) {
        loaded = true;
        return true;
    }
}
