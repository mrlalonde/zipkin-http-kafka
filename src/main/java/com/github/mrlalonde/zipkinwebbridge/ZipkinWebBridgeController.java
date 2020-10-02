package com.github.mrlalonde.zipkinwebbridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import zipkin2.Span;
import zipkin2.codec.SpanBytesDecoder;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@RestController
public class ZipkinWebBridgeController {
    private static final Logger LOG = LoggerFactory.getLogger(ZipkinWebBridgeController.class);

    private final String topic;

    ZipkinWebBridgeController(@Value("spans.topic") String topic) {
        this.topic = topic;
    }


    @PostMapping("/api/v2/spans")
    public Mono<ResponseEntity<Object>> uploadSpans(@RequestBody byte[] spanBytes) {

        Collection<Span> spans = SpanBytesDecoder.JSON_V2.decodeList(spanBytes);
        spans.forEach( span ->  LOG.info("receivedSpan: {}", span));


        return Mono.just(ResponseEntity.ok().build());
    }
}
