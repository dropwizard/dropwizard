package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = CustomDeserialization.class)
public class CustomRepresentation {
    public int id;
}
