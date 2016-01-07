package io.dropwizard.jersey.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptorContext;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ConfiguredGZipEncoderTest {
    @Test
    public void gzipParametersSpec() throws IOException {
        ClientRequestContext context = mock(ClientRequestContext.class);
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(context.getHeaders()).thenReturn(headers);
        headers.put(HttpHeaders.CONTENT_ENCODING, null);
        when(context.hasEntity()).thenReturn(true);

        new ConfiguredGZipEncoder(true).filter(context);

        assertThat(headers.getFirst(HttpHeaders.CONTENT_ENCODING).toString(), is("gzip"));
    }

    @Test
    public void aroundWriteToSpec() throws IOException, WebApplicationException {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
        WriterInterceptorContextMock context = new WriterInterceptorContextMock(headers);
        new ConfiguredGZipEncoder(true).aroundWriteTo(context);
        assertThat(context.getOutputStream(), is(instanceOf(GZIPOutputStream.class)));
        assertThat(context.isProceedCalled(), is(true));
    }
    @Test
    public void aroundWriteToSpecX_GZip() throws IOException, WebApplicationException {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(HttpHeaders.CONTENT_ENCODING, "x-gzip");
        WriterInterceptorContextMock context = new WriterInterceptorContextMock(headers);
        new ConfiguredGZipEncoder(true).aroundWriteTo(context);
        assertThat(context.getOutputStream(), is(instanceOf(GZIPOutputStream.class)));
        assertThat(context.isProceedCalled(), is(true));
    }
    @Test
    public void otherEncodingWillNotAroundWrite() throws IOException, WebApplicationException {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(HttpHeaders.CONTENT_ENCODING, "someOtherEnc");
        WriterInterceptorContextMock context = new WriterInterceptorContextMock(headers);
        new ConfiguredGZipEncoder(true).aroundWriteTo(context);
        assertThat(context.getOutputStream(), is(not(instanceOf(GZIPOutputStream.class))));
        assertThat(context.isProceedCalled(), is(true));
    }
    @Test
    public void noEncodingwillNotAroundWrite() throws IOException, WebApplicationException {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(HttpHeaders.CONTENT_ENCODING, null);
        WriterInterceptorContextMock context = new WriterInterceptorContextMock(headers);
        new ConfiguredGZipEncoder(true).aroundWriteTo(context);
        assertThat(context.getOutputStream(), is(not(instanceOf(GZIPOutputStream.class))));
        assertThat(context.isProceedCalled(), is(true));
    }


    @Test(expected = NullPointerException.class)
    public void contextMayNotBeNull() throws IOException {
        ClientRequestContext context = null;
        new ConfiguredGZipEncoder(false).filter(context);
    }


    private class WriterInterceptorContextMock implements WriterInterceptorContext {
        private final MultivaluedMap<String, Object> headers;
        private OutputStream os = new OutputStream() {
            @Override
            public void write(int i) throws IOException {
                //void
            }
        };
        private boolean proceedCalled = false;

        public WriterInterceptorContextMock(MultivaluedMap<String, Object> headers) {
            this.headers = headers;
        }

        @Override
        public void proceed() throws IOException, WebApplicationException {
            proceedCalled=true;
        }

        @Override
        public Object getEntity() {
            return null;
        }

        @Override
        public void setEntity(Object entity) {

        }

        @Override
        public OutputStream getOutputStream() {
            return os;
        }

        @Override
        public void setOutputStream(OutputStream os) {
            this.os = os;
        }

        @Override
        public MultivaluedMap<String, Object> getHeaders() {
            return headers;
        }

        @Override
        public Object getProperty(String name) {
            return null;
        }

        @Override
        public Collection<String> getPropertyNames() {
            return null;
        }

        @Override
        public void setProperty(String name, Object object) {

        }

        @Override
        public void removeProperty(String name) {

        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public void setAnnotations(Annotation[] annotations) {

        }

        @Override
        public Class<?> getType() {
            return null;
        }

        @Override
        public void setType(Class<?> type) {

        }

        @Override
        public Type getGenericType() {
            return null;
        }

        @Override
        public void setGenericType(Type genericType) {

        }

        @Override
        public MediaType getMediaType() {
            return null;
        }

        @Override
        public void setMediaType(MediaType mediaType) {

        }

        public boolean isProceedCalled() {
            return proceedCalled;
        }
    }
}
