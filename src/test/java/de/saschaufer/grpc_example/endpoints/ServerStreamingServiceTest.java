package de.saschaufer.grpc_example.endpoints;

import de.saschaufer.grpc_example.proto.server_streaming.Input;
import de.saschaufer.grpc_example.proto.server_streaming.Output;
import de.saschaufer.grpc_example.proto.server_streaming.ServerStreamingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest
@PropertySource("classpath:application.yml")
class ServerStreamingServiceTest {

    private static ManagedChannel channel;
    private static ServerStreamingServiceGrpc.ServerStreamingServiceBlockingStub blockingStub;
    private static ServerStreamingServiceGrpc.ServerStreamingServiceStub nonblockingStub;

    @BeforeAll
    static void beforeAll() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        blockingStub = ServerStreamingServiceGrpc.newBlockingStub(channel);
        nonblockingStub = ServerStreamingServiceGrpc.newStub(channel);
    }

    @AfterAll
    static void afterAll() {
        channel.shutdown();
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
