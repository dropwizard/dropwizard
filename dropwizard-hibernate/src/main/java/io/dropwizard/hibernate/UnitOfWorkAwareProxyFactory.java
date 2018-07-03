package io.dropwizard.hibernate;

import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory for creating proxies for components that use Hibernate data access objects
 * outside Jersey resources.
 * <p>A created proxy will be aware of the {@link UnitOfWork} annotation
 * on the original class methods and will open a Hibernate session with a transaction
 * around them.</p>
 */
public class UnitOfWorkAwareProxyFactory {

    private final Map<String, ClusteredSessionFactory> clusteredSessionFactories;

    public UnitOfWorkAwareProxyFactory(String name, ClusteredSessionFactory clusteredSessionFactory) {
        clusteredSessionFactories = Collections.singletonMap(name, clusteredSessionFactory);
    }

    public UnitOfWorkAwareProxyFactory(HibernateBundle<?>... bundles) {
        final Map<String, ClusteredSessionFactory> clusteredSessionFactoriesBuilder = new HashMap<>();
        for (HibernateBundle<?> bundle : bundles) {
            clusteredSessionFactoriesBuilder.put(bundle.name(), bundle.getSessionFactory());
        }
        clusteredSessionFactories = Collections.unmodifiableMap(clusteredSessionFactoriesBuilder);
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
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> clazz, Class<?>[] constructorParamTypes, Object[] constructorArguments) {
        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(clazz);

        try {
            final Proxy proxy = (Proxy) (constructorParamTypes.length == 0 ?
                    factory.createClass().getConstructor().newInstance() :
                    factory.create(constructorParamTypes, constructorArguments));
            proxy.setHandler((self, overridden, proceed, args) -> {
                final UnitOfWork unitOfWork = overridden.getAnnotation(UnitOfWork.class);
                final UnitOfWorkAspect unitOfWorkAspect = newAspect(clusteredSessionFactories);
                try {
                    unitOfWorkAspect.beforeStart(unitOfWork);
                    Object result = proceed.invoke(self, args);
                    unitOfWorkAspect.afterEnd();
                    return result;
                } catch (InvocationTargetException e) {
                    unitOfWorkAspect.onError();
                    throw e.getCause();
                } catch (Exception e) {
                    unitOfWorkAspect.onError();
                    throw e;
                } finally {
                    unitOfWorkAspect.onFinish();
                }
            });
            return (T) proxy;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                InvocationTargetException e) {
            throw new IllegalStateException("Unable to create a proxy for the class '" + clazz + "'", e);
        }
    }

    /**
     * @return a new aspect
     */
    public UnitOfWorkAspect newAspect() {
        return new UnitOfWorkAspect(clusteredSessionFactories);
    }

    /**
     * @return a new aspect
     * @param clusteredSessionFactories
     */
    public UnitOfWorkAspect newAspect(Map<String, ClusteredSessionFactory> clusteredSessionFactories) {
        return new UnitOfWorkAspect(clusteredSessionFactories);
    }
}
