package com.github.mrlalonde.zipkinwebbridge;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import zipkin2.Span;
import zipkin2.codec.SpanBytesEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class TestingApp {

    public static void main(String[] args) throws IOException {
        Tester tester = new Tester();
        List<Span> spans = new ArrayList<>();
        spans.add(Span.newBuilder()
                        .id(1L)
                        .traceId("2")
                        .timestamp(System.currentTimeMillis())
                        .putTag("foo", "bar")
                        .build());
        spans.add(Span.newBuilder()
                .id(1L)
                .traceId("2")
                .timestamp(System.currentTimeMillis())
                .putTag("foo", "bar")
                .build());

        ClientResponse clientResponse = tester.test(spans);
        if (!clientResponse.statusCode().is2xxSuccessful()) {
            throw new RuntimeException("Status code is: " + clientResponse.statusCode());
        }

        System.out.println("Success!");
    }



    private static final class Tester {
        private final WebClient webClient = WebClient.create();

        ClientResponse test(List<Span> spans) throws IOException {
            return webClient.post()
                    .uri("http://localhost:8080/api/v2/spans")
                    .bodyValue(toGzip(spans))
                    .exchange()
                    .block();
        }
        private byte[] toGzip(List<Span> spans) throws IOException {
            byte[] raw = SpanBytesEncoder.JSON_V2.encodeList(spans);
            try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); GZIPOutputStream gzipOut = new GZIPOutputStream(bout)) {
                gzipOut.write(raw);
                gzipOut.finish();
                return bout.toByteArray();
            }
        }

    }
}
