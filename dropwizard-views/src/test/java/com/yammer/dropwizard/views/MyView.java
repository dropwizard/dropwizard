package com.yammer.dropwizard.views;

public class MyView extends View {
    private final String name;
    
    public MyView(String name) {
        super("/example.ftl");
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
