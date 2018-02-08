package io.dropwizard.jdbi3;

import org.jdbi.v3.core.extension.ExtensionMethod;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.TemplateEngine;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NamePrependingTemplateEngineTest {
    private static final String TEMPLATE = UUID.randomUUID().toString();
    private static final String ORIGINAL_RENDERED = UUID.randomUUID().toString();

    public interface MyDao {
        String myDbCall();
    }

    private TemplateEngine original;
    private StatementContext ctx;
    private NamePrependingTemplateEngine sut;

    @Before
    public void setup() {
        original = mock(TemplateEngine.class);
        ctx = mock(StatementContext.class);
        when(original.render(TEMPLATE, ctx)).thenReturn(ORIGINAL_RENDERED);

        sut = new NamePrependingTemplateEngine(original);
    }

    @Test
    public void testNoExtensionMethodShouldReturnOriginal() {
        when(ctx.getExtensionMethod()).thenReturn(null);

        final String result = sut.render(TEMPLATE, ctx);

        assertThat(result).isEqualTo(ORIGINAL_RENDERED);
    }

    @Test
    public void testPrependsCorrectName() throws NoSuchMethodException {
        final ExtensionMethod extensionMethod = new ExtensionMethod(MyDao.class, MyDao.class.getMethod("myDbCall"));

        when(ctx.getExtensionMethod()).thenReturn(extensionMethod);

        final String result = sut.render(TEMPLATE, ctx);

        assertThat(result).isEqualTo(
            "/* " +
            extensionMethod.getType().getSimpleName() +
            "." +
            extensionMethod.getMethod().getName() +
            " */ " +
            ORIGINAL_RENDERED);
    }
}
