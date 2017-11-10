package io.dropwizard.validation.selfvalidating;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * This class is the base validator for the <code>@SelfValidating</code> annotation. It
 * initiates the self validation process on an object, generating wrapping methods to call
 * the validation methods efficiently and then calls them.
 */
public class SelfValidatingValidator implements ConstraintValidator<SelfValidating, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(SelfValidatingValidator.class);
    private static final AtomicInteger COUNTER = new AtomicInteger();
    
    private final HashMap<Class<?>, List<ValidationCaller<?>>> methodMap = new HashMap<>();
    
    @Override
    public void initialize(SelfValidating constraintAnnotation) {}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        List<ValidationCaller<?>> callers = findMethods(value.getClass());

        ViolationCollector collector = new ViolationCollector(context);
        context.disableDefaultConstraintViolation();
        for (ValidationCaller caller:callers) {
            caller.setValidationObject(value);
            caller.call(collector);
        }
        return !collector.hasViolationOccurred();
    }

    /**
     * This method generates and caches <code>ValidationCaller</code>s for each method annotated 
     * with <code>@SelfValidation</code> that adheres to required signature.
     * @param annotated
     * @return
     */
    private synchronized List<ValidationCaller<?>> findMethods(Class<? extends Object> annotated) {
        if (methodMap.containsKey(annotated))
            return methodMap.get(annotated);
    
        synchronized (methodMap) {
            if (methodMap.containsKey(annotated))
                return methodMap.get(annotated);
            
            List<ValidationCaller<?>> l = new ArrayList<>();
            
            ClassPool cp;
            CtClass callerSuperclass;
            CtClass[] callingParameters;
            try {
                cp = ClassPool.getDefault();
                callerSuperclass = cp.get(ValidationCaller.class.getName());
                callingParameters = new CtClass[] {cp.get(ViolationCollector.class.getName())};
            } catch (NotFoundException e) {
                throw new IllegalStateException("Failed to load included class", e);
            }
                
            for (Method m:annotated.getMethods()) {
                if (m.isAnnotationPresent(SelfValidation.class)) {
                    if (!void.class.equals(m.getReturnType()))
                        LOG.error("The method {} is annotated with SelfValidation but does not return void. It is ignored.", m);
                    else if (m.getParameterTypes().length != 1 || !m.getParameterTypes()[0].equals(ViolationCollector.class))
                        LOG.error("The method {} is annotated with SelfValidation but does not have a single parameter of type {}", m, ViolationCollector.class);
                    else if ((m.getModifiers() & Modifier.PUBLIC) == 0)
                        LOG.error("The method {} is annotated with SelfValidation but is not public", m);
                    else {
                        try {
                            CtClass cc = cp.makeClass("ValidationCallerGeneratedImpl" + COUNTER.getAndIncrement());
                            cc.setSuperclass(callerSuperclass);
             
                            CtMethod method = new CtMethod(CtClass.voidType, "call", callingParameters, cc);
                            cc.addMethod(method);
                            method.setBody("{ return ((" + annotated.getName() + ")getValidationObject())." + m.getName() + "($1); }");
                            
                            cc.setModifiers(Modifier.PUBLIC);
                            @SuppressWarnings("unchecked")
                            ValidationCaller<?> caller = (ValidationCaller<?>) cc.toClass().getConstructor().newInstance();
                            l.add(caller);
                        } catch (Exception e) {
                            LOG.error("Failed to generate ValidationCaller for method " + m.toString(), e);
                        }
                    }
                }
            }
            if (l.isEmpty())
                LOG.error("The class {} is annotated with SelfValidating but contains no valid methods that are annotated with SelfValidation", annotated);
                
            methodMap.put(annotated, l);
            return l;
        }
        
        
    }

}
