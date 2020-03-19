package com.github.mrlalonde.zipkinwebbridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RestController
public class ZipkinWebBridgeController {
    private static final Logger LOG = LoggerFactory.getLogger(ZipkinWebBridgeController.class);

    private final String topic;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    ZipkinWebBridgeController(@Value("spans.topic") String topic, KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }


    @PostMapping("/api/v2/spans")
    public Mono<ResponseEntity<Object>> uploadSpans(@RequestBody byte[] spanBytes) {
        return Mono.fromFuture(publish(spanBytes))
                .map(ignoredSuccess -> ResponseEntity.ok().build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    private CompletableFuture<SendResult<String, byte[]>> publish(byte[] spanBytes) {
        return kafkaTemplate.send(topic, spanBytes).completable();
    }
}
