package io.dropwizard.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NullAway")
class Issue3796Test {
    @Test
    void configurationWithCustomDeserializerCanBeRead() throws IOException, ConfigurationException {
        final ConfigurationFactory<CustomConfiguration> factory = new YamlConfigurationFactory<>(CustomConfiguration.class, BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw");
        final CustomConfiguration testObject = factory.build(new ResourceConfigurationSourceProvider(), "issue-3796.yml");

        assertThat(testObject).isNotNull();
        assertThat(testObject.customProperty).isNotNull();
        assertThat(testObject.customProperty.customString).isEqualTo("hello, world");
    }

    static class CustomConfiguration {
        @Nullable
        public CustomProperty customProperty;
    }

    @JsonDeserialize(using = CustomDeserializer.class)
    static class CustomProperty {
        final String customString;

        CustomProperty(String customString) {
            this.customString = customString;
        }
    }

    static class CustomDeserializer extends StdDeserializer<CustomProperty> {
        public CustomDeserializer() {
            super(CustomProperty.class);
        }

        @Override
        public CustomProperty deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            assertThat(parser.getCodec()).isNotNull();

            TreeNode treeNode = parser.readValueAsTree();
            final TextNode custom = (TextNode) treeNode.path("custom");
            return new CustomProperty(custom.asText());
        }
    }
}
