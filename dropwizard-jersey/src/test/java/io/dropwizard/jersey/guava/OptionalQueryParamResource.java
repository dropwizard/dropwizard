package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;
import io.dropwizard.jersey.params.UUIDParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/optional")
public class OptionalQueryParamResource {

    @GET
    @Path("/message")
    public String getMessage(@QueryParam("message") Optional<String> message) {
        return message.or("Default Message");
    }

    @GET
    @Path("/my-message")
    public String getMyMessage(@QueryParam("mymessage") Optional<MyMessage> myMessage) {
        return myMessage.or(new MyMessage("My Default Message")).getMessage();
    }

    @GET
    @Path("/uuid")
    public String getUUID(@QueryParam("uuid") Optional<UUIDParam> uuid) {
        return uuid.or(new UUIDParam("d5672fa8-326b-40f6-bf71-d9dacf44bcdc")).get().toString();
    }
}
