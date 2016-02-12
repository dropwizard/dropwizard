package io.dropwizard.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class ScanningHibernateBundleTest {

    @Test
    public void testFindEntityClassesFromDirectory() {
        //given
        String packageWithEntities = "io.dropwizard.hibernate.fake.entities.pckg";
        //when
        ImmutableList<Class<?>> findEntityClassesFromDirectory = ScanningHibernateBundle.findEntityClassesFromDirectory(new String[] { packageWithEntities });

        //then
        assertFalse(findEntityClassesFromDirectory.isEmpty());
        assertEquals(4, findEntityClassesFromDirectory.size());
    }

    @Test
    public void testFindEntityClassesFromMultipleDirectories() {
        //given
        String packageWithEntities = "io.dropwizard.hibernate.fake.entities.pckg";
        String packageWithEntities2 = "io.dropwizard.hibernate.fake2.entities.pckg";
        //when
        ImmutableList<Class<?>> findEntityClassesFromDirectory = ScanningHibernateBundle.findEntityClassesFromDirectory(new String[] { packageWithEntities, packageWithEntities2 });

        //then
        assertFalse(findEntityClassesFromDirectory.isEmpty());
        assertEquals(8, findEntityClassesFromDirectory.size());
    }
}
