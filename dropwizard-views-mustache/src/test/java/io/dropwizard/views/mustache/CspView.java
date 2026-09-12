package io.dropwizard.views.mustache;

import io.dropwizard.views.common.View;

public class CspView extends View {
    protected CspView() {
        super("/csp-nonce.mustache");
    }
}
