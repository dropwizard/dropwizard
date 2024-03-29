package io.dropwizard.documentation.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import io.dropwizard.documentation.mq.MessageQueueFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// core: ExampleConfiguration
public class ExampleConfiguration extends Configuration {
    @Valid
    @NotNull
    private MessageQueueFactory messageQueue = new MessageQueueFactory();

    @JsonProperty("messageQueue")
    public MessageQueueFactory getMessageQueueFactory() {
        return messageQueue;
    }

    @JsonProperty("messageQueue")
    public void setMessageQueueFactory(MessageQueueFactory factory) {
        this.messageQueue = factory;
    }
}
// core: ExampleConfiguration
