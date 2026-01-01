package de.saschaufer.grpc_example_java.server.endpoints;

import de.saschaufer.grpc_example_java.server.config.AppProperties;
import de.saschaufer.grpc_example_java.server.config.InterceptorConfig;
import de.saschaufer.grpc_example_java.server.interceptor.AuthInterceptor;
import de.saschaufer.grpc_example_java.stubs.proto.auth.AuthServiceGrpc;
import de.saschaufer.grpc_example_java.stubs.proto.auth.Input;
import de.saschaufer.grpc_example_java.stubs.proto.auth.Output;
import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.grpc.test.autoconfigure.LocalGrpcPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.test.annotation.DirtiesContext;

import java.nio.file.Path;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DirtiesContext
@Import(AuthServiceTest.TestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {InterceptorConfig.class, AuthService.class})
class AuthServiceTest {

    @TempDir
    private static Path tempDir;

    @Autowired
    private AuthServiceGrpc.AuthServiceBlockingStub stub;

    @TestConfiguration
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        AppProperties appProperties() {
            return new AppProperties("1234", tempDir);
        }

        @Bean
        @Lazy
        AuthServiceGrpc.AuthServiceBlockingStub stub(final GrpcChannelFactory channels, @LocalGrpcPort final int port) {
            return AuthServiceGrpc.newBlockingStub(channels.createChannel("0.0.0.0:" + port));
        }
    }

    @Test
    void call_positive() {

        final Input input = Input.newBuilder().setString("World").build();
        final CallCredentials passwordCredentials = new PasswordCredentials("1234");

        final Output output = stub.withCallCredentials(passwordCredentials).call(input);

        assertThat(output.getString(), is("Hello World"));
    }

    @Test
    void call_negative_PasswordWrong() {

        final Input input = Input.newBuilder().setString("World").build();
        final CallCredentials passwordCredentials = new PasswordCredentials("4321");
        final AuthServiceGrpc.AuthServiceBlockingStub stubWithCredentials = stub.withCallCredentials(passwordCredentials);

        final StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> stubWithCredentials.call(input));

        assertThat(e.getStatus().getCode(), is(Status.UNAUTHENTICATED.getCode()));
        assertThat(e.getStatus().getDescription(), is("Password wrong"));
    }

    @RequiredArgsConstructor
    private static class PasswordCredentials extends CallCredentials {

        private final String password;

        @Override
        public void applyRequestMetadata(final RequestInfo requestInfo, final Executor executor, final MetadataApplier metadataApplier) {
            executor.execute(() -> {
                final Metadata metadata = new Metadata();
                metadata.put(AuthInterceptor.AUTHORIZATION, password);
                metadataApplier.apply(metadata);
            });
        }
    }
}
