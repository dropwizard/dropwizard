package io.dropwizard.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
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

    private enum EnumWithLowercase {lower_case_enum, mixedCaseEnum}

    private enum EnumWithCreator {
        TEST;

        @JsonCreator
        public static EnumWithCreator fromString(String value) {
            return EnumWithCreator.TEST;
        }
    }

    private enum CurrencyCode {
        USD("United States dollar"),
        AUD("a_u_d"),
        CAD("c-a-d"),
        BLA("b.l.a"),
        EUR("Euro"),
        GBP("Pound sterling");

        private final String description;

        CurrencyCode(String name) {
            this.description = name;
        }

        @Override
        public String toString() {
            return description;
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

    @Test
    public void readsEnumsUsingToString() throws Exception {
        final ObjectMapper toStringEnumsMapper = mapper.copy()
                .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        assertThat(toStringEnumsMapper.readValue("\"Pound sterling\"", CurrencyCode.class)).isEqualTo(CurrencyCode.GBP);
    }
    
    @Test
    public void readsEnumsUsingToStringWithDeserializationFeatureOff() throws Exception {
        assertThat(mapper.readValue("\"Pound sterling\"", CurrencyCode.class)).isEqualTo(CurrencyCode.GBP);
        assertThat(mapper.readValue("\"a_u_d\"", CurrencyCode.class)).isEqualTo(CurrencyCode.AUD);
        assertThat(mapper.readValue("\"c-a-d\"", CurrencyCode.class)).isEqualTo(CurrencyCode.CAD);
        assertThat(mapper.readValue("\"b.l.a\"", CurrencyCode.class)).isEqualTo(CurrencyCode.BLA);
    }    
}
