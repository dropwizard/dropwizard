package io.dropwizard.testing.junit5;

/**
 * {@link DropwizardExtensionsSupport} class uses this interface to find fields in test class instance and test
 * class
 */
public interface DropwizardExtension {

    /**
     * Executed before test method or class.
     */
    void before() throws Throwable;

    /**
     * Executed after test method or class.
     */
    void after() throws Throwable;
}
