package io.dropwizard.jersey.params;

import com.google.common.base.Strings;

import java.util.Optional;

/**
 * A parameter encapsulating optional string values with the condition that empty string inputs are
 * interpreted as being absent. This class is useful when it is desired for empty parameters to be
 * synonymous with absent parameters instead of empty parameters evaluating to
 * {@code Optional.of("")}.
 */
public class NonEmptyStringParam extends AbstractParam<Optional<String>> {
    public NonEmptyStringParam(String input) {
        super(input);
    }

    @Override
    protected Optional<String> parse(String input) throws Exception {
        return Optional.ofNullable(Strings.emptyToNull(input));
    }
}
