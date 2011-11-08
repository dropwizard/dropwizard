package com.yammer.flopwizard.example;

import com.yammer.dropwizard.json.JsonSnakeCase;

@JsonSnakeCase
public class Saying {
    private final int sayingId;
    private final String text;

    public Saying(int sayingId, String text) {
        this.sayingId = sayingId;
        this.text = text;
    }

    public int getSayingId() {
        return sayingId;
    }

    public String getText() {
        return text;
    }
}
