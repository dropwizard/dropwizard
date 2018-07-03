package io.dropwizard.hibernate;

import io.dropwizard.Configuration;
import org.glassfish.jersey.server.internal.scanning.AnnotationAcceptingListener;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;

import javax.persistence.Entity;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension of HibernateBundle that scans given package for entities instead of giving them by hand.
 */
public abstract class ScanningHibernateBundle<T extends Configuration> extends HibernateBundle<T> {
    /**
     * @param pckg string with package containing Hibernate entities (classes annotated with Hibernate {@code @Entity}
     *             annotation) e. g. {@code com.codahale.fake.db.directory.entities}
     */
    protected ScanningHibernateBundle(String pckg) {
        this(pckg, new ClusteredSessionFactoryFactory());
    }

    protected ScanningHibernateBundle(String pckg, ClusteredSessionFactoryFactory clusteredSessionFactoryFactory) {
        this(new String[]{pckg}, clusteredSessionFactoryFactory);
    }

    protected ScanningHibernateBundle(String[] pckgs, ClusteredSessionFactoryFactory clusteredSessionFactoryFactory) {
        super(findEntityClassesFromDirectory(pckgs), clusteredSessionFactoryFactory);
    }

    /**
     * Method scanning given directory for classes containing Hibernate @Entity annotation
     *
     * @param pckgs string array with packages containing Hibernate entities (classes annotated with @Entity annotation)
     *             e.g. com.codahale.fake.db.directory.entities
     * @return ImmutableList with classes from given directory annotated with Hibernate @Entity annotation
     */
    public static List<Class<?>> findEntityClassesFromDirectory(String[] pckgs) {
        @SuppressWarnings("unchecked")
        final AnnotationAcceptingListener asl = new AnnotationAcceptingListener(Entity.class);
        try (final PackageNamesScanner scanner = new PackageNamesScanner(pckgs, true)) {
            while (scanner.hasNext()) {
                final String next = scanner.next();
                if (asl.accept(next)) {
                    try (final InputStream in = scanner.open()) {
                        asl.process(next, in);
                    } catch (IOException e) {
                        throw new RuntimeException("AnnotationAcceptingListener failed to process scanned resource: " + next);
                    }
                }
            }
        }

        return new ArrayList<>(asl.getAnnotatedClasses());
    }
}
