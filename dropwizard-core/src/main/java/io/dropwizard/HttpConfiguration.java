package io.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects.ToStringHelper;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class HttpConfiguration extends Configuration {
    @Valid
    @NotNull
    private ServerFactory server = new DefaultServerFactory();

    /**
     * Returns the server-specific section of the configuration file.
     *
     * @return server-specific configuration parameters
     */
    @JsonProperty("server")
    public ServerFactory getServerFactory() {
        return server;
    }

    /**
     * Sets the HTTP-specific section of the configuration file.
     */
    @JsonProperty("server")
    public void setServerFactory(ServerFactory factory) {
        this.server = factory;
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("server", server);
    }

}
