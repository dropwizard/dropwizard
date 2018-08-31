package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;
import java.lang.annotation.Annotation;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

public class FuzzyEnumParamConverterProviderTest {
    private final FuzzyEnumParamConverterProvider paramConverterProvider = new FuzzyEnumParamConverterProvider();

    private enum Fuzzy {
        A_1,
        A_2;
    }

    private enum WithToString {
        A_1,
        A_2;

        @Override
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

        @Nullable
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

        @SuppressWarnings("unused")
        public String getCode() {
            return this.code;
        }

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
        public String getCode() {
            return this.code;
        }

        @SuppressWarnings("unused")
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

        @Nullable
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

        @Nullable
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

    private <T> ParamConverter<T> getConverter(Class<T> rawType) {
        return requireNonNull(paramConverterProvider.getConverter(rawType, null, new Annotation[] {}));
    }

    @Test
    public void testFuzzyEnum() {
        final ParamConverter<Fuzzy> converter = getConverter(Fuzzy.class);
        assertThat(converter.fromString(null)).isNull();
        assertThat(converter.fromString("A.1")).isSameAs(Fuzzy.A_1);
        assertThat(converter.fromString("A-1")).isSameAs(Fuzzy.A_1);
        assertThat(converter.fromString("A_1")).isSameAs(Fuzzy.A_1);
        assertThat(converter.fromString(" A_1")).isSameAs(Fuzzy.A_1);
        assertThat(converter.fromString("A_1 ")).isSameAs(Fuzzy.A_1);
        assertThat(converter.fromString("A_2")).isSameAs(Fuzzy.A_2);

        final WebApplicationException throwable = catchThrowableOfType(() -> converter.fromString("B"), WebApplicationException.class);
        assertThat(throwable.getResponse())
            .extracting(Response::getEntity)
            .matches(e -> ((ErrorMessage) e.get(0)).getCode() == 400)
            .matches(e -> ((ErrorMessage) e.get(0)).getMessage().contains("A_1"))
            .matches(e -> ((ErrorMessage) e.get(0)).getMessage().contains("A_2"));
    }


    @Test
    public void testToString() {
        final ParamConverter<WithToString> converter = getConverter(WithToString.class);
        assertThat(converter.toString(WithToString.A_1)).isEqualTo("<A_1>");
    }

    @Test
    public void testNonEnum() {
        assertThat(paramConverterProvider.getConverter(Klass.class, null, new Annotation[] {})).isNull();
    }

    @Test
    public void testEnumViaExplicitFromString() {
        final ParamConverter<ExplicitFromString> converter = getConverter(ExplicitFromString.class);
        assertThat(converter.fromString("1")).isSameAs(ExplicitFromString.A);
        assertThat(converter.fromString("2")).isSameAs(ExplicitFromString.B);
        final WebApplicationException throwable = catchThrowableOfType(() -> converter.fromString("3"), WebApplicationException.class);
        assertThat(throwable.getResponse())
            .extracting(Response::getEntity)
            .matches(e -> ((ErrorMessage) e.get(0)).getCode() == 400)
            .matches(e -> ((ErrorMessage) e.get(0)).getMessage().contains("is not a valid ExplicitFromString"));
    }

    @Test
    public void testEnumViaExplicitFromStringThatThrowsWebApplicationException() {
        final ParamConverter<ExplicitFromStringThrowsWebApplicationException> converter =
            getConverter(ExplicitFromStringThrowsWebApplicationException.class);
        final WebApplicationException throwable = catchThrowableOfType(() -> converter.fromString("3"), WebApplicationException.class);
        assertThat(throwable.getResponse())
            .extracting(Response::getStatusInfo)
            .matches(e -> ((Response.StatusType) e.get(0)).getStatusCode() == 418)
            .matches(e -> ((Response.StatusType) e.get(0)).getReasonPhrase().contains("I am a teapot"));
    }

    @Test
    public void testEnumViaExplicitFromStringThatThrowsOtherException() {
        final ParamConverter<ExplicitFromStringThrowsOtherException> converter =
            getConverter(ExplicitFromStringThrowsOtherException.class);
        final WebApplicationException throwable = catchThrowableOfType(() -> converter.fromString("1"), WebApplicationException.class);
        assertThat(throwable.getResponse())
            .extracting(Response::getEntity)
            .matches(e -> ((ErrorMessage) e.get(0)).getCode() == 400)
            .matches(e -> ((ErrorMessage) e.get(0)).getMessage().contains("Failed to convert"));
    }

    @Test
    public void testEnumViaExplicitFromStringNonStatic() {
        final ParamConverter<ExplicitFromStringNonStatic> converter = getConverter(ExplicitFromStringNonStatic.class);
        final WebApplicationException throwable = catchThrowableOfType(() -> converter.fromString("1"), WebApplicationException.class);
        assertThat(throwable.getResponse())
            .extracting(Response::getEntity)
            .matches(e -> ((ErrorMessage) e.get(0)).getCode() == 400)
            .matches(e -> ((ErrorMessage) e.get(0)).getMessage().contains("A"))
            .matches(e -> ((ErrorMessage) e.get(0)).getMessage().contains("B"));

        assertThat(converter.fromString("A")).isSameAs(ExplicitFromStringNonStatic.A);
    }

    @Test
    public void testEnumViaExplicitFromStringPrivate() {
        final ParamConverter<ExplicitFromStringPrivate> converter = getConverter(ExplicitFromStringPrivate.class);
        final WebApplicationException throwable = catchThrowableOfType(() -> converter.fromString("1"), WebApplicationException.class);
        assertThat(throwable.getResponse())
            .extracting(Response::getEntity)
            .matches(e -> ((ErrorMessage) e.get(0)).getCode() == 400)
            .matches(e -> ((ErrorMessage) e.get(0)).getMessage().contains("Not permitted to call"));
    }
}
