package com.yammer.dropwizard.views;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

/**
 * A {@link Bundle} which enables the rendering of FreeMarker views by your service.
 *
 * <p>A view combines a Freemarker template with a set of Java objects:</p>
 *
 * <pre><code>
 * public class PersonView extends View {
 *     private final Person person;
 *
 *     public PersonView(Person person) {
 *         super("profile.ftl");
 *         this.person = person;
 *     }
 *
 *     public Person getPerson() {
 *         return person;
 *     }
 * }
 * </code></pre>
 *
 *<p>The {@code "profile.ftl"} is the path of the template relative to the class name. If this
 * class was {@code com.example.service.PersonView}, Freemarker would then look for the file
 * {@code src/main/resources/com/example/service/profile.ftl}. If the template path
 * starts with a slash (e.g., {@code "/hello.ftl"}), Freemarker will look for the file {@code
 * src/main/resources/hello.ftl}.
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
 * &lt;#-- @ftlvariable name="" type="com.example.service.PersonView" --&gt;
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
 */
public class ViewBundle implements Bundle {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // nothing doing
    }

    @Override
    public void run(Environment environment) {
        environment.addProvider(ViewMessageBodyWriter.class);
    }
}
