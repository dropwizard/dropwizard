package io.dropwizard.views;

import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Environment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;


/**
 * A {@link ConfiguredBundle}, which by default, enables the rendering of FreeMarker & Mustache views by your application.
 *
 * <p>Other instances of {@link ViewRenderer} can be used by initializing your {@link ViewBundle} with a
 * {@link Iterable} of the {@link ViewRenderer} instances to be used when configuring your {@link ConfiguredBundle}:</p>
 *
 * <pre><code>
 * new ViewBundle(ImmutableList.of(myViewRenderer))
 * </code></pre>
 *
 * <p>A view combines a Freemarker or Mustache template with a set of Java objects:</p>
 *
 * <pre><code>
 * public class PersonView extends View {
 *     private final Person person;
 *
 *     public PersonView(Person person) {
 *         super("profile.ftl"); // or super("profile.mustache"); for a Mustache template
 *         this.person = person;
 *     }
 *
 *     public Person getPerson() {
 *         return person;
 *     }
 * }
 * </code></pre>
 *
 * <p>The {@code "profile.ftl[hx]"} or {@code "profile.mustache"} is the path of the template relative to the class name. If
 * this class was {@code com.example.application.PersonView}, Freemarker or Mustache would then look for the file
 * {@code src/main/resources/com/example/application/profile.ftl} or {@code
 * src/main/resources/com/example/application/profile.mustache} respectively. If the template path
 * starts with a slash (e.g., {@code "/hello.ftl"} or {@code "/hello.mustache"}), Freemarker or Mustache will look for
 * the file {@code src/main/resources/hello.ftl} or {@code src/main/resources/hello.mustache} respectively.
 *
 * <p>A resource method with a view would looks something like this:</p>
 *
 * <pre>
 * &#64;GET
 * public PersonView getPerson(@PathParam("id") String id) {
 *     return new PersonView(dao.find(id));
 * }
 * </pre>
 *
 * <p>Freemarker templates look something like this:</p>
 *
 * <pre>{@code
 * <#-- @ftlvariable name="" type="com.example.application.PersonView" -->
 * <html>
 *     <body>
 *         <h1>Hello, ${person.name?html}!</h1>
 *     </body>
 * </html>
 * }</pre>
 *
 * <p>In this template, {@code ${person.name}} calls {@code getPerson().getName()}, and the
 * {@code ?html} escapes all HTML control characters in the result. The {@code ftlvariable} comment
 * at the top indicate to Freemarker (and your IDE) that the root object is a {@code Person},
 * allowing for better type-safety in your templates.</p>
 *
 * See Also: <a href="http://freemarker.sourceforge.net/docs/index.html">FreeMarker Manual</a>
 *
 * <p>Mustache templates look something like this:</p>
 *
 * <pre>{@code
 * <html>
 *     <body>
 *         <h1>Hello, {{person.name}}!</h1>
 *     </body>
 * </html>
 * }</pre>
 *
 * <p>In this template, {@code {{person.name}}} calls {@code getPerson().getName()}.</p>
 *
 * See Also: <a href="http://mustache.github.io/mustache.5.html">Mustache Manual</a>
 */
public class ViewBundle<T> implements ConfiguredBundle<T>, ViewConfigurable<T> {
    private final Iterable<ViewRenderer> viewRenderers;

    public ViewBundle() {
        this(ServiceLoader.load(ViewRenderer.class));
    }

    public ViewBundle(Iterable<ViewRenderer> viewRenderers) {
        final Set<ViewRenderer> viewRendererSet = new HashSet<>();

        viewRenderers.forEach(viewRendererSet::add);

        this.viewRenderers = Collections.unmodifiableSet(viewRendererSet);
    }

    @Override
    public Map<String, Map<String, String>> getViewConfiguration(T configuration) {
        return Map.of();
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        final Map<String, Map<String, String>> options = getViewConfiguration(configuration);
        for (ViewRenderer viewRenderer : viewRenderers) {
            final Map<String, String> viewOptions = options.get(viewRenderer.getConfigurationKey());
            viewRenderer.configure(viewOptions == null ? Map.of() : viewOptions);
        }
        environment.jersey().register(new ViewMessageBodyWriter(environment.metrics(), viewRenderers));
    }
}
