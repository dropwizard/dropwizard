package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.time.LocalDate;

public class OkRepresentation {

    private Integer message = 0;

    @Nullable
    private LocalDate date;

    @JsonProperty
    public Integer getMessage() {
        return message;
    }

    @JsonProperty
    @Nullable
    public LocalDate getDate() {
        return date;
    }

    @JsonProperty
    public void setMessage(Integer message) {
        this.message = message;
    }

    @JsonProperty
    public void setDate(LocalDate date) {
        this.date = date;
    }
}
