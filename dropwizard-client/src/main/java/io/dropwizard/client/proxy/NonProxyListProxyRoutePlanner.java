package io.dropwizard.client.proxy;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of {@link org.apache.http.conn.routing.HttpRoutePlanner}
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
            return ImmutableList.of();
        }

        final ImmutableList.Builder<Pattern> patterns = ImmutableList.builder();
        for (String nonProxyHost : nonProxyHosts) {
            // Replaces a wildcard to a regular expression
            patterns.add(Pattern.compile(WILDCARD.matcher(nonProxyHost).replaceAll(REGEX_WILDCARD)));
        }
        return patterns.build();
    }

    @VisibleForTesting
    protected List<Pattern> getNonProxyHostPatterns() {
        return nonProxyHostPatterns;
    }

    @Override
    protected HttpHost determineProxy(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        for (Pattern nonProxyHostPattern : nonProxyHostPatterns) {
            if (nonProxyHostPattern.matcher(target.getHostName()).matches()) {
                return null;
            }
        }
        return super.determineProxy(target, request, context);
    }
}
