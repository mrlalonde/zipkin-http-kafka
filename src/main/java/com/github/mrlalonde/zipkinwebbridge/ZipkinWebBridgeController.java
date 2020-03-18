package com.github.mrlalonde.zipkinwebbridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import zipkin2.Span;

import java.util.List;

@RestController
public class ZipkinWebBridgeController {
    private static final Logger LOG = LoggerFactory.getLogger(ZipkinWebBridgeController.class);

    @Autowired
    private WebClient webClient;

    @PostMapping("/api/v2/spans")
    public ResponseEntity<?> uploadSpans(@RequestBody List<Span> spans) {
        for (Span span : spans) {
            LOG.info("Got span (of {}) {}", spans.size(), span);
        }
        return ResponseEntity.ok().build();
    }
}
