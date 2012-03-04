package com.yammer.dropwizard.freemarker.example;

import com.yammer.dropwizard.templates.View;

public class BadView extends View<String> {
    protected BadView(String model) {
        super("/woo-oo-ahh.txt", model);
    }
}
