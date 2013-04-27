package com.yammer.dropwizard.views.flashscope;

import com.google.common.base.Predicate;
import com.sun.jersey.api.client.ClientResponse;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import javax.ws.rs.core.NewCookie;

import static com.google.common.collect.Iterables.find;
import static org.junit.Assert.assertNotNull;

public class TestUtils {
    public static NewCookie flashCookieIn(ClientResponse response) {
        return find(response.getCookies(), new Predicate<NewCookie>() {
            public boolean apply(NewCookie newCookie) {
                return newCookie.getName().equals(FlashScope.COOKIE_NAME);
            }
        });
    }

    public static Matcher<Iterable<? super NewCookie>> hasCookieWithName(String name) {
        return CoreMatchers.hasItem(withName(name));
    }

    public static Matcher<NewCookie> withName(final String cookieName) {
        return new TypeSafeDiagnosingMatcher<NewCookie>() {
            @Override
            protected boolean matchesSafely(NewCookie newCookie, Description description) {
                if (!newCookie.getName().equals(cookieName)) {
                    description.appendText("cookie name is not " + cookieName);
                    return false;
                }

                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a cookie with name " + cookieName);
            }
        };
    }

    public static NewCookie findCookie(ClientResponse response, final String name) {
        NewCookie cookie = find(response.getCookies(), new Predicate<NewCookie>() {
            public boolean apply(NewCookie newCookie) {
                return newCookie.getName().equals(name);
            }
        }, null);

        assertNotNull("No cookie with name " + name + " found in response", cookie);
        return cookie;
    }
}
