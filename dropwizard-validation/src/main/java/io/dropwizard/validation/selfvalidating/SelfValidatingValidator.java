package io.dropwizard.validation.selfvalidating;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This class is the base validator for the <code>@SelfValidating</code> annotation. It
 * initiates the self validation process on an object, generating wrapping methods to call
 * the validation methods efficiently and then calls them.
 */
public class SelfValidatingValidator implements ConstraintValidator<SelfValidating, Object> {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final Logger log = LoggerFactory.getLogger(SelfValidatingValidator.class);

    private final ConcurrentMap<Class<?>, List<ValidationCaller<?>>> methodMap = Maps.newConcurrentMap();

    @Override
    public void initialize(SelfValidating constraintAnnotation) {
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        final ViolationCollector collector = new ViolationCollector(context);
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
    @SuppressWarnings("unchecked")
    private List<ValidationCaller<?>> findMethods(Class<?> annotated) {
        final ClassPool cp;
        final CtClass callerSuperclass;
        final CtClass[] callingParameters;
        try {
            cp = ClassPool.getDefault();
            callerSuperclass = cp.get(ValidationCaller.class.getName());
            callingParameters = new CtClass[]{cp.get(ViolationCollector.class.getName())};
        } catch (NotFoundException e) {
            throw new IllegalStateException("Failed to load included class", e);
        }

        final List<ValidationCaller<?>> callers = Arrays.stream(annotated.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(SelfValidation.class))
            .filter(this::isCorrectMethod)
            .map(m -> {
                try {
                    CtClass cc = cp.makeClass("ValidationCallerGeneratedImpl" + COUNTER.getAndIncrement());
                    cc.setSuperclass(callerSuperclass);

                    CtMethod cm = new CtMethod(CtClass.voidType, "call", callingParameters, cc);
                    cc.addMethod(cm);
                    cm.setBody("{ return ((" + annotated.getName() + ")getValidationObject())." + m.getName() + "($1); }");

                    cc.setModifiers(Modifier.PUBLIC);
                    return (ValidationCaller<?>) cc.toClass().getConstructor().newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to generate ValidationCaller for method " + m, e);
                }
            }).collect(Collectors.toList());
        if (callers.isEmpty()) {
            log.error("The class {} is annotated with @SelfValidating but contains no valid methods that are annotated " +
                "with @SelfValidation", annotated);
        }
        return callers;
    }

    @VisibleForTesting
    boolean isCorrectMethod(Method m) {
        if (!void.class.equals(m.getReturnType())) {
            log.error("The method {} is annotated with @SelfValidation but does not return void. It is ignored", m);
            return false;
        } else if (m.getParameterTypes().length != 1 || !m.getParameterTypes()[0].equals(ViolationCollector.class)) {
            log.error("The method {} is annotated with @SelfValidation but does not have a single parameter of type {}",
                m, ViolationCollector.class);
            return false;
        } else if (!Modifier.isPublic(m.getModifiers())) {
            log.error("The method {} is annotated with @SelfValidation but is not public", m);
            return false;
        }
        return true;
    }
}
