package io.dropwizard.hibernate;

import javax.persistence.Entity;

import io.dropwizard.Configuration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.sun.jersey.spi.scanning.AnnotationScannerListener;

/**
 * Extension of HibernateBundle that scans given package for entites instead of giving them by hand.
 *
 */
public abstract class ScanningHibernateBundle<T extends Configuration> extends HibernateBundle<T> {

    /**
     *
     * @param pckg string with package containing Hibernate entities (classes annotated with Hibernate @Entity annotation)
     *          e.g. com.codahale.fake.db.directory.entities
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
     *          e.g. com.codahale.fake.db.directory.entities
     * @return ImmutableList with classes from given directory annotated with Hibernate @Entity annotation
     */
    public static ImmutableList<Class<?>> findEntityClassesFromDirectory(String pckg) {
        Builder<Class<?>> builder = ImmutableList.<Class<?>>builder();

        PackageNamesScanner scanner = new PackageNamesScanner(new String[] {pckg});

        @SuppressWarnings("unchecked")
        final AnnotationScannerListener asl = new AnnotationScannerListener(Entity.class);
        scanner.scan(asl);

        for (Class<?> clazz : asl.getAnnotatedClasses()) {
            builder.add(clazz);
        }

        return builder.build();
    }
}
