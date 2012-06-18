package com.yammer.dropwizard.swagger;

import com.sun.jersey.api.core.ResourceConfig;
import com.wordnik.swagger.jaxrs.JavaHelp;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public abstract class SwaggerResource extends JavaHelp {

    @GET
    @Path("__api/describe")
    @Override
    public Response getHelp(@Context ServletConfig servConfig, @Context ResourceConfig resConfig,
                            @Context HttpHeaders headers, @Context UriInfo uriInfo) {
        return super.getHelp(servConfig, resConfig, headers, uriInfo);
    }
}
