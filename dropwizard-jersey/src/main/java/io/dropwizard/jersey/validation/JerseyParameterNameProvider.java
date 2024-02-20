package io.dropwizard.jersey.validation;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import org.hibernate.validator.parameternameprovider.ReflectionParameterNameProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            if (a instanceof QueryParam queryParam) {
                return Optional.of("query param " + queryParam.value());
            } else if (a instanceof PathParam pathParam) {
                return Optional.of("path param " + pathParam.value());
            } else if (a instanceof HeaderParam headerParam) {
                return Optional.of("header " + headerParam.value());
            } else if (a instanceof CookieParam cookieParam) {
                return Optional.of("cookie " + cookieParam.value());
            } else if (a instanceof FormParam formParam) {
                return Optional.of("form field " + formParam.value());
            } else if (a instanceof Context) {
                return Optional.of("context");
            } else if (a instanceof MatrixParam matrixParam) {
                return Optional.of("matrix param " + matrixParam.value());
            }
        }

        return Optional.empty();
    }

}
