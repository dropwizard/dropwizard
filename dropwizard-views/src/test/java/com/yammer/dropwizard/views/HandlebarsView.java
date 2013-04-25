package com.yammer.dropwizard.views;

public class HandlebarsView extends View {
    private final String happyName;

    public HandlebarsView(String happyName) {
        super("/yay.hbs");
        this.happyName = happyName;
    }

    public String getHappyName() {
        return happyName;
    }
}
