package com.example.helloworld.resoureces;

import com.example.helloworld.resources.ProtectedResource;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ProtectedResourceTest extends ResourceTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    @Override
    protected void setUpResources() {
        addResource(new ProtectedResource());
    }

    @Test
    public void showSecret() throws Exception {
        // todo
    }
}
