package com.yammer.dropwizard.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.yammer.dropwizard.logging.Log;
import org.yaml.snakeyaml.nodes.*;

import java.io.*;

class YamlConverter {
    private static final Log LOG = Log.forClass(YamlConverter.class);
    private final Json json;
    private final JsonFactory factory;

    YamlConverter(Json json, JsonFactory factory) {
        this.factory = factory;
        this.json = json;
    }

    JsonNode convert(File file) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final JsonGenerator generator = factory.createJsonGenerator(output).useDefaultPrettyPrinter();
        final Reader reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8);
        try {
            final Node yaml = new org.yaml.snakeyaml.Yaml().compose(reader);
            build(yaml, generator);
            generator.close();
            LOG.debug("Parsed {} as:\n {}", file, output.toString());
            return json.readValue(output.toByteArray(), JsonNode.class);
        } finally {
            reader.close();
        }
    }

    private void build(Node yaml, JsonGenerator json) throws IOException {
        if (yaml instanceof MappingNode) {
            final MappingNode mappingNode = (MappingNode) yaml;
            json.writeStartObject();
            for (NodeTuple tuple : mappingNode.getValue()) {
                if (tuple.getKeyNode() instanceof ScalarNode) {
                    json.writeFieldName(((ScalarNode) tuple.getKeyNode()).getValue());
                }

                build(tuple.getValueNode(), json);
            }
            json.writeEndObject();
        } else if (yaml instanceof SequenceNode) {
            json.writeStartArray();
            for (Node node : ((SequenceNode) yaml).getValue()) {
                build(node, json);
            }
            json.writeEndArray();
        } else if (yaml instanceof ScalarNode) {
            final ScalarNode scalarNode = (ScalarNode) yaml;
            final String className = scalarNode.getTag().getClassName();
            if ("bool".equals(className)) {
                json.writeBoolean(Boolean.parseBoolean(scalarNode.getValue()));
            } else if ("int".equals(className)) {
                json.writeNumber(Long.parseLong(scalarNode.getValue()));
            } else if ("float".equals(className)) {
                json.writeNumber(Double.parseDouble(scalarNode.getValue()));
            } else if ("null".equals(className)) {
                json.writeNull();
            } else {
                json.writeString(scalarNode.getValue());
            }
        }
    }
}
