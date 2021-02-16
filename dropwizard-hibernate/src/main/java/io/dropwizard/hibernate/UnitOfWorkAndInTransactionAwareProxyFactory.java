package io.dropwizard.hibernate;

import com.campspot.jdbi3.DAOManager;
import com.campspot.jdbi3.InTransaction;
import com.campspot.jdbi3.TransactionAspect;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.hibernate.SessionFactory;
import org.jdbi.v3.core.Jdbi;

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
public class UnitOfWorkAndInTransactionAwareProxyFactory {

    private final Map<String, SessionFactory> sessionFactories;
    private final DAOManager daoManager;
    private final Map<String, Jdbi> dbis;

    public UnitOfWorkAndInTransactionAwareProxyFactory(String name, SessionFactory sessionFactory, DAOManager daoManager, Map<String, Jdbi> dbis) {
        sessionFactories = Collections.singletonMap(name, sessionFactory);
        this.daoManager = daoManager;
        this.dbis = dbis;
    }

    public UnitOfWorkAndInTransactionAwareProxyFactory(DAOManager daoManager, Map<String, Jdbi> dbis, HibernateBundle<?>... bundles) {
        final Map<String, SessionFactory> sessionFactoriesBuilder = new HashMap<>();
        for (HibernateBundle<?> bundle : bundles) {
            sessionFactoriesBuilder.put(bundle.name(), bundle.getSessionFactory());
        }
        sessionFactories = Collections.unmodifiableMap(sessionFactoriesBuilder);
        this.daoManager = daoManager;
        this.dbis = dbis;
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
                final InTransaction inTransaction = overridden.getAnnotation(InTransaction.class);
                final TransactionAspect transactionAspect = newTransactionAspect(dbis, daoManager);
                final UnitOfWorkAspect unitOfWorkAspect = newUnitOfWorkAspect(sessionFactories);
                try {
                    unitOfWorkAspect.beforeStart(unitOfWork);
                    transactionAspect.beforeStart(inTransaction);
                    Object result = proceed.invoke(self, args);
                    unitOfWorkAspect.afterEnd();
                    transactionAspect.afterEnd();
                    return result;
                } catch (InvocationTargetException e) {
                    unitOfWorkAspect.onError();
                    transactionAspect.onError();
                    throw e.getCause();
                } catch (Exception e) {
                    unitOfWorkAspect.onError();
                    transactionAspect.onError();
                    throw e;
                } finally {
                    unitOfWorkAspect.onFinish();
                    transactionAspect.onFinish();
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
        return new UnitOfWorkAspect(sessionFactories);
    }

    /**
     * @return a new aspect
     * @param sessionFactories
     */
    public UnitOfWorkAspect newUnitOfWorkAspect(Map<String, SessionFactory> sessionFactories) {
        return new UnitOfWorkAspect(sessionFactories);
    }

    public TransactionAspect newTransactionAspect(Map<String, Jdbi> dbis, DAOManager daoManager) {
        return new TransactionAspect(dbis, daoManager);
    }
}
