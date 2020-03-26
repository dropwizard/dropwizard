package io.dropwizard.validation.selfvalidating;

import com.fasterxml.classmate.AnnotationConfiguration;
import com.fasterxml.classmate.AnnotationInclusion;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * This class is the base validator for the <code>@SelfValidating</code> annotation. It
 * initiates the self validation process on an object, generating wrapping methods to call
 * the validation methods efficiently and then calls them.
 */
public class SelfValidatingValidator implements ConstraintValidator<SelfValidating, Object> {
    private static final Logger log = LoggerFactory.getLogger(SelfValidatingValidator.class);

    @SuppressWarnings("rawtypes")
    private final ConcurrentMap<Class<?>, List<ValidationCaller>> methodMap = new ConcurrentHashMap<>();
    private final AnnotationConfiguration annotationConfiguration = new AnnotationConfiguration.StdConfiguration(AnnotationInclusion.INCLUDE_AND_INHERIT_IF_INHERITED);
    private final TypeResolver typeResolver = new TypeResolver();
    private final MemberResolver memberResolver = new MemberResolver(typeResolver);
    private boolean escapeExpressions = true;

    @Override
    public void initialize(SelfValidating constraintAnnotation) {
        escapeExpressions = constraintAnnotation.escapeExpressions();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        final ViolationCollector collector = new ViolationCollector(context, escapeExpressions);
        context.disableDefaultConstraintViolation();
        for (ValidationCaller caller : methodMap.computeIfAbsent(value.getClass(), this::findMethods)) {
            caller.setValidationObject(value);
            caller.call(collector);
        }
        return !collector.hasViolationOccurred();
    }

    /**
     * This method generates <code>ValidationCaller</code>s for each method annotated
     * with <code>@SelfValidation</code> that adheres to required signature.
     */
    @SuppressWarnings({ "rawtypes" })
    private <T> List<ValidationCaller> findMethods(Class<T> annotated) {
        ResolvedTypeWithMembers annotatedType = memberResolver.resolve(typeResolver.resolve(annotated), annotationConfiguration, null);
        final List<ValidationCaller> callers = Arrays.stream(annotatedType.getMemberMethods())
            .filter(this::isValidationMethod)
            .filter(this::isMethodCorrect)
            .map(m -> new ProxyValidationCaller<>(annotated, m))
            .collect(Collectors.toList());
        if (callers.isEmpty()) {
            log.warn("The class {} is annotated with @SelfValidating but contains no valid methods that are annotated " +
                "with @SelfValidation", annotated);
        }
        return callers;
    }

    private boolean isValidationMethod(ResolvedMethod m) {
        return m.get(SelfValidation.class) != null;
    }

    boolean isMethodCorrect(ResolvedMethod m) {
        if (m.getReturnType()!=null) {
            log.error("The method {} is annotated with @SelfValidation but does not return void. It is ignored", m.getRawMember());
            return false;
        } else if (m.getArgumentCount() != 1 || !m.getArgumentType(0).getErasedType().equals(ViolationCollector.class)) {
            log.error("The method {} is annotated with @SelfValidation but does not have a single parameter of type {}",
                m.getRawMember(), ViolationCollector.class);
            return false;
        } else if (!m.isPublic()) {
            log.error("The method {} is annotated with @SelfValidation but is not public", m.getRawMember());
            return false;
        }
        return true;
    }

    final static class ProxyValidationCaller<T> extends ValidationCaller<T> {
        private final Class<T> cls;
        private final ResolvedMethod resolvedMethod;

        ProxyValidationCaller(Class<T> cls, ResolvedMethod resolvedMethod) {
            this.cls = cls;
            this.resolvedMethod = resolvedMethod;
        }

        @Override
        public void call(ViolationCollector vc) {
            final Method method = resolvedMethod.getRawMember();
            final T obj = cls.cast(getValidationObject());
            try {
                method.invoke(obj, vc);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Couldn't call " + resolvedMethod + " on " + getValidationObject(), e);
            }
        }
    }
}
