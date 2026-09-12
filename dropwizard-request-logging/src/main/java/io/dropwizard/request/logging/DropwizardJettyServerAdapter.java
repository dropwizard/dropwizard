package io.dropwizard.request.logging;

import ch.qos.logback.access.common.spi.ServerAdapter;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

import java.util.Map;
import java.util.stream.Collectors;

class DropwizardJettyServerAdapter implements ServerAdapter {
    private final Request request;
    private final Response response;

    public DropwizardJettyServerAdapter(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public long getRequestTimestamp() {
        return Request.getTimeStamp(request);
    }

    @Override
    public long getContentLength() {
        return Response.getContentBytesWritten(response);
    }

    @Override
    public int getStatusCode() {
        return response.getStatus();
    }

    @Override
    public Map<String, String> buildResponseHeaderMap() {
        return response.getHeaders()
            .stream()
            .collect(
                Collectors.groupingBy(HttpField::getName,
                    Collectors.mapping(HttpField::getValue,
                        Collectors.joining(",")))
            );
    }
}
