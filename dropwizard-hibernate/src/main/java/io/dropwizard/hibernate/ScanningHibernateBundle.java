package io.dropwizard.hibernate;

import javax.persistence.Entity;

import io.dropwizard.Configuration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.IOException;
import java.io.InputStream;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.glassfish.jersey.server.internal.scanning.AnnotationAcceptingListener;

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

        PackageNamesScanner scanner = new PackageNamesScanner(new String[] {pckg}, true);

        @SuppressWarnings("unchecked")
        final AnnotationAcceptingListener asl = new AnnotationAcceptingListener(Entity.class);
        
        while (scanner.hasNext()) {
          final String next = scanner.next();
                if (asl.accept(next)) {
                    final InputStream in = scanner.open();
                    try {
                        asl.process(next, in);
                    } catch (IOException e) {
                      throw new RuntimeException("AnnotationAcceptingListener failed to process scanned resource: " + next);
                    } finally {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            throw new RuntimeException("Error closing resource stream", ex);
                        }
                    }
                }
            }

        for (Class<?> clazz : asl.getAnnotatedClasses()) {
            builder.add(clazz);
        }

        return builder.build();
    }
}
