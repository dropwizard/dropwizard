package io.dropwizard.views.freemarker;

import io.dropwizard.views.common.View;

public class AutoEscapingView extends View {
    private final String content;

    protected AutoEscapingView(String content) {
        super("/auto-escaping.ftl");
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
