package com.github.mrlalonde.zipkinwebbridge;

import org.junit.jupiter.api.Test;
import zipkin2.Span;
import zipkin2.codec.SpanBytesDecoder;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpanTest {
    @Test
    public void t() {
        Span span = Span.newBuilder()
                .id(1L)
                .traceId("2")
                .timestamp(System.currentTimeMillis())
                .putTag("foo", "bar")
                .build();

        String json = span.toString();
        System.out.println(json);
        Span spanDeser = SpanBytesDecoder.JSON_V2.decodeOne(json.getBytes(Charset.defaultCharset()));

        assertEquals(span, spanDeser);
    }
}
