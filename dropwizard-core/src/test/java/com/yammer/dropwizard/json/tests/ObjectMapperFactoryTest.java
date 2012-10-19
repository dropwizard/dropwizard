package com.yammer.dropwizard.json.tests;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import com.yammer.dropwizard.json.AnnotationSensitivePropertyNamingStrategy;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ObjectMapperFactoryTest {
    private final ObjectMapperFactory factory = new ObjectMapperFactory();

    @Test
    public void buildsObjectMappers() throws Exception {
        final ObjectMapper mapper = factory.build();

        assertThat(mapper.writeValueAsString("woo"))
                .isEqualTo("\"woo\"");
    }

    @Test
    public void buildsObjectMappersWithSpecificJsonFactories() throws Exception {
        final JsonFactory jsonFactory = new YAMLFactory();
        final ObjectMapper mapper = factory.build(jsonFactory);

        assertThat(mapper.getFactory())
                .isSameAs(jsonFactory);
    }

    @Test
    public void defaultsToAnnotationSensitivePropertyNames() throws Exception {
        assertThat(factory.getPropertyNamingStrategy())
                .isEqualTo(AnnotationSensitivePropertyNamingStrategy.INSTANCE);

        final ObjectMapper mapper = factory.build();

        assertThat(mapper.getDeserializationConfig().getPropertyNamingStrategy())
                .isSameAs(AnnotationSensitivePropertyNamingStrategy.INSTANCE);
    }

    @Test
    public void defaultsToAllowingComments() throws Exception {
        final ObjectMapper mapper = factory.build();

        assertThat(mapper.getFactory().isEnabled(JsonParser.Feature.ALLOW_COMMENTS))
                .isTrue();
    }

    @Test
    public void defaultsToAllowingUnknownFields() throws Exception {
        final ObjectMapper mapper = factory.build();

        assertThat(mapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES))
                .isFalse();
    }

    @Test
    public void supportsBasicGuavaTypes() throws Exception {
        final ObjectMapper mapper = factory.build();

        assertThat(mapper.readValue("[1,2,3]", new TypeReference<ImmutableList<Integer>>() {}))
                .isEqualTo(ImmutableList.of(1, 2, 3));
    }

    @Test
    public void supportsExtraGuavaTypes() throws Exception {
        final ObjectMapper mapper = factory.build();

        assertThat(mapper.readValue("\"one:1\"", HostAndPort.class))
                .isEqualTo(HostAndPort.fromParts("one", 1));
    }

    @Test
    public void supportsLogbackTypes() throws Exception {
        final ObjectMapper mapper = factory.build();

        assertThat(mapper.readValue("\"ALL\"", Level.class))
                .isEqualTo(Level.ALL);
    }
}

