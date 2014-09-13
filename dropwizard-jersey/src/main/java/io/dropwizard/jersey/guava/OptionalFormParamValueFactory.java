package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ParamException;
import org.glassfish.jersey.server.internal.InternalServerProperties;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.ExtractorException;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractor;

import javax.ws.rs.Encoded;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OptionalFormParamValueFactory extends AbstractContainerRequestValueFactory<Object> {

    private static final Form EMPTY_FORM = new Form();
    private static final Annotation ENCODED_ANNOTATIONS = getEncodedAnnotation();

    private final MultivaluedParameterExtractor<?> extractor;
    private final boolean decode;

    public OptionalFormParamValueFactory(final MultivaluedParameterExtractor<?> extractor, final boolean decode) {
        this.extractor = extractor;
        this.decode = decode;
    }

    private static Form getCachedForm(final ContainerRequest request, boolean decode) {
        return (Form) request.getProperty(decode ?
                InternalServerProperties.FORM_DECODED_PROPERTY : InternalServerProperties.FORM_PROPERTY);
    }

    private static ContainerRequest ensureValidRequest(final ContainerRequest request) throws IllegalStateException {
        if (request.getMethod().equals("GET")) {
            throw new IllegalStateException(LocalizationMessages.FORM_PARAM_METHOD_ERROR());
        }

        if (!MediaTypes.typeEqual(MediaType.APPLICATION_FORM_URLENCODED_TYPE, request.getMediaType())) {
            throw new IllegalStateException(LocalizationMessages.FORM_PARAM_CONTENT_TYPE_ERROR());
        }
        return request;
    }

    private static Annotation getEncodedAnnotation() {
        /**
         * Encoded-annotated class.
         */
        @Encoded
        final class EncodedAnnotationTemp {
        }
        return EncodedAnnotationTemp.class.getAnnotation(Encoded.class);
    }

    @Override
    public Object provide() {
        final ContainerRequest request = getContainerRequest();
        Form form = getCachedForm(request, decode);

        if (form == null) {
            final Form otherForm = getCachedForm(request, !decode);
            if (otherForm != null) {
                form = switchUrlEncoding(request, otherForm);
                cacheForm(request, form);
            } else {
                form = getForm(request);
                cacheForm(request, form);
            }
        }

        try {
            final Object value = extractor.extract(form.asMap());
            return Optional.fromNullable(value);
        } catch (ExtractorException e) {
            throw new ParamException.FormParamException(e.getCause(), extractor.getName(), extractor.getDefaultValueString());
        }
    }

    private Form switchUrlEncoding(final ContainerRequest request, final Form otherForm) {
        final Set<Map.Entry<String, List<String>>> entries = otherForm.asMap().entrySet();
        final Form newForm = new Form();

        for (Map.Entry<String, List<String>> entry : entries) {
            final String charsetName = ReaderWriter.getCharset(MediaType.valueOf(
                    request.getHeaderString(HttpHeaders.CONTENT_TYPE))).name();

            try {
                final String key = decode ?
                        URLDecoder.decode(entry.getKey(), charsetName) : URLEncoder.encode(entry.getKey(), charsetName);

                for (String value : entry.getValue()) {
                    newForm.asMap().add(key, decode ?
                            URLDecoder.decode(value, charsetName) : URLEncoder.encode(value, charsetName));
                }

            } catch (UnsupportedEncodingException uee) {
                throw new ProcessingException(LocalizationMessages.ERROR_UNSUPPORTED_ENCODING(charsetName,
                        extractor.getName()), uee);
            }
        }
        return newForm;
    }

    private void cacheForm(final ContainerRequest request, final Form form) {
        request.setProperty(decode ?
                InternalServerProperties.FORM_DECODED_PROPERTY : InternalServerProperties.FORM_PROPERTY, form);
    }

    private Form getForm(final ContainerRequest request) {
        return getFormParameters(ensureValidRequest(request));
    }

    private Form getFormParameters(final ContainerRequest request) {
        if (MediaTypes.typeEqual(MediaType.APPLICATION_FORM_URLENCODED_TYPE, request.getMediaType())) {
            request.bufferEntity();

            final Form form;
            if (decode) {
                form = request.readEntity(Form.class);
            } else {
                Annotation[] annotations = new Annotation[1];
                annotations[0] = ENCODED_ANNOTATIONS;
                form = request.readEntity(Form.class, annotations);
            }

            return (form == null ? EMPTY_FORM : form);
        } else {
            return EMPTY_FORM;
        }
    }
}
