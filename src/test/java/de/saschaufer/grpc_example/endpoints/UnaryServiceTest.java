package de.saschaufer.grpc_example.endpoints;

import de.saschaufer.grpc_example.proto.unary.Input;
import de.saschaufer.grpc_example.proto.unary.Output;
import de.saschaufer.grpc_example.proto.unary.UnaryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;

import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@PropertySource("classpath:application.yml")
class UnaryServiceTest {

    private static ManagedChannel channel;
    private static UnaryServiceGrpc.UnaryServiceBlockingStub blockingStub;
    private static UnaryServiceGrpc.UnaryServiceStub nonblockingStub;

    @BeforeAll
    static void beforeAll() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        blockingStub = UnaryServiceGrpc.newBlockingStub(channel);
        nonblockingStub = UnaryServiceGrpc.newStub(channel);
    }

    @AfterAll
    static void afterAll() {
        channel.shutdown();
    }

    @ParameterizedTest
    @CsvSource({"1,2,hallo,ollah", "2,4,Abc,cbA", "10,20,123,321"})
    void call_positive_blockingStub(final int number, final long numExpected, final String string, final String stringExpected) {

        final Input input = Input.newBuilder().setNumber(number).setString(string).build();

        final Output output = blockingStub.call(input);

        assertThat(output.getNumber(), is(numExpected));
        assertThat(output.getString(), is(stringExpected));
    }

    @Test
    void call_positive_blockingStub_defaultValues() {

        // Default value for integer is 0 if not set
        // Default value for string is an empty string if not set

        final Input input = Input.newBuilder().build();

        final Output output = blockingStub.call(input);

        assertThat(output.getNumber(), is(0L));
        assertThat(output.getString(), emptyString());
    }

    @Test
    void call_positive_nonblockingStub() throws InterruptedException {

        final TestObserver<Output> testObserver = new TestObserver<>(3, true);

        nonblockingStub.call(Input.newBuilder().setNumber(1).setString("ab").build(), testObserver);
        nonblockingStub.call(Input.newBuilder().setNumber(2).setString("cd").build(), testObserver);
        nonblockingStub.call(Input.newBuilder().setNumber(3).setString("ef").build(), testObserver);

        testObserver.getLatch().await(1, TimeUnit.SECONDS);

        assertThat(testObserver.getError(), nullValue());
        assertThat(testObserver.getResults().size(), is(3));

        testObserver.getResults().sort(Comparator.comparingLong(Output::getNumber));

        assertThat(testObserver.getResults().getFirst().getNumber(), is(2L));
        assertThat(testObserver.getResults().getFirst().getString(), is("ba"));

        assertThat(testObserver.getResults().get(1).getNumber(), is(4L));
        assertThat(testObserver.getResults().get(1).getString(), is("dc"));

        assertThat(testObserver.getResults().getLast().getNumber(), is(6L));
        assertThat(testObserver.getResults().getLast().getString(), is("fe"));
    }
}
