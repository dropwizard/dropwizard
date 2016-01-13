package io.dropwizard.testing.junit;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class DropwizardAppRuleReentrantTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    DropwizardTestSupport<TestConfiguration> testSupport;

    @Mock
    Statement statement;

    @Mock
    Description description;

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
