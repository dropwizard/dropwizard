package com.example.helloworld.resoureces;

import com.codahale.dropwizard.testing.ResourceTest;
import com.example.helloworld.resources.ViewResource;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewResourceTest extends ResourceTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    @Override
    protected void setUpResources() {
        addResource(new ViewResource());
    }

    @Test
    public void freemarkerUTF8() throws Exception {
        // todo
    }

    @Test
    public void freemarkerISO88591() throws Exception {
        // todo
    }

    @Test
    public void mustacheUTF8() throws Exception {
        // todo
    }

    @Test
    public void mustacheISO88591() throws Exception {
        // todo
    }
}
