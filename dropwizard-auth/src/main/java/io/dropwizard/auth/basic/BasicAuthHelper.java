package io.dropwizard.auth.basic;

import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpRequestContext;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class BasicAuthHelper {
    public static final String CHALLENGE_FORMAT = "Basic realm=\"%s\"";
    private static final Pattern authorizationPattern = Pattern.compile("^Basic (.*)$");
    private static final Pattern decodedPattern = Pattern.compile("^(.*):(.*)$");

    private BasicAuthHelper() {
    }

    public static Optional<BasicCredentials> getBasicCredentialsFromHeader(HttpRequestContext httpRequestContext) {
        final String header = httpRequestContext.getHeaderValue(HttpHeaders.AUTHORIZATION);
        if (header == null) {
            return Optional.absent();
        }
        final Matcher authorizationMatcher = authorizationPattern.matcher(header);
        if (!authorizationMatcher.matches()) {
            return Optional.absent();
        }

        final String encoded = authorizationMatcher.group(1);
        final Matcher decodeMatcher = decodedPattern.matcher(B64Code.decode(encoded, StringUtil.__ISO_8859_1));
        if (!decodeMatcher.matches()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());
        }

        return Optional.of(new BasicCredentials(decodeMatcher.group(1), decodeMatcher.group(2)));
    }

    public static Response createUnauthorizedResponse(final String realm) {
        final String challenge = String.format(CHALLENGE_FORMAT, realm);
        return Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, challenge)
                .entity("Credentials are required to access this resource.")
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build();
    }
}
