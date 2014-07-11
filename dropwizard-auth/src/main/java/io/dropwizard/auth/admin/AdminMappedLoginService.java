package io.dropwizard.auth.admin;

import org.eclipse.jetty.security.MappedLoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.security.Password;

import java.io.IOException;

public class AdminMappedLoginService extends MappedLoginService {

    public AdminMappedLoginService(final String userName, final String password, final String role) {
        putUser(userName, new Password(password), new String[]{role});
    }

    @Override
    public String getName() {
        return "Hello";
    }

    @Override
    protected UserIdentity loadUser(final String username) {
        return null;
    }

    @Override
    protected void loadUsers() throws IOException {
    }
}
