package io.dropwizard.jersey.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;

import org.junit.Test;

import io.dropwizard.jersey.errors.ErrorMessage;

public class FuzzyEnumParamConverterProviderTest {
    private final FuzzyEnumParamConverterProvider paramConverterProvider = new FuzzyEnumParamConverterProvider();

    private enum Fuzzy {
        A_1,
        A_2;
    }

    private enum WithToString {
        A_1,
        A_2;

        public String toString() {
            return "<" + this.name() + ">";
        }
    }

    private enum ExplicitFromString {
        A("1"),
        B("2");

        private final String code;

        ExplicitFromString(String code) {
            this.code = code;
        }

        public static ExplicitFromString fromString(String str) {
            for (ExplicitFromString e : ExplicitFromString.values()) {
                if (str.equals(e.code)) {
                    return e;
                }
            }
            return null;
        }
    }

    private enum ExplicitFromStringThrowsWebApplicationException {
        A("1"),
        B("2");

        private final String code;

        ExplicitFromStringThrowsWebApplicationException(String code) {
            this.code = code;
        }

        public static ExplicitFromStringThrowsWebApplicationException fromString(String str) {
            throw new WebApplicationException(Response.status(new Response.StatusType() {
                @Override
                public int getStatusCode() {
                    return 418;
                }

                @Override
                public Response.Status.Family getFamily() {
                    return Response.Status.Family.CLIENT_ERROR;
                }

                @Override
                public String getReasonPhrase() {
                    return "I am a teapot";
                }
            }).build());
        }
    }

    private enum ExplicitFromStringThrowsOtherException {
        A("1"),
        B("2");

        private final String code;

        ExplicitFromStringThrowsOtherException(String code) {
            this.code = code;
        }

        public static ExplicitFromStringThrowsOtherException fromString(String str) {
            throw new RuntimeException("Boo!");
        }
    }

    private enum ExplicitFromStringNonStatic {
        A("1"),
        B("2");

        private final String code;

        ExplicitFromStringNonStatic(String code) {
            this.code = code;
        }

        public ExplicitFromStringNonStatic fromString(String str) {
            for (ExplicitFromStringNonStatic e : ExplicitFromStringNonStatic.values()) {
                if (str.equals(e.code)) {
                    return e;
                }
            }
            return null;
        }
    }

    private enum ExplicitFromStringPrivate {
        A("1"),
        B("2");

        private final String code;

        ExplicitFromStringPrivate(String code) {
            this.code = code;
        }

        private static ExplicitFromStringPrivate fromString(String str) {
            for (ExplicitFromStringPrivate e : ExplicitFromStringPrivate.values()) {
                if (str.equals(e.code)) {
                    return e;
                }
            }
            return null;
        }
    }

    static class Klass {

    }

    @Test
    public void testFuzzyEnum() throws IOException {
        final ParamConverter<Fuzzy> converter =
            paramConverterProvider.getConverter(Fuzzy.class, null, new Annotation[] {});
        assertThat(converter.fromString(null)).isNull();
        assertThat(converter.fromString("A.1")).isSameAs(Fuzzy.A_1);
        assertThat(converter.fromString("A-1")).isSameAs(Fuzzy.A_1);
        assertThat(converter.fromString("A_1")).isSameAs(Fuzzy.A_1);
        assertThat(converter.fromString(" A_1")).isSameAs(Fuzzy.A_1);
        assertThat(converter.fromString("A_1 ")).isSameAs(Fuzzy.A_1);
        assertThat(converter.fromString("A_2")).isSameAs(Fuzzy.A_2);
        assertThatThrownBy(() -> converter.fromString("B"))
            .isInstanceOf(WebApplicationException.class)
            .extracting(e -> (((WebApplicationException)e).getResponse()).getEntity())
            .matches(e -> ((ErrorMessage) e[0]).getCode() == 400)
            .matches(e -> ((ErrorMessage) e[0]).getMessage().contains("A_1"))
            .matches(e -> ((ErrorMessage) e[0]).getMessage().contains("A_2"));
    }

