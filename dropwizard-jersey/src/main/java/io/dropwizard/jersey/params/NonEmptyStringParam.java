package io.dropwizard.jersey.params;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

/**
 * A parameter encapsulating optional string values with the condition that empty string inputs are
 * interpreted as being absent. This class is useful when it is desired for empty parameters to be
 * synonymous with absent parameters instead of empty parameters evaluating to
 * {@code Optional.of("")}.
 */
public class NonEmptyStringParam extends AbstractParam<Optional<String>> {
    protected NonEmptyStringParam(String input) {
        super(input);
    }

    @Override
    protected Optional<String> parse(String input) throws Exception {
        return Optional.fromNullable(Strings.emptyToNull(input));
    }
}
