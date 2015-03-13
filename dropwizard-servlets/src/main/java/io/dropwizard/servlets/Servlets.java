package io.dropwizard.servlets;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility functions for dealing with servlets.
 */
public class Servlets {
    private Servlets() { /* singleton */ }

    /**
     * Returns the full URL of the given request.
     *
     * @param request    an HTTP servlet request
     * @return the full URL, including the query string
     */
    public static String getFullUrl(HttpServletRequest request) {

        if (request.getQueryString() == null) {
            return request.getRequestURI();
        }

        return request.getRequestURI() + "?" + request.getQueryString();
    }
}
