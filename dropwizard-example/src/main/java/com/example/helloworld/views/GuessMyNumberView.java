package com.example.helloworld.views;

import com.yammer.dropwizard.views.View;

public class GuessMyNumberView extends View {

    public GuessMyNumberView(String message) {
        super("guess-my-number.ftl");
        this.message = message;
    }

    private final String message;

    public String getMessage() {
        return message;
    }
}
