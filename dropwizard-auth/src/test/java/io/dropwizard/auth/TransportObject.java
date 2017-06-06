package io.dropwizard.auth;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** 
 *
 * Some object with a "secret" internal representation we want only authenticated 
 * people to know about.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class TransportObject {
    @NotNull
    @Size(min = 1)
    @Pattern(regexp="ab?b")
    @JsonProperty
    private String givenName;
    
    public String getGivenName() {
        return givenName;
    }
    
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
}
