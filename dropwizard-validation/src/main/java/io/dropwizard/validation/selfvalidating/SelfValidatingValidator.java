package io.dropwizard.validation.selfvalidating;

import com.fasterxml.classmate.AnnotationConfiguration;
import com.fasterxml.classmate.AnnotationInclusion;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.RawMethod;
import com.fasterxml.classmate.members.ResolvedMethod;
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
import java.util.function.IntPredicate;
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
    private final AnnotationConfiguration annotationConfiguration = new AnnotationConfiguration.StdConfiguration(AnnotationInclusion.INCLUDE_AND_INHERIT_IF_INHERITED);
    private final TypeResolver typeResolver = new TypeResolver();
    private final MemberResolver memberResolver = new MemberResolver(typeResolver);

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

        ResolvedTypeWithMembers annotatedType = memberResolver.resolve(typeResolver.resolve(annotated), annotationConfiguration, null);
        final List<ValidationCaller<?>> callers = Arrays.stream(annotatedType.getMemberMethods())
            .filter(this::isValidationMethod)
            .filter(this::isMethodCorrect)
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
            log.warn("The class {} is annotated with @SelfValidating but contains no valid methods that are annotated " +
                "with @SelfValidation", annotated);
        }
        return callers;
    }

    private boolean isValidationMethod(ResolvedMethod m) {
        return m.get(SelfValidation.class) != null;
    }
    
    @VisibleForTesting
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
}
