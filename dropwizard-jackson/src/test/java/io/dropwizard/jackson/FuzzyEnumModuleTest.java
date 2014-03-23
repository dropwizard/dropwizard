package io.dropwizard.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.sql.ClientInfoStatus;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class FuzzyEnumModuleTest {
    private final ObjectMapper mapper = new ObjectMapper();
    
    private enum EnumWithLowercase { lower_case_enum, mixedCaseEnum }

    private enum EnumWithCreator {
        TEST;

        @JsonCreator
        public static EnumWithCreator fromString(String value) {
            return EnumWithCreator.TEST;
        }
    }

    @Before
    public void setUp() throws Exception {
        mapper.registerModule(new FuzzyEnumModule());
    }

    @Test
    public void mapsUpperCaseEnums() throws Exception {
        assertThat(mapper.readValue("\"SECONDS\"", TimeUnit.class))
                .isEqualTo(TimeUnit.SECONDS);
    }

    @Test
    public void mapsLowerCaseEnums() throws Exception {
        assertThat(mapper.readValue("\"milliseconds\"", TimeUnit.class))
                .isEqualTo(TimeUnit.MILLISECONDS);
    }

    @Test
    public void mapsPaddedEnums() throws Exception {
        assertThat(mapper.readValue("\"   MINUTES \"", TimeUnit.class))
                .isEqualTo(TimeUnit.MINUTES);
    }

    @Test
    public void mapsSpacedEnums() throws Exception {
        assertThat(mapper.readValue("\"   MILLI SECONDS \"", TimeUnit.class))
                .isEqualTo(TimeUnit.MILLISECONDS);
    }

    @Test
    public void mapsDashedEnums() throws Exception {
        assertThat(mapper.readValue("\"REASON-UNKNOWN\"", ClientInfoStatus.class))
                .isEqualTo(ClientInfoStatus.REASON_UNKNOWN);
    }

    @Test
    public void mapsDottedEnums() throws Exception {
        assertThat(mapper.readValue("\"REASON.UNKNOWN\"", ClientInfoStatus.class))
                .isEqualTo(ClientInfoStatus.REASON_UNKNOWN);
    }

    @Test
    public void mapsWhenEnumHasCreator() throws Exception {
        assertThat(mapper.readValue("\"BLA\"", EnumWithCreator.class))
                .isEqualTo(EnumWithCreator.TEST);
    }

    @Test
    public void failsOnIncorrectValue() throws Exception {
        try {
            mapper.readValue("\"wrong\"", TimeUnit.class);
            failBecauseExceptionWasNotThrown(JsonMappingException.class);
        } catch (JsonMappingException e) {
            assertThat(e.getOriginalMessage())
                    .isEqualTo("wrong was not one of [NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS]");
        }
    }
 
    @Test
    public void mapsToLowerCaseEnums() throws Exception {
        assertThat(mapper.readValue("\"lower_case_enum\"", EnumWithLowercase.class))
                .isEqualTo(EnumWithLowercase.lower_case_enum);
    }
    
    @Test
    public void mapsMixedCaseEnums() throws Exception {
        assertThat(mapper.readValue("\"mixedCaseEnum\"", EnumWithLowercase.class))
                .isEqualTo(EnumWithLowercase.mixedCaseEnum);
    }
   
}
