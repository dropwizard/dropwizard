package io.dropwizard.views.freemarker;

import io.dropwizard.views.common.View;

public class CspView extends View {
    protected CspView() {
        super("/csp-nonce.ftl");
    }
}
