package com.yammer.dropwizard.views.example;

import com.yammer.dropwizard.views.View;

public class MustacheView extends View {
    private final String happyName;

    public MustacheView(String happyName) {
        super("/yay.mustache");
        this.happyName = happyName;
    }

    public String getHappyName() {
        return happyName;
    }
}
