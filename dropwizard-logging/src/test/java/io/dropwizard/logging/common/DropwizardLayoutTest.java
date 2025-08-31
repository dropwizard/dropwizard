package io.dropwizard.logging.common;

import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DropwizardLayoutTest {
    private final LoggerContext context = mock();
    private final DropwizardLayout layout = new DropwizardLayout(context, TimeZone.getTimeZone("UTC"));

    @Test
    void prefixesThrowables() {
        assertThat(layout.getDefaultConverterSupplierMap())
                .hasEntrySatisfying("dwEx", supplier ->
                    assertThat(supplier.get()).isInstanceOf(PrefixedThrowableProxyConverter.class));
    }

    @Test
    void prefixesExtendedThrowables() {
        assertThat(layout.getDefaultConverterSupplierMap())
                .hasEntrySatisfying("dwXEx", supplier ->
                    assertThat(supplier.get()).isInstanceOf(PrefixedExtendedThrowableProxyConverter.class));
    }

    @Test
    void hasAContext() {
        assertThat(layout.getContext())
                .isEqualTo(context);
    }

    @Test
    void hasAPatternWithATimeZoneAndExtendedThrowables() {
        assertThat(layout.getPattern())
                .isEqualTo("%-5p [%d{ISO8601,UTC}] %c: %m%n%dwREx");
    }
}
