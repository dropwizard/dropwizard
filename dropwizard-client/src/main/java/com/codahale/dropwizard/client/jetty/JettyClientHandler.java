package com.codahale.dropwizard.client.jetty;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.TerminatingClientHandler;
import com.sun.jersey.core.header.InBoundHeaders;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpMethod;

import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class JettyClientHandler extends TerminatingClientHandler {
    private final HttpClient client;

    public JettyClientHandler(HttpClient client) {
        this.client = client;
    }

    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        try {
            final Request request = buildRequest(cr);
            writeOutBoundHeaders(cr.getHeaders(), request);
            final ContentResponse response = request.send();
            return new ClientResponse(response.getStatus(),
                                      getInBoundHeaders(response),
                                      new ByteArrayInputStream(response.getContent()),
                                      getMessageBodyWorkers());
        } catch (InterruptedException | TimeoutException | ExecutionException | IOException e) {
            throw new ClientHandlerException(e);
        }
    }

    private InBoundHeaders getInBoundHeaders(Response response) {
        final InBoundHeaders headers = new InBoundHeaders();
        for (HttpField header : response.getHeaders()) {
            headers.add(header.getName(), header.getValue());
        }
        return headers;
    }

    private void writeOutBoundHeaders(MultivaluedMap<String, Object> headers, Request request) {
        for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
            final List<Object> vs = e.getValue();
            if (vs.size() == 1) {
                request.header(e.getKey(), ClientRequest.getHeaderValue(vs.get(0)));
            } else {
                final StringBuilder header = new StringBuilder();
                for (Object v : e.getValue()) {
                    if (header.length() > 0) {
                        header.append(',');
                    }
                    header.append(ClientRequest.getHeaderValue(v));
                }
                request.header(e.getKey(), header.toString());
            }
        }
    }

    private Request buildRequest(ClientRequest req) throws IOException {
        final Request request = client.newRequest(req.getURI())
                                      .method(HttpMethod.fromString(req.getMethod()));
        if (req.getEntity() != null) {
            final RequestEntityWriter writer = getRequestEntityWriter(req);
            final PipedOutputStream output = new PipedOutputStream();
            final PipedInputStream input = new PipedInputStream(output);
            request.content(new InputStreamContentProvider(input));
            writer.writeRequestEntity(output);
        }
        return request;
    }
}
