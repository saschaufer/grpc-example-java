package de.saschaufer.grpc_example_java.server.endpoints;

import de.saschaufer.grpc_example_java.server.config.AppProperties;
import de.saschaufer.grpc_example_java.stubs.proto.unary.Input;
import de.saschaufer.grpc_example_java.stubs.proto.unary.Output;
import de.saschaufer.grpc_example_java.stubs.proto.unary.UnaryServiceGrpc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DirtiesContext
@Import(UnaryServiceTest.TestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {UnaryService.class})
class UnaryServiceTest {

    @TempDir
    private static Path tempDir;

    @Autowired
    private UnaryServiceGrpc.UnaryServiceBlockingStub blockingStub;

    @Autowired
    private UnaryServiceGrpc.UnaryServiceStub nonblockingStub;

    @TestConfiguration
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        AppProperties appProperties() {
            return new AppProperties("1234", tempDir);
        }

        @Bean
        @Lazy
        UnaryServiceGrpc.UnaryServiceBlockingStub blockingStub(final GrpcChannelFactory channels, @LocalGrpcPort final int port) {
            return UnaryServiceGrpc.newBlockingStub(channels.createChannel("0.0.0.0:" + port));
        }

        @Bean
        @Lazy
        UnaryServiceGrpc.UnaryServiceStub nonblockingStub(final GrpcChannelFactory channels, @LocalGrpcPort final int port) {
            return UnaryServiceGrpc.newStub(channels.createChannel("0.0.0.0:" + port));
        }
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
