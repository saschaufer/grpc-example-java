package de.saschaufer.grpc_example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@Validated
@ConfigurationProperties("app")
public record AppProperties(
        String password,
        Path filesPath
) {
}
