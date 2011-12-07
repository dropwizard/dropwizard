package com.yammer.dropwizard.bundles;

import com.sun.jersey.freemarker.FreemarkerViewProcessor;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;

/**
 * A {@link Bundle} which enables the rendering of FreeMarker templates by your service.
 *
 * <p>A resource method with a template would looks something like this:</p>
 *
 * <pre><code>
 * \@GET
 * public Viewable getPerson(\@PathParam("id") String id) {
 *     final Person person = dao.find(id);
 *     return new Viewable("index.ftl", person);
 * }
 * </code></pre>
 *
 * <p>The {@code "index.ftl"} is the path of the template relative to the class name. If this
 * class was {@code com.example.service.PersonResource}, Jersey would then look for the file
 * {@code src/main/resources/com/example/service/PersonResource/index.ftl}, which might look
 * something like this:</p>
 *
 * <pre>{@code
 * <html>
 *     <body>
 *         <h1>Hello, ${name?html}!</h1>
 *     </body>
 * </html>
 * }</pre>
 *
 * <p>In this template, {@code ${name}} calls {@code Person#getName()}, and the {@code ?html}
 * escapes all HTML control characters in the result.</p>
 *
 * @see <a href="http://freemarker.sourceforge.net/docs/index.html">FreeMarker Manual</a>
 */
public class TemplateBundle implements Bundle {
    private final String templatePath;

    /**
     * Creates a new {@link TemplateBundle} with no specified template path.
     */
    public TemplateBundle() {
        this(null);
    }

    /**
     * Creates a new {@link TemplateBundle} with the specified template path.
     *
     * @param templatePath    the location, in the class path, of the FreeMarker templates
     */
    public TemplateBundle(String templatePath) {
        this.templatePath = templatePath;
    }

    @Override
    public void initialize(Environment environment) {
        environment.setJerseyProperty(FreemarkerViewProcessor.FREEMARKER_TEMPLATES_BASE_PATH, templatePath);
        environment.addProvider(FreemarkerViewProcessor.class);
    }
}