    @Test
    public void testToString() throws IOException {
        final ParamConverter<WithToString> converter =
            paramConverterProvider.getConverter(WithToString.class, null, new Annotation[] {});
        assertThat(converter.toString(WithToString.A_1)).isEqualTo("<A_1>");
    }

    @Test
    public void testNonEnum() throws IOException {
        final ParamConverter<Klass> converter =
            paramConverterProvider.getConverter(Klass.class, null, new Annotation[] {});
        assertThat(converter).isNull();
    }

    @Test
    public void testEnumViaExplicitFromString() throws IOException {
        final ParamConverter<ExplicitFromString> converter =
            paramConverterProvider.getConverter(ExplicitFromString.class, null, new Annotation[] {});
        assertThat(converter.fromString("1")).isSameAs(ExplicitFromString.A);
        assertThat(converter.fromString("2")).isSameAs(ExplicitFromString.B);
        assertThatThrownBy(() -> converter.fromString("3")).isInstanceOf(WebApplicationException.class)
            .extracting(e -> (((WebApplicationException)e).getResponse()).getEntity())
            .matches(e -> ((ErrorMessage) e[0]).getCode() == 400)
            .matches(e -> ((ErrorMessage) e[0]).getMessage().contains("is not a valid ExplicitFromString"));
    }

    @Test
    public void testEnumViaExplicitFromStringThatThrowsWebApplicationException() throws IOException {
        final ParamConverter<ExplicitFromStringThrowsWebApplicationException> converter =
            paramConverterProvider.getConverter(ExplicitFromStringThrowsWebApplicationException.class, null, new Annotation[] {});
        assertThatThrownBy(() -> converter.fromString("3")).isInstanceOf(WebApplicationException.class)
            .extracting(e -> (((WebApplicationException)e).getResponse()).getStatusInfo())
            .matches(e -> ((Response.StatusType) e[0]).getStatusCode() == 418)
            .matches(e -> ((Response.StatusType) e[0]).getReasonPhrase().contains("I am a teapot"));
    }

    @Test
    public void testEnumViaExplicitFromStringThatThrowsOtherException() throws IOException {
        final ParamConverter<ExplicitFromStringThrowsOtherException> converter =
            paramConverterProvider.getConverter(ExplicitFromStringThrowsOtherException.class, null, new Annotation[] {});
        assertThatThrownBy(() -> converter.fromString("1")).isInstanceOf(WebApplicationException.class)
            .extracting(e -> (((WebApplicationException)e).getResponse()).getEntity())
            .matches(e -> ((ErrorMessage) e[0]).getCode() == 400)
            .matches(e -> ((ErrorMessage) e[0]).getMessage().contains("Failed to convert"));
    }

    @Test
    public void testEnumViaExplicitFromStringNonStatic() throws IOException {
        final ParamConverter<ExplicitFromStringNonStatic> converter =
            paramConverterProvider.getConverter(ExplicitFromStringNonStatic.class, null, new Annotation[] {});
        assertThatThrownBy(() -> converter.fromString("1")).isInstanceOf(WebApplicationException.class)
            .extracting(e -> (((WebApplicationException)e).getResponse()).getEntity())
            .matches(e -> ((ErrorMessage) e[0]).getCode() == 400)
            .matches(e -> ((ErrorMessage) e[0]).getMessage().contains("A"))
            .matches(e -> ((ErrorMessage) e[0]).getMessage().contains("B"));

        assertThat(converter.fromString("A")).isSameAs(ExplicitFromStringNonStatic.A);
    }

    @Test
    public void testEnumViaExplicitFromStringPrivate() throws IOException {
        final ParamConverter<ExplicitFromStringPrivate> converter =
            paramConverterProvider.getConverter(ExplicitFromStringPrivate.class, null, new Annotation[] {});
        assertThatThrownBy(() -> converter.fromString("1")).isInstanceOf(WebApplicationException.class)
            .extracting(e -> (((WebApplicationException)e).getResponse()).getEntity())
            .matches(e -> ((ErrorMessage) e[0]).getCode() == 400)
            .matches(e -> ((ErrorMessage) e[0]).getMessage().contains("Not permitted to call"));
    }
}
