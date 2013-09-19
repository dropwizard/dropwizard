package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BrokenRepresentation {
    private List<String> messages;

    public BrokenRepresentation(List<String> messages) {
        this.messages = messages;
    }

    @JsonProperty
    public List<String> getMessages() {
        return messages;
    }

    @JsonProperty
    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
