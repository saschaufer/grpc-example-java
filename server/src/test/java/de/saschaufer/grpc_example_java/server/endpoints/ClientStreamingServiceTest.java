package de.saschaufer.grpc_example_java.server.endpoints;

import de.saschaufer.grpc_example_java.server.config.AppProperties;
import de.saschaufer.grpc_example_java.stubs.proto.client_streaming.ClientStreamingServiceGrpc;
import de.saschaufer.grpc_example_java.stubs.proto.client_streaming.Input;
import de.saschaufer.grpc_example_java.stubs.proto.client_streaming.Output;
import io.grpc.stub.StreamObserver;
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
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@DirtiesContext
@Import(ClientStreamingServiceTest.TestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ClientStreamingService.class})
class ClientStreamingServiceTest {

    @TempDir
    private static Path tempDir;

    @Autowired
    private ClientStreamingServiceGrpc.ClientStreamingServiceStub nonblockingStub;

    @TestConfiguration
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        AppProperties appProperties() {
            return new AppProperties("1234", tempDir);
        }

        @Bean
        @Lazy
        ClientStreamingServiceGrpc.ClientStreamingServiceStub nonblockingStub(final GrpcChannelFactory channels, @LocalGrpcPort final int port) {
            return ClientStreamingServiceGrpc.newStub(channels.createChannel("0.0.0.0:" + port));
        }
    }

    @Test
    void call_positive_nonblockingStub() throws InterruptedException {

        final TestObserver<Output> testObserver = new TestObserver<>(1, true);

        final StreamObserver<Input> inputObserver = nonblockingStub.call(testObserver);

        inputObserver.onNext(Input.newBuilder().setString("abc").build());
        inputObserver.onNext(Input.newBuilder().setString("DEF").build());
        inputObserver.onNext(Input.newBuilder().setString("123").build());

        inputObserver.onCompleted();

        testObserver.getLatch().await(1, TimeUnit.SECONDS);

        assertThat(testObserver.getError(), nullValue());
        assertThat(testObserver.getResults().size(), is(1));

        assertThat(testObserver.getResults().getFirst().getString(), is("abcDEF123"));
    }
}
