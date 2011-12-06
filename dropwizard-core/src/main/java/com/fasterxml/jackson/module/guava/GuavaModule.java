package com.fasterxml.jackson.module.guava;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.*;

public class GuavaModule extends Module // can't use just SimpleModule, due to generic types
{
    private static final String NAME = "GuavaModule";
    
    // Should externalize this, probably...
    private final static Version VERSION = new Version(0, 1, 0, null); // 0.1.0

    public GuavaModule()
    {
        
    }

    @Override public String getModuleName() { return NAME; }
    @Override public Version version() { return VERSION; }
    
    @Override
    public void setupModule(SetupContext context)
    {
        context.addDeserializers(new GuavaDeserializers());
        context.addSerializers(new GuavaSerializers());
    }

}
