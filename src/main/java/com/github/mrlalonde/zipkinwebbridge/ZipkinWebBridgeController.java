package com.github.mrlalonde.zipkinwebbridge;

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
    @Autowired
    private WebClient webClient;


    @PostMapping("/api/v2/spans")
    public ResponseEntity<?> uploadSpans(@RequestBody List<Span> spans) {
        return ResponseEntity.ok().build();
    }
}
