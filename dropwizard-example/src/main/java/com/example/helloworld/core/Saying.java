package com.example.helloworld.core;

import org.codehaus.jackson.annotate.JsonProperty;

public class Saying {
    @JsonProperty
    private long id;

    @JsonProperty
    private String content;

    private Saying() {
        // Jackson deserialization
    }

    public Saying(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
