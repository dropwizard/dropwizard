package io.dropwizard.jdbi3;

import org.jdbi.v3.core.extension.ExtensionMethod;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.TemplateEngine;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NamePrependingTemplateEngineTest {
    public interface MyDao {
        String myDbCall();
    }

    @Test
    public void testNoExtensionMethodShouldReturnOriginal() {
        final TemplateEngine original = mock(TemplateEngine.class);
        final StatementContext ctx = mock(StatementContext.class);
        final String template = UUID.randomUUID().toString();
        final String originalRendered = UUID.randomUUID().toString();

        when(ctx.getExtensionMethod()).thenReturn(null);
        when(original.render(template, ctx)).thenReturn(originalRendered);

        final NamePrependingTemplateEngine sut = new NamePrependingTemplateEngine(original);

        final String result = sut.render(template, ctx);

        assertThat(result).isEqualTo(originalRendered);
    }

    @Test
    public void testPrependsCorrectName() throws NoSuchMethodException {
        final TemplateEngine original = mock(TemplateEngine.class);
        final StatementContext ctx = mock(StatementContext.class);
        final String template = UUID.randomUUID().toString();
        final String originalRendered = UUID.randomUUID().toString();

        final ExtensionMethod extensionMethod = new ExtensionMethod(MyDao.class, MyDao.class.getMethod("myDbCall"));

        when(ctx.getExtensionMethod()).thenReturn(extensionMethod);
        when(original.render(template, ctx)).thenReturn(originalRendered);

        final NamePrependingTemplateEngine sut = new NamePrependingTemplateEngine(original);

        final String result = sut.render(template, ctx);

        assertThat(result).containsSequence(
            "/* ",
            extensionMethod.getType().getSimpleName(),
            ".",
            extensionMethod.getMethod().getName(),
            " */",
            originalRendered);
    }
}
