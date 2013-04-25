package com.codahale.dropwizard.jetty;

import com.codahale.dropwizard.validation.ConstraintViolations;
import com.google.common.collect.ImmutableList;
import org.eclipse.jetty.server.handler.ErrorHandler;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * An {@link ErrorHandler} subclass which doesn't identify itself as a Jetty server.
 */
public class UnbrandedErrorHandler extends ErrorHandler {
    public UnbrandedErrorHandler() {
        super();
        setShowStacks(false);
    }

    @Override
    public void writeErrorPage(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException {
        super.writeErrorPage(request, writer, code, message, showStacks);
    }

    @Override
    protected void writeErrorPageBody(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException {
        final String uri = request.getRequestURI();
        writeErrorPageMessage(request, writer, code, message, uri);
        if (showStacks) {
            writeErrorPageStacks(request, writer);
        }



        for (int i= 0; i < 20; i++) {
            writer.write("<br/>                                                \n");
        }
    }

    public void writeValidationErrorPage(HttpServletRequest request, StringWriter writer, ConstraintViolationException exception) throws IOException {
        writer.write("<html>\n<head>\n");
        writeErrorPageHead(request, writer, 422, "Unprocessable Entity");
        writer.write("</head>\n<body>");
        writeInvalidationErrorPageBody(request,
                                       writer,
                                       exception.getMessage(),
                                       ConstraintViolations.formatUntyped(exception.getConstraintViolations()));
        writer.write("\n</body>\n</html>\n");
    }

    private void writeInvalidationErrorPageBody(HttpServletRequest request, StringWriter writer, String message, ImmutableList<String> errors) throws IOException {
        final String uri = request.getRequestURI();
        writeErrorPageMessage(request, writer, 422, "Unprocessable Entity", uri);

        writer.write("<h2>");
        write(writer, message);
        writer.write("</h2>");

        writer.write("<ul>");
        for (String error : errors) {
            writer.write("<li>");
            write(writer, error);
            writer.write("</li>");
        }
        writer.write("</ul>");
    }
}
