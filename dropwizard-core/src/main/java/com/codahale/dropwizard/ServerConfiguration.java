package com.codahale.dropwizard;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServerConfiguration extends Configuration
{
    @Valid
    @NotNull
    private ServerFactory server = new NopServerFactory();

    /**
     * Returns the server-specific section of the configuration file.
     * 
     * @return server-specific configuration parameters
     */
    @JsonProperty("server")
    public ServerFactory getServerFactory()
    {
        return server;
    }

    /**
     * Sets the HTTP-specific section of the configuration file.
     */
    @JsonProperty("server")
    public void setServerFactory(ServerFactory factory)
    {
        this.server = factory;
    }

}
