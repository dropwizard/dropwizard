package io.dropwizard.testing.junit;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static io.dropwizard.testing.junit.ConfigOverride.config;
import static io.dropwizard.testing.junit.DropwizardAppRuleTest.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DropwizardServiceRuleResetConfigOverrideTest {

    @Rule
    public final DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<TestConfiguration>(TestApplication.class,
                                                     resourceFilePath("test-config.yaml"),
                                                     config("message", "A new way to say Hooray!"));

    @Test
    public void test1() throws Exception {
        // in this test we set a NEW system property
        System.setProperty("dw.extra", "Some extra system property");
    }

    @Test
    public void test2() throws Exception {
        // now we check that the property set in the previous test is still in effect
        assertThat(System.getProperty("dw.extra")).isEqualTo("Some extra system property");

        // but the override we configured in our DropwizardAppRule is still correct
        assertThat(System.getProperty("dw.message")).isEqualTo("A new way to say Hooray!");
    }
}
