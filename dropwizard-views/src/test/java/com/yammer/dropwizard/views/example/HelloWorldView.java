package com.yammer.dropwizard.views.example;

import com.yammer.dropwizard.views.View;

public class HelloWorldView extends View {
    private final Person person;

    public HelloWorldView(Person person) {
        super("/hello-world.ftl");
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }
}
