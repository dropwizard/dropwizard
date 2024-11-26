package io.dropwizard.hibernate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScanningHibernateBundleTest {

    @Test
    void testFindEntityClassesFromDirectory() {
        String packageWithEntities = "io.dropwizard.hibernate.fake.entities.pckg";

        assertThat(ScanningHibernateBundle.findEntityClassesFromDirectory(new String[]{packageWithEntities}))
            .hasSize(4);
    }

    @Test
    void testFindEntityClassesFromMultipleDirectories() {
        String packageWithEntities = "io.dropwizard.hibernate.fake.entities.pckg";
        String packageWithEntities2 = "io.dropwizard.hibernate.fake2.entities.pckg";

        assertThat(ScanningHibernateBundle.findEntityClassesFromDirectory(new String[]{packageWithEntities, packageWithEntities2}))
            .hasSize(8);
    }
}
