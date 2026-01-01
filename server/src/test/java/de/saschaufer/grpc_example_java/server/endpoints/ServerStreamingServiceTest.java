package de.saschaufer.grpc_example_java.server.endpoints;

import de.saschaufer.grpc_example_java.server.config.AppProperties;
import de.saschaufer.grpc_example_java.stubs.proto.server_streaming.Input;
import de.saschaufer.grpc_example_java.stubs.proto.server_streaming.Output;
import de.saschaufer.grpc_example_java.stubs.proto.server_streaming.ServerStreamingServiceGrpc;
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@DirtiesContext
@Import(ServerStreamingServiceTest.TestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ServerStreamingService.class})
class ServerStreamingServiceTest {

    @TempDir
    private static Path tempDir;

    @Autowired
    private ServerStreamingServiceGrpc.ServerStreamingServiceBlockingStub blockingStub;

    @Autowired
    private ServerStreamingServiceGrpc.ServerStreamingServiceStub nonblockingStub;

    @TestConfiguration
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        AppProperties appProperties() {
            return new AppProperties("1234", tempDir);
        }

        @Bean
        @Lazy
        ServerStreamingServiceGrpc.ServerStreamingServiceBlockingStub blockingStub(final GrpcChannelFactory channels, @LocalGrpcPort final int port) {
            return ServerStreamingServiceGrpc.newBlockingStub(channels.createChannel("0.0.0.0:" + port));
        }

        @Bean
        @Lazy
        ServerStreamingServiceGrpc.ServerStreamingServiceStub nonblockingStub(final GrpcChannelFactory channels, @LocalGrpcPort final int port) {
            return ServerStreamingServiceGrpc.newStub(channels.createChannel("0.0.0.0:" + port));
        }
    }

    @Test
    void call_positive_blockingStub() {

        final Iterator<Output> outputList1 = blockingStub.call(Input.newBuilder().build());
        final Iterator<Output> outputList2 = blockingStub.call(Input.newBuilder().build());

        int expected = 0;
        while (outputList1.hasNext()) {
            final Output output = outputList1.next();
            assertThat(output.getNumber(), is(++expected));
        }

        assertThat(expected, is(10));


        expected = 0;
        while (outputList2.hasNext()) {
            final Output output = outputList2.next();
            assertThat(output.getNumber(), is(++expected));
        }

        assertThat(expected, is(10));
    }

    @Test
    void call_positive_nonblockingStub() throws InterruptedException {

        final TestObserver<Output> testObserver = new TestObserver<>(3, true);

        nonblockingStub.call(Input.newBuilder().build(), testObserver);
        nonblockingStub.call(Input.newBuilder().build(), testObserver);
        nonblockingStub.call(Input.newBuilder().build(), testObserver);

        testObserver.getLatch().await(1, TimeUnit.SECONDS);

        assertThat(testObserver.getError(), nullValue());
        assertThat(testObserver.getResults().size(), is(30));

        testObserver.getResults().sort(Comparator.comparingLong(Output::getNumber));

        int num = 0;
        for (int i = 0; i < 30; i += 3) {
            num++;
            assertThat(testObserver.getResults().get(i).getNumber(), is(num));
            assertThat(testObserver.getResults().get(i + 1).getNumber(), is(num));
            assertThat(testObserver.getResults().get(i + 2).getNumber(), is(num));
        }
    }
}
