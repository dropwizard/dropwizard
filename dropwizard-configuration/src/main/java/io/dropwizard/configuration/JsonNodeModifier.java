package io.dropwizard.configuration;
public class JsonNodeModifier {
    protected void addOverride(JsonNode root, String name, String value) {
        JsonNode node = root;
        final List<String> parts = Arrays.stream(ESCAPED_DOT_SPLIT_PATTERN.split(name))
            .map(String::trim)
            .map(key -> ESCAPED_DOT_PATTERN.matcher(key).replaceAll("."))
            .collect(Collectors.toList());
        for (int i = 0; i < parts.size(); i++) {
            final String key = parts.get(i);

            if (!(node instanceof ObjectNode)) {
                throw new IllegalArgumentException("Unable to override " + name + "; it's not a valid path.");
            }
            final ObjectNode obj = (ObjectNode) node;

            final String remainingPath = String.join(".", parts.subList(i, parts.size()));
            if (obj.has(remainingPath) && !remainingPath.equals(key)
                && obj.get(remainingPath).isValueNode()) {
                obj.put(remainingPath, value);
                return;
            }

            JsonNode child;
            final boolean moreParts = i < parts.size() - 1;

            if (key.matches(".+\\[\\d+\\]$")) {
                final int s = key.indexOf('[');
                final int index = Integer.parseInt(key.substring(s + 1, key.length() - 1));
                child = obj.get(key.substring(0, s));
                if (child == null) {
                    throw new IllegalArgumentException("Unable to override " + name +
                        "; node with index not found.");
                }
                if (!child.isArray()) {
                    throw new IllegalArgumentException("Unable to override " + name +
                        "; node with index is not an array.");
                } else if (index >= child.size()) {
                    throw new ArrayIndexOutOfBoundsException("Unable to override " + name +
                        "; index is greater than size of array.");
                }
                if (moreParts) {
                    child = child.get(index);
                    node = child;
                } else {
                    final ArrayNode array = (ArrayNode) child;
                    array.set(index, TextNode.valueOf(value));
                    return;
                }
            } else if (moreParts) {
                child = obj.get(key);
                if (child == null) {
                    child = obj.objectNode();
                    obj.set(key, child);
                }
                if (child.isArray()) {
                    throw new IllegalArgumentException("Unable to override " + name +
                        "; target is an array but no index specified");
                }
                node = child;
            }

            if (!moreParts) {
                if ((node.get(key) != null && node.get(key).isArray())
                    || (node.get(key) == null && configurationMetadata.isCollectionOfStrings(name))) {
                    ArrayNode arrayNode = (ArrayNode) obj.get(key);
                    if (arrayNode == null) {
                        arrayNode = obj.arrayNode();
                        obj.set(key, arrayNode);
                    }
                    arrayNode.removeAll();
                    Arrays.stream(ESCAPED_COMMA_SPLIT_PATTERN.split(value))
                        .map(String::trim)
                        .map(val -> ESCAPED_COMMA_PATTERN.matcher(val).replaceAll(","))
                        .forEach(arrayNode::add);
                } else {
                    obj.put(key, value);
                }
            }
        }
    }
}
