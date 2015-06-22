package io.dropwizard.jersey.validation;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;

public class ConstraintViolationExceptionMapperTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return DropwizardResourceConfig.forTesting(new MetricRegistry())
                .packages("io.dropwizard.jersey.validation");
    }

    @Test
    public void postInvalidEntityIs422() throws Exception {
        assumeThat(Locale.getDefault().getLanguage(), is("en"));

        final Response response = target("/valid/foo").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("{}", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class)).isEqualTo("{\"errors\":[\"name may not be empty\"]}");
    }

    @Test
    public void postNullEntityIs422() throws Exception {
        final Response response = target("/valid/foo").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(null, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(422);

        String ret = "{\"errors\":[\"The request entity was empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidReturnIs500() throws Exception {
        // return value is too long and so will fail validation
        final Response response = target("/valid/bar")
                .queryParam("name", "dropwizard").request().get();
        assertThat(response.getStatus()).isEqualTo(500);

        String ret = "{\"errors\":[\"server response length must be between 0 and 3\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidQueryParamsIs400() throws Exception {
        // query parameter is too short and so will fail validation
        final Response response = target("/valid/bar")
                .queryParam("name", "hi").request().get();

        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"query param name length must be between 3 and 2147483647\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);

        // Send another request to trigger reflection cache
        final Response cache = target("/valid/bar")
                .queryParam("name", "hi").request().get();
        assertThat(cache.getStatus()).isEqualTo(400);
        assertThat(cache.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidCustomTypeIs400() throws Exception {
        // query parameter is too short and so will fail validation
        final Response response = target("/valid/barter")
                .queryParam("name", "hi").request().get();

        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"query param name length must be between 3 and 2147483647\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidBeanParamsIs400() throws Exception {
        // bean parameter is too short and so will fail validation
        final Response response = target("/valid/zoo")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"query param name may not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidHeaderParamsIs400() throws Exception {
        final Response response = target("/valid/head")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"header cheese may not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidCookieParamsIs400() throws Exception {
        final Response response = target("/valid/cooks")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"cookie user_id may not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidPathParamsIs400() throws Exception {
        final Response response = target("/valid/goods/11")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"path param id not a well-formed email address\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidFormParamsIs400() throws Exception {
        final Response response = target("/valid/form")
                .request().post(Entity.form(new Form()));
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"form field username may not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void postInvalidMethodClassIs422() throws Exception {
        final Response response = target("/valid/nothing")
                .request().post(Entity.entity("{}", MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(422);

        String ret = "{\"errors\":[\"must have a false thing\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidNestedReturnIs500() throws Exception {
        final Response response = target("/valid/nested").request().get();
        assertThat(response.getStatus()).isEqualTo(500);

        String ret = "{\"errors\":[\"server response representation.name may not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidNested2ReturnIs500() throws Exception {
        final Response response = target("/valid/nested2").request().get();
        assertThat(response.getStatus()).isEqualTo(500);

        String ret = "{\"errors\":[\"server response example must have a false thing\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidContextIs400() throws Exception {
        final Response response = target("/valid/context").request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"context may not be null\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidMatrixParamIs400() throws Exception {
        final Response response = target("/valid/matrix")
                .matrixParam("bob", "").request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"matrix param bob may not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void functionWithSameNameReturnDifferentErrors() throws Exception {
        // This test is to make sure that functions with the same name and
        // number of parameters (but different parameter types), don't return
        // the same validation error due to any caching effects
        final Response response = target("/valid/head")
                .request().get();

        String ret = "{\"errors\":[\"header cheese may not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);

        final Response response2 = target("/valid/headCopy")
                .request().get();
        String ret2 = "{\"errors\":[\"query param cheese may not be null\"]}";
        assertThat(response2.readEntity(String.class)).isEqualTo(ret2);
    }
}
