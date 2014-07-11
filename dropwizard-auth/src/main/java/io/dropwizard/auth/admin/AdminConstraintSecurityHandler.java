package io.dropwizard.auth.admin;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;

public class AdminConstraintSecurityHandler extends ConstraintSecurityHandler {
    private static final String ADMIN_ROLE = "admin";

    public AdminConstraintSecurityHandler(final String userName, final String password) {
        final Constraint constraint = new Constraint(Constraint.__BASIC_AUTH, ADMIN_ROLE);
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[]{ADMIN_ROLE});
        final ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");
        setAuthenticator(new BasicAuthenticator());
        addConstraintMapping(cm);
        setLoginService(new AdminMappedLoginService(userName, password, ADMIN_ROLE));
    }
}
