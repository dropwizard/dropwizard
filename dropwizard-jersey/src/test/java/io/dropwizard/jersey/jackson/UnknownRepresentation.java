package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UnknownRepresentation {
    private int bork;

    public UnknownRepresentation(int bork) {
        this.bork = bork;
    }

    @JsonProperty
    public int getBork() {
        return bork;
    }

    @JsonProperty
    public void setBork(int bork) {
        this.bork = bork;
    }
}
