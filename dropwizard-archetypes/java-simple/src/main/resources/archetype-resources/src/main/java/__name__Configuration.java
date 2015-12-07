package ${package};

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.*;
import javax.validation.constraints.*;

public class ${name}Configuration extends Configuration {

    @NotEmpty
    private String applicationName;

    @JsonProperty
    public String getApplicationName() {
        return applicationName;
    }

    @JsonProperty
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
