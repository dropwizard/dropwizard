package io.dropwizard.testing.junit;

import java.util.*;

class DAOTestHibernateConfiguration {

    String connectionUrl = "jdbc:h2:mem:" + UUID.randomUUID();
    String connectionUsername = "sa";
    String connectionPassword = "";
    String connectionDriverClass = "org.h2.Driver";
    String hbm2ddlAuto = "create";
    boolean showSql = false;
    boolean useSqlComments = false;
    Set<Class<?>> entityClasses = new LinkedHashSet<>();
    Map<String, String> properties = new HashMap<>();
}
