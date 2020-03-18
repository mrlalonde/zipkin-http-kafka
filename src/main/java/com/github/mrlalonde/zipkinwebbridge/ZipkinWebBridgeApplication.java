package com.github.mrlalonde.zipkinwebbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
public class ZipkinWebBridgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZipkinWebBridgeApplication.class, args);
    }
}
