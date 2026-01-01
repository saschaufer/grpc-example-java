package de.saschaufer.grpc_example.endpoints;

import de.saschaufer.grpc_example.interceptor.AuthInterceptor;
import de.saschaufer.grpc_example.proto.auth.AuthServiceGrpc;
import de.saschaufer.grpc_example.proto.auth.Input;
import de.saschaufer.grpc_example.proto.auth.Output;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@PropertySource("classpath:application.yml")
class AuthServiceTest {

    private static ManagedChannel channel;
    private static AuthServiceGrpc.AuthServiceBlockingStub stub;

    @BeforeAll
    static void beforeAll() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        stub = AuthServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    static void afterAll() {
        channel.shutdown();
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

        final StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> stub.withCallCredentials(passwordCredentials).call(input));

        assertThat(e.getStatus().getCode(), is(Status.UNAUTHENTICATED.getCode()));
        assertThat(e.getStatus().getDescription(), is("Password wrong"));
    }

    @RequiredArgsConstructor
    private static class PasswordCredentials extends CallCredentials {

        private final String password;

        @Override
        public void applyRequestMetadata(final CallCredentials.RequestInfo requestInfo, final Executor executor, final CallCredentials.MetadataApplier metadataApplier) {
            executor.execute(() -> {
                final Metadata metadata = new Metadata();
                metadata.put(AuthInterceptor.AUTHORIZATION, password);
                metadataApplier.apply(metadata);
            });
        }
    }
}
