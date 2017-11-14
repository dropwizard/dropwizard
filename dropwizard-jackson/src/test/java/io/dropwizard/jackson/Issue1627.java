package io.dropwizard.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.util.UUID;

public class Issue1627 {

    @Nullable
    private final String string;

    @Nullable
    private final UUID uuid;

    public Issue1627(@Nullable String string, @Nullable UUID uuid) {
        this.string = string;
        this.uuid = uuid;
    }

    @JsonProperty
    @Nullable
    public String getString() {
        return this.string;
    }

    @JsonProperty
    @Nullable
    public UUID getUuid() {
        return this.uuid;
    }
}
