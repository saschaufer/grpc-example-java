package de.saschaufer.grpc_example.config;

import de.saschaufer.grpc_example.interceptor.AuthInterceptor;
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
