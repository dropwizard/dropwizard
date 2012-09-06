package com.yammer.dropwizard.views;

import java.io.IOException;

public class ViewRenderException extends IOException {
    private static final long serialVersionUID = -2972444466317717696L;
    public ViewRenderException(String message) {
        super(message);
    }
}
