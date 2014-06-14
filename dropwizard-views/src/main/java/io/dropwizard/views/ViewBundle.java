package io.dropwizard.views;

import com.sun.jersey.spi.service.ServiceFinder;
import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * A {@link Bundle}, which by default, enables the rendering of FreeMarker & Mustache views by your application.
 *
 * <p>Other instances of {@link ViewRenderer} can be used by initializing your {@link ViewBundle} with a
 * {@link Iterable} of the {@link ViewRenderer} instances to be used when configuring your {@link Bundle}:</p>
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
 *<p>The {@code "profile.ftl"} or {@code "profile.mustache"} is the path of the template relative to the class name. If
 * this class was {@code com.example.application.PersonView}, Freemarker or Mustache would then look for the file
 * {@code src/main/resources/com/example/application/profile.ftl} or {@code
 * src/main/resources/com/example/application/profile.mustache} respectively. If the template path
 * starts with a slash (e.g., {@code "/hello.ftl"} or {@code "/hello.mustache"}), Freemarker or Mustache will look for
 * the file {@code src/main/resources/hello.ftl} or {@code src/main/resources/hello.mustache} respectively.
 *
 * <p>A resource method with a view would looks something like this:</p>
 *
 * <pre><code>
 * \@GET
 * public PersonView getPerson(\@PathParam("id") String id) {
 *     return new PersonView(dao.find(id));
 * }
 * </code></pre>
 *
 * <p>Freemarker templates look something like this:</p>
 *
 * <pre>{@code
 * &lt;#-- @ftlvariable name="" type="com.example.application.PersonView" --&gt;
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
 * allowing for better typesafety in your templates.</p>
 *
 * @see <a href="http://freemarker.sourceforge.net/docs/index.html">FreeMarker Manual</a>
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
 * @see <a href="http://mustache.github.io/mustache.5.html">Mustache Manual</a>
 */
public class ViewBundle implements Bundle {
    private final Iterable<ViewRenderer> viewRenderers;

    public ViewBundle() {
        this(ServiceFinder.find(ViewRenderer.class));
    }

    public ViewBundle(Iterable<ViewRenderer> viewRenderers) {
        this.viewRenderers = viewRenderers;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // nothing doing
    }

    @Override
    public void run(Environment environment) {
        environment.jersey().register(new ViewMessageBodyWriter(environment.metrics(), viewRenderers));
    }
}
