package com.yammer.dropwizard.json;

import com.yammer.dropwizard.logging.Log;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.type.TypeReference;
import org.yaml.snakeyaml.nodes.*;

import java.io.*;

public class Yaml {
    private static final Log LOG = Log.forClass(Yaml.class);
    private final JsonNode node;

    public Yaml(File file) throws IOException {
        final FileReader reader = new FileReader(file);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final JsonGenerator json = Json.factory.createJsonGenerator(output).useDefaultPrettyPrinter();
        try {
            final Node yaml = new org.yaml.snakeyaml.Yaml().compose(reader);
            build(yaml, json);
            json.close();
            LOG.debug("Parsed {} as:\n {}", file, output.toString());
            this.node = Json.read(output.toByteArray(), JsonNode.class);
        } finally {
            reader.close();
        }
    }

    private static void build(Node yaml, JsonGenerator json) throws IOException {
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
            } else {
                json.writeString(scalarNode.getValue());
            }
        }
    }

    public <T> T read(Class<T> klass) throws IOException {
        return Json.read(node, klass);
    }

    public <T> T read(TypeReference<T> ref) throws IOException {
        return Json.read(node, ref);
    }
}
