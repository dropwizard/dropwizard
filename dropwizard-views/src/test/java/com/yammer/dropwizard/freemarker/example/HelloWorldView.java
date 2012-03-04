package com.yammer.dropwizard.freemarker.example;

import com.yammer.dropwizard.templates.View;

public class HelloWorldView extends View<Person> {
    public HelloWorldView(Person person) {
        super("/hello-world.ftl", person);
    }
}
