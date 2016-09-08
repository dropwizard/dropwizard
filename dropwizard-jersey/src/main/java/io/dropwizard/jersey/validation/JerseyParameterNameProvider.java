package io.dropwizard.jersey.validation;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;

import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JerseyParameterNameProvider extends DefaultParameterNameProvider {

    @Override
    public List<String> getParameterNames(Constructor<?> constructor) {
        return super.getParameterNames(constructor);
    }

    @Override
    public List<String> getParameterNames(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        List<String> names = new ArrayList<>( parameterAnnotations.length );
        for (Annotation[] annotations : parameterAnnotations) {
            String name = getParameterName(annotations);
            if ( name == null ) {
                name = "arg" + (names.size() + 1);
            }
            names.add( name );
        }
        return names;
    }

    protected String getParameterName(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == QueryParam.class) {
                return ((QueryParam)annotation).value();
            }
            else if (annotation.annotationType() == FormParam.class) {
                return ((FormParam)annotation).value();
            }
            else if (annotation.annotationType() == PathParam.class) {
                return ((PathParam)annotation).value();
            }
        }
        return null;
    }

}
