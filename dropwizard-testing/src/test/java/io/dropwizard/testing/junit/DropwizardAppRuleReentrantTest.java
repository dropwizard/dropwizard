package io.dropwizard.testing.junit;

import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class DropwizardAppRuleReentrantTest {

    DropwizardTestSupport<TestConfiguration> testSupport = mock(DropwizardTestSupport.class);
    Statement statement = mock(Statement.class);
    Description description = mock(Description.class);

    @Test
    public void testReentrantRuleStartsApplicationOnlyOnce() throws Throwable {
        DropwizardAppRule<TestConfiguration> dropwizardAppRule = new DropwizardAppRule<>(testSupport);

        RuleChain.outerRule(dropwizardAppRule)
            .around(dropwizardAppRule) // recursive
            .apply(statement, description)
            .evaluate();

        InOrder inOrder = inOrder(testSupport, statement, description);
        inOrder.verify(testSupport, times(1)).before();
        inOrder.verify(statement).evaluate();
        inOrder.verify(testSupport, times(1)).after();
        inOrder.verifyNoMoreInteractions();
    }
}
