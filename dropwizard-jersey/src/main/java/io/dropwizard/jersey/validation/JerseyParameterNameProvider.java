package io.dropwizard.jersey.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import org.hibernate.validator.parameternameprovider.ReflectionParameterNameProvider;

/**
 * Adds jersey support to parameter name discovery in hibernate validator.
 *
 * <p>This provider will behave like the hibernate-provided {@link ReflectionParameterNameProvider} except when a
 * method parameter is annotated with a jersey parameter annotation, like {@link QueryParam}. If a jersey parameter
 * annotation is present the value of the annotation is used as the parameter name.</p>
 */
public class JerseyParameterNameProvider extends ReflectionParameterNameProvider {

    @Override
    public List<String> getParameterNames(Method method) {
        Parameter[] parameters = method.getParameters();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        List<String> names = new ArrayList<>(parameterAnnotations.length);
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            String name = getParameterNameFromAnnotations(annotations).orElse(parameters[i].getName());
            names.add(name);
        }
        return names;
    }

    /**
     * Derives member's name and type from it's annotations
     */
    public static Optional<String> getParameterNameFromAnnotations(Annotation[] memberAnnotations) {
        for (Annotation a : memberAnnotations) {
            if (a instanceof QueryParam) {
                return Optional.of("query param " + ((QueryParam) a).value());
            } else if (a instanceof PathParam) {
                return Optional.of("path param " + ((PathParam) a).value());
            } else if (a instanceof HeaderParam) {
                return Optional.of("header " + ((HeaderParam) a).value());
            } else if (a instanceof CookieParam) {
                return Optional.of("cookie " + ((CookieParam) a).value());
            } else if (a instanceof FormParam) {
                return Optional.of("form field " + ((FormParam) a).value());
            } else if (a instanceof Context) {
                return Optional.of("context");
            } else if (a instanceof MatrixParam) {
                return Optional.of("matrix param " + ((MatrixParam) a).value());
            }
        }

        return Optional.empty();
    }
}
