package io.dropwizard.auth.admin;

import org.eclipse.jetty.util.security.Password;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AdminMappedLoginServiceTest {

    @Test
    public void testSuccessfulLogin() throws IOException, ServletException {
        assertNotNull(new AdminMappedLoginService("user", "pass", "role").login("user", new Password("pass")));
    }

    @Test
    public void testFailedLogin() throws IOException, ServletException {
        assertNull(new AdminMappedLoginService("user", "pass", "role").login("user", new Password("pass2")));
    }
}
