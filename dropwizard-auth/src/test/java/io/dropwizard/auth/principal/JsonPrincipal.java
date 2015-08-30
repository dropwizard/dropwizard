package io.dropwizard.auth.principal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.auth.PrincipalImpl;

/**
 * Principal instance supporting JSON deserialization.
 */
public class JsonPrincipal extends PrincipalImpl {

    @JsonCreator
    public JsonPrincipal(@JsonProperty("name") String name) {
        super(name);
    }
}
