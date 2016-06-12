package io.dropwizard.testing.junit;

import java.util.LinkedHashSet;
import java.util.Set;

class DAOTestHibernateConfiguration {

    String connectionUrl;
    String connectionUsername;
    String connectionDriverClass;
    String currentSessionContextClass;
    String hbm2ddlAuto;
    String showSql;

    Set<Class<?>> entityClasses;

    DAOTestHibernateConfiguration() {
        connectionUrl = "jdbc:h2:mem:";
        connectionUsername = "test";
        connectionDriverClass = "org.h2.Driver";
        currentSessionContextClass = "managed";
        hbm2ddlAuto = "create";
        showSql = "false";

        entityClasses = new LinkedHashSet<>();
    }
}
