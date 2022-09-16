package io.dropwizard.hibernate;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.hibernate.SessionFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A factory for creating proxies for components that use Hibernate data access objects
 * outside Jersey resources.
 * <p>A created proxy will be aware of the {@link UnitOfWork} annotation
 * on the original class methods and will open a Hibernate session with a transaction
 * around them.</p>
 */
public class UnitOfWorkAwareProxyFactory {

    private final Map<String, SessionFactory> sessionFactories;

    public UnitOfWorkAwareProxyFactory(String name, SessionFactory sessionFactory) {
        sessionFactories = Collections.singletonMap(name, sessionFactory);
    }

    public UnitOfWorkAwareProxyFactory(HibernateBundle<?>... bundles) {
        final Map<String, SessionFactory> sessionFactoriesBuilder = new HashMap<>();
        for (HibernateBundle<?> bundle : bundles) {
            sessionFactoriesBuilder.put(bundle.name(), bundle.getSessionFactory());
        }
        sessionFactories = Collections.unmodifiableMap(sessionFactoriesBuilder);
    }


    /**
     * Creates a new <b>@UnitOfWork</b> aware proxy of a class with the default constructor.
     *
     * @param clazz the specified class definition
     * @param <T>   the type of the class
     * @return a new proxy
     */
    public <T> T create(Class<T> clazz) {
        return create(clazz, new Class<?>[]{}, new Object[]{});
    }

    /**
     * Creates a new <b>@UnitOfWork</b> aware proxy of a class with an one-parameter constructor.
     *
     * @param clazz                the specified class definition
     * @param constructorParamType the type of the constructor parameter
     * @param constructorArguments the argument passed to the constructor
     * @param <T>                  the type of the class
     * @return a new proxy
     */
    public <T> T create(Class<T> clazz, Class<?> constructorParamType, Object constructorArguments) {
        return create(clazz, new Class<?>[]{constructorParamType}, new Object[]{constructorArguments});
    }

    /**
     * Creates a new <b>@UnitOfWork</b> aware proxy of a class with a complex constructor.
     *
     * @param clazz                 the specified class definition
     * @param constructorParamTypes the types of the constructor parameters
     * @param constructorArguments  the arguments passed to the constructor
     * @param <T>                   the type of the class
     * @return a new proxy
     */
    public <T> T create(Class<T> clazz, Class<?>[] constructorParamTypes, Object[] constructorArguments) {
        final Class<? extends T> proxied = new ByteBuddy().subclass(clazz).method(ElementMatchers.any())
            .intercept(MethodDelegation.to(new MethodInterceptor(sessionFactories)))
            .make()
            .load(clazz.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
            .getLoaded();

        try {
            return (constructorParamTypes.length == 0 ?
                    proxied.getConstructor().newInstance() :
                    proxied.getConstructor(constructorParamTypes).newInstance(constructorArguments));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                InvocationTargetException e) {
            throw new IllegalStateException("Unable to create a proxy for the class '" + clazz + "'", e);
        }
    }

    /**
     * @return a new aspect
     */
    public UnitOfWorkAspect newAspect() {
        return new UnitOfWorkAspect(sessionFactories);
    }

    /**
     * @return a new aspect
     * @param sessionFactories
     */
    public UnitOfWorkAspect newAspect(Map<String, SessionFactory> sessionFactories) {
        return new UnitOfWorkAspect(sessionFactories);
    }

    public class MethodInterceptor {

        private final Map<String, SessionFactory> sessionFactories;

        public MethodInterceptor(Map<String, SessionFactory> sessionFactories) {
          this.sessionFactories = sessionFactories;
        }

        @RuntimeType
        public Object invoke(@Origin Method overridden, @SuperCall Callable<Object> proxy) throws Throwable {
            final UnitOfWork[] unitsOfWork = overridden.getAnnotationsByType(UnitOfWork.class);
            if (unitsOfWork.length == 0) {
                return proxy.call();
            }
            final Map<UnitOfWork, UnitOfWorkAspect> unitOfWorkAspectMap;
            if (unitsOfWork.length == 1) {
                unitOfWorkAspectMap = Collections.singletonMap(unitsOfWork[0], newAspect(sessionFactories));
            } else {
                unitOfWorkAspectMap = new HashMap<>();
                Arrays
                    .stream(unitsOfWork)
                    .collect(Collectors.toMap(UnitOfWork::value, Function.identity(), (first, second) -> second))
                    .values()
                    .forEach(unitOfWork -> unitOfWorkAspectMap.put(unitOfWork, newAspect(sessionFactories)));
            }
            try {
                unitOfWorkAspectMap.forEach((unitOfWork, unitOfWorkAspect) -> unitOfWorkAspect.beforeStart(unitOfWork));
                Object result = proxy.call();
                unitOfWorkAspectMap.values().forEach(UnitOfWorkAspect::afterEnd);
                return result;
            } catch (InvocationTargetException e) {
                unitOfWorkAspectMap.values().forEach(UnitOfWorkAspect::onError);
                throw e.getCause();
            } catch (Exception e) {
                unitOfWorkAspectMap.values().forEach(UnitOfWorkAspect::onError);
                throw e;
            } finally {
                unitOfWorkAspectMap.values().forEach(UnitOfWorkAspect::onFinish);
            }
        }
    }
}
