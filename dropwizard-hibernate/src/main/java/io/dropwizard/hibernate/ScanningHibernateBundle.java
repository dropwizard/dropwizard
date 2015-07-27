package io.dropwizard.hibernate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import io.dropwizard.HttpConfiguration;
import org.glassfish.jersey.server.internal.scanning.AnnotationAcceptingListener;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;

import javax.persistence.Entity;
import java.io.IOException;
import java.io.InputStream;

/**
 * Extension of HibernateBundle that scans given package for entities instead of giving them by hand.
 */
public abstract class ScanningHibernateBundle<T extends HttpConfiguration> extends HibernateBundle<T> {
    /**
     * @param pckg string with package containing Hibernate entities (classes annotated with Hibernate {@code @Entity}
     *             annotation) e. g. {@code com.codahale.fake.db.directory.entities}
     */
    protected ScanningHibernateBundle(String pckg) {
        this(pckg, new SessionFactoryFactory());
    }

    protected ScanningHibernateBundle(String pckg, SessionFactoryFactory sessionFactoryFactory) {
        super(findEntityClassesFromDirectory(pckg), sessionFactoryFactory);
    }

    /**
     * Method scanning given directory for classes containing Hibernate @Entity annotation
     *
     * @param pckg string with package containing Hibernate entities (classes annotated with @Entity annotation)
     *             e.g. com.codahale.fake.db.directory.entities
     * @return ImmutableList with classes from given directory annotated with Hibernate @Entity annotation
     */
    public static ImmutableList<Class<?>> findEntityClassesFromDirectory(String pckg) {
        @SuppressWarnings("unchecked")
        final AnnotationAcceptingListener asl = new AnnotationAcceptingListener(Entity.class);
        final PackageNamesScanner scanner = new PackageNamesScanner(new String[]{pckg}, true);

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

        final Builder<Class<?>> builder = ImmutableList.builder();
        for (Class<?> clazz : asl.getAnnotatedClasses()) {
            builder.add(clazz);
        }

        return builder.build();
    }
}
