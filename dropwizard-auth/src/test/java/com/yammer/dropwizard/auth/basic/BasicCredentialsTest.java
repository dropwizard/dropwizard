package com.yammer.dropwizard.auth.basic;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BasicCredentialsTest {
    private final BasicCredentials credentials = new BasicCredentials("u", "p");

    @Test
    public void hasAUsername() throws Exception {
        assertThat(credentials.getUsername(),
                   is("u"));
    }

    @Test
    public void hasAPassword() throws Exception {
        assertThat(credentials.getPassword(),
                   is("p"));
    }

    @Test
    public void hasAWorkingEqualsMethod() throws Exception {
        assertThat(credentials,
                   is(equalTo(credentials)));

        assertThat(credentials,
                   is(equalTo(new BasicCredentials("u", "p"))));

        assertThat(credentials,
                   is(not(equalTo(null))));

        assertThat(credentials,
                   is(not(equalTo((Object) "string"))));

        assertThat(credentials,
                   is(not(equalTo(new BasicCredentials("u1", "p")))));

        assertThat(credentials,
                   is(not(equalTo(new BasicCredentials("u", "p1")))));
    }

    @Test
    public void hasAWorkingHashCode() throws Exception {
        assertThat(credentials.hashCode(),
                   is(new BasicCredentials("u", "p").hashCode()));

        assertThat(credentials.hashCode(),
                   is(not(new BasicCredentials("u1", "p").hashCode())));

        assertThat(credentials.hashCode(),
                   is(not(new BasicCredentials("u", "p1").hashCode())));
    }

    @Test
    public void isHumanReadable() throws Exception {
        assertThat(credentials.toString(),
                   is("BasicCredentials{username=u, password=**********}"));
    }
}
