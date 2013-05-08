package com.codahale.dropwizard.servlets;

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
        final StringBuilder url = new StringBuilder(100).append(request.getRequestURI());
        if (request.getQueryString() != null) {
            url.append('?').append(request.getQueryString());
        }
        return url.toString();
    }
}
