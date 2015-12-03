package io.dropwizard.jersey.jackson;

/**
 * Jackson has less insight on how to serialize/deserialize an object that
 * doesn't adhere to the Bean spec. This class needs additional annotations in
 * order to be properly serialized/deserialized.
 */
public class NonBeanImplementation {
    public Integer val() {
        return 1;
    }
}
