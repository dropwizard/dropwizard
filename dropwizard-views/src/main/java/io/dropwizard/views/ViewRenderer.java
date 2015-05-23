package io.dropwizard.views;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

/**
 * The rendering engine for a type of view.
 */
public interface ViewRenderer {
    /**
     * Returns {@code true} if the renderer can render the given {@link View}.
     *
     * @param view a view
     * @return {@code true} if {@code view} can be rendered
     */
    boolean isRenderable(View view);

    /**
     * Renders the given {@link View} for the given {@link Locale} to the given {@link
     * OutputStream}.
     *
     * @param view   a view
     * @param locale the locale in which the view should be rendered
     * @param output the output stream
     * @throws IOException             if there is an error writing to {@code output}
     * @throws WebApplicationException if there is an error rendering the template
     */
    void render(View view,
                Locale locale,
                OutputStream output) throws IOException;

    /**
      * options for configuring the view renderer
      * @param options
     */
    void configure(Map<String, String> options);

    /**
     * @return the suffix of the template type, e.g '.ftl', '.mustache'
     */
    String getSuffix();
}
