package com.github.mrlalonde.zipkinwebbridge;

import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class ZipkinConfiguration {

    @Bean
    ArmeriaServerConfigurator serverConfigurator(
            ZipkinWebBridgeController httpCollector) {
        return sb -> {
            sb.annotatedService(httpCollector);

            // It's common for backend requests to have timeouts of the magic number 10s, so we go ahead
            // and default to a slightly longer timeout on the server to be able to handle these with
            // better error messages where possible.
            sb.requestTimeout(Duration.ofSeconds(11));
        };
    }
}
