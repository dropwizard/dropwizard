package io.dropwizard.views.trimou;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.trimou.engine.config.Configuration;
import org.trimou.engine.config.EngineConfigurationKey;
import org.trimou.engine.locator.PathTemplateLocator;
import org.trimou.engine.locator.TemplateLocator;
import org.trimou.exception.MustacheException;
import org.trimou.exception.MustacheProblem;
import org.trimou.util.Strings;

/**
 * A {@link TemplateLocator} for partials and template inheritance.
 *
 * @author Martin Kouba
 */
public class DropwizardFragmentTemplateLocator extends PathTemplateLocator<String> {

    private String defaultFileEncoding;

    protected DropwizardFragmentTemplateLocator(int priority, String suffix) {
        super(priority, Strings.SLASH, suffix.substring(1));
    }

    @Override
    public Reader locate(String templateId) {
        InputStream in = this.getClass().getResourceAsStream(getRealPath(templateId));
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

    @Override
    protected String constructVirtualPath(String source) {
        throw new UnsupportedOperationException();
    }

    private String getRealPath(String templateId) {
        return getRootPath() + addSuffix(toRealPath(templateId));
    }

}
