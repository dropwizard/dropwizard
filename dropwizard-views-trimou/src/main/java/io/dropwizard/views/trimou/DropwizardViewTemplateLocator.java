package io.dropwizard.views.trimou;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.trimou.engine.config.Configuration;
import org.trimou.engine.config.EngineConfigurationKey;
import org.trimou.engine.locator.AbstractTemplateLocator;
import org.trimou.engine.locator.TemplateLocator;
import org.trimou.exception.MustacheException;
import org.trimou.exception.MustacheProblem;

/**
 * A {@link TemplateLocator} for views.
 *
 * @author Martin Kouba
 */
public class DropwizardViewTemplateLocator extends AbstractTemplateLocator {

    private String defaultFileEncoding;

    protected DropwizardViewTemplateLocator(int priority) {
        super(priority);
    }

    @Override
    public Reader locate(String templateId) {
        InputStream in = this.getClass().getResourceAsStream(templateId);
        try {
            return in != null ? new InputStreamReader(in, defaultFileEncoding) : null;
        } catch (UnsupportedEncodingException e) {
            throw new MustacheException(MustacheProblem.TEMPLATE_LOADING_ERROR, e);
        }
    }

    @Override
    public Set<String> getAllIdentifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void init(Configuration configuration) {
        this.defaultFileEncoding = configuration.getStringPropertyValue(EngineConfigurationKey.DEFAULT_FILE_ENCODING);
    }

}
