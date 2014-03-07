package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import io.dropwizard.validation.ConstraintViolations;
import io.dropwizard.validation.Validated;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

/**
 * A Jersey provider which enables using Jackson to parse request entities into objects and generate
 * response entities from objects. Any request entity method parameters annotated with
 * {@code @Valid} are validated, and an informative 422 Unprocessable Entity response is returned
 * should the entity be invalid.
 * <p/>
 * (Essentially, extends {@link JacksonJaxbJsonProvider} with validation and support for
 * {@link JsonIgnoreType}.)
 */
public class JacksonMessageBodyProvider extends JacksonJaxbJsonProvider {
    /**
     * The default group array used in case any of the validate methods is called without a group.
     */
    private static final Class<?>[] DEFAULT_GROUP_ARRAY = new Class<?>[]{ Default.class };
    private final ObjectMapper mapper;
    private final Validator validator;

    public JacksonMessageBodyProvider(ObjectMapper mapper, Validator validator) {
        this.validator = validator;
        this.mapper = mapper;
        setMapper(mapper);
    }

    @Override
    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return isProvidable(type) && super.isReadable(type, genericType, annotations, mediaType);
    }

    @Override
    public Object readFrom(Class<Object> type,
                           Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException {
        return validate(annotations, super.readFrom(type,
                                                    genericType,
                                                    annotations,
                                                    mediaType,
                                                    httpHeaders,
                                                    entityStream));
    }

    private Object validate(Annotation[] annotations, Object value) {
        if(null == value) {
            throw new ConstraintViolationException("The request entity was empty",
                    Collections.<ConstraintViolation<Object>>emptySet());
        }

        final Class<?>[] classes = findValidationGroups(annotations);

        if (classes != null) {
            Set<ConstraintViolation<Object>> violations = null;

            if(value instanceof Map) {
                violations = validate(((Map)value).values(), classes);
            } else if(value instanceof Iterable) {
                violations = validate((Iterable)value, classes);
            } else if(value.getClass().isArray()) {
                violations = new HashSet<>();

                Object[] values = (Object[]) value;
                for(Object item : values) {
                    violations.addAll(validator.validate(item, classes));
                }
            } else {
                violations = validator.validate(value, classes);
            }

            if (violations != null && !violations.isEmpty()) {
                throw new ConstraintViolationException("The request entity had the following errors:",
                        ConstraintViolations.copyOf(violations));
            }
        }

        return value;
    }

    private Set<ConstraintViolation<Object>> validate(Iterable values, Class<?>[] classes) {
        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        for(Object value : values) {
            violations.addAll(validator.validate(value, classes));
        }

        return violations;
    }

    private Class<?>[] findValidationGroups(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == Valid.class) {
                return DEFAULT_GROUP_ARRAY;
            } else if (annotation.annotationType() == Validated.class) {
                return  ((Validated) annotation).value();
            }
        }
        return null;
    }

    @Override
    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        return isProvidable(type) && super.isWriteable(type, genericType, annotations, mediaType);
    }

    private boolean isProvidable(Class<?> type) {
        final JsonIgnoreType ignore = type.getAnnotation(JsonIgnoreType.class);
        return (ignore == null) || !ignore.value();
    }

    public ObjectMapper getObjectMapper() {
        return mapper;
    }
}
