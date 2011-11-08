package com.yammer.flopwizard.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/upload")
@Consumes(MediaType.WILDCARD)
public class UploadResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadResource.class);
    
    @POST
    public void upload(String body) {
        LOGGER.info("New upload: " + body);
    }
}
