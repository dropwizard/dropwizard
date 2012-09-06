package com.yammer.dropwizard.views;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public interface ViewRenderer {
    boolean isRenderable(View view);

    void render(View view, Locale locale, OutputStream output) throws IOException, WebApplicationException;
}
