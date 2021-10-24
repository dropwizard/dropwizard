package io.dropwizard.jersey.jackson;

class JsonProcessingExceptionMapperWithDetailsTest extends JsonProcessingExceptionMapperTest{
    @Override
    boolean showDetails() {
        return true;
    }
}
