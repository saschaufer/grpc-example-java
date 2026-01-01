package de.saschaufer.grpc_example.endpoints;

import de.saschaufer.grpc_example.proto.bidirectional_streaming.BidirectionalStreamingServiceGrpc;
import de.saschaufer.grpc_example.proto.bidirectional_streaming.Input;
import de.saschaufer.grpc_example.proto.bidirectional_streaming.Output;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest
@PropertySource("classpath:application.yml")
class BidirectionalStreamingServiceTest extends BidirectionalStreamingServiceGrpc.BidirectionalStreamingServiceImplBase {

    private static ManagedChannel channel;
    // No blocking stub available, because input is a stream
    private static BidirectionalStreamingServiceGrpc.BidirectionalStreamingServiceStub nonblockingStub;

    @BeforeAll
    static void beforeAll() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        nonblockingStub = BidirectionalStreamingServiceGrpc.newStub(channel);
    }

    @AfterAll
    static void afterAll() {
        channel.shutdown();
    }

    @Test
    void call_positive_nonblockingStub() throws InterruptedException {

        final TestObserver<Output> testObserver = new TestObserver<>(1, true);

        final StreamObserver<Input> inputObserver = nonblockingStub.call(testObserver);

        inputObserver.onNext(Input.newBuilder().setString("AbC").build());
        inputObserver.onNext(Input.newBuilder().setString("dEf").build());
        inputObserver.onNext(Input.newBuilder().setString("123").build());

        inputObserver.onCompleted();

        testObserver.getLatch().await(1, TimeUnit.SECONDS);

        assertThat(testObserver.getError(), nullValue());
        assertThat(testObserver.getResults().size(), is(3));

        assertThat(testObserver.getResults().getFirst().getString(), is("aBc"));
        assertThat(testObserver.getResults().get(1).getString(), is("DeF"));
        assertThat(testObserver.getResults().getLast().getString(), is("123"));
    }
}
