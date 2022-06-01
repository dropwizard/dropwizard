package io.dropwizard.jersey.jackson;

class JsonProcessingExceptionMapperWithoutDetailsTest extends JsonProcessingExceptionMapperTest {
    @Override
    boolean showDetails() {
        return false;
    }
}
