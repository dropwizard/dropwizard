package io.dropwizard.client.proxy;

import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of {@link org.apache.hc.client5.http.routing.HttpRoutePlanner}
 * that routes requests through proxy and takes into account list of hosts that should not be proxied
 */
public class NonProxyListProxyRoutePlanner extends DefaultProxyRoutePlanner {

    private static final Pattern WILDCARD = Pattern.compile("\\*");
    private static final String REGEX_WILDCARD = ".*";

    private List<Pattern> nonProxyHostPatterns;

    public NonProxyListProxyRoutePlanner(HttpHost proxy, @Nullable List<String> nonProxyHosts) {
        super(proxy, null);
        nonProxyHostPatterns = getNonProxyHostPatterns(nonProxyHosts);
    }

    public NonProxyListProxyRoutePlanner(HttpHost proxy, SchemePortResolver schemePortResolver,
                                         @Nullable List<String> nonProxyHosts) {
        super(proxy, schemePortResolver);
        this.nonProxyHostPatterns = getNonProxyHostPatterns(nonProxyHosts);
    }

    private List<Pattern> getNonProxyHostPatterns(@Nullable List<String> nonProxyHosts) {
        if (nonProxyHosts == null) {
            return Collections.emptyList();
        }

        final List<Pattern> patterns = new ArrayList<>(nonProxyHosts.size());
        for (String nonProxyHost : nonProxyHosts) {
            // Replaces a wildcard to a regular expression
            patterns.add(Pattern.compile(WILDCARD.matcher(nonProxyHost).replaceAll(REGEX_WILDCARD)));
        }
        return Collections.unmodifiableList(patterns);
    }

    protected List<Pattern> getNonProxyHostPatterns() {
        return nonProxyHostPatterns;
    }

    @Override
    @Nullable
    protected HttpHost determineProxy(HttpHost target, HttpContext context) throws HttpException {
        for (Pattern nonProxyHostPattern : nonProxyHostPatterns) {
            if (nonProxyHostPattern.matcher(target.getHostName()).matches()) {
                return null;
            }
        }
        return super.determineProxy(target, context);
    }
}
