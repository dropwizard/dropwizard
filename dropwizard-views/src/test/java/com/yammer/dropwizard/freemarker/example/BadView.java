package com.yammer.dropwizard.freemarker.example;

import com.yammer.dropwizard.templates.View;

public class BadView extends View {
    protected BadView() {
        super("/woo-oo-ahh.txt");
    }
}
