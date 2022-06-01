package io.dropwizard.servlets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServletsTest {
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletRequest fullRequest = mock(HttpServletRequest.class);

    @BeforeEach
    void setUp() throws Exception {
        when(request.getRequestURI()).thenReturn("/one/two");
        when(fullRequest.getRequestURI()).thenReturn("/one/two");
        when(fullRequest.getQueryString()).thenReturn("one=two&three=four");
    }

    @Test
    void formatsBasicURIs() throws Exception {
        assertThat(Servlets.getFullUrl(request)).isEqualTo("/one/two");
    }

    @Test
    void formatsFullURIs() throws Exception {
        assertThat(Servlets.getFullUrl(fullRequest)).isEqualTo("/one/two?one=two&three=four");
    }
}
