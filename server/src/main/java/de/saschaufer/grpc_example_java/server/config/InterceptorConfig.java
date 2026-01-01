package de.saschaufer.grpc_example_java.server.config;

import de.saschaufer.grpc_example_java.server.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.server.GlobalServerInterceptor;

@Configuration
@RequiredArgsConstructor
public class InterceptorConfig {

    private final AppProperties appProperties;

    @Bean
    @GlobalServerInterceptor
    AuthInterceptor authInterceptor() {
        return new AuthInterceptor(appProperties);
    }
}
