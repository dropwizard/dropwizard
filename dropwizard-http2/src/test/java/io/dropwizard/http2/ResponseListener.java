package io.dropwizard.http2;

import com.google.common.base.Charsets;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.util.Callback;

import javax.ws.rs.core.MediaType;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseListener extends Stream.Listener.Adapter {

    private final ByteArrayOutputStream responseByteStream = new ByteArrayOutputStream();
    private final CountDownLatch latch;

    public ResponseListener(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onHeaders(Stream stream, HeadersFrame frame) {
        final MetaData metaData = frame.getMetaData();
        assertThat(metaData.getVersion()).isEqualTo(HttpVersion.HTTP_2);
        assertThat(metaData.getFields().get(HttpHeader.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Override
    public void onData(Stream stream, DataFrame frame, Callback callback) {
        while (frame.getData().hasRemaining()) {
            responseByteStream.write(frame.getData().get());
        }

        callback.succeeded();
        if (frame.isEndStream()) {
            assertThat(new String(responseByteStream.toByteArray(), Charsets.UTF_8))
                    .isEqualTo(FakeApplication.HELLO_WORLD);
            latch.countDown();
        }
    }
}
