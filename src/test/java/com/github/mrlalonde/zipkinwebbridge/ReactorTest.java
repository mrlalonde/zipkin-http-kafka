package com.github.mrlalonde.zipkinwebbridge;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReactorTest {
    @Test
    public void test() {
        Flux<String> flux = Flux.fromArray(new String[] {"a", "b"});
        Mono<String> mono = Mono.from(flux);
        assertEquals("a", mono.block());
    }
}
