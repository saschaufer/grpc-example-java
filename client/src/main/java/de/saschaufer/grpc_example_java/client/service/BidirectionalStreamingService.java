package de.saschaufer.grpc_example_java.client.service;

import de.saschaufer.grpc_example_java.client.config.AppProperties;
import de.saschaufer.grpc_example_java.stubs.proto.bidirectional_streaming.BidirectionalStreamingServiceGrpc;
import de.saschaufer.grpc_example_java.stubs.proto.bidirectional_streaming.Input;
import de.saschaufer.grpc_example_java.stubs.proto.bidirectional_streaming.Output;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BidirectionalStreamingService extends GrpcService {

    private final BidirectionalStreamingServiceGrpc.BidirectionalStreamingServiceStub stub;

    public BidirectionalStreamingService(final AppProperties appProperties, final GrpcChannelFactory channelFactory) {
        super(appProperties, channelFactory);
        stub = BidirectionalStreamingServiceGrpc.newStub(getChannel());
    }

    @Override
    public void run() {

        final StreamObserver<Input> input = stub.call(new StreamObserver<>() {

            @Override
            public void onNext(final Output output) {
                log.atInfo().setMessage("Received response").addKeyValue("output", output).log();
            }

            @Override
            public void onError(final Throwable throwable) {
                log.atError().setMessage("Error").setCause(throwable).log();
            }

            @Override
            public void onCompleted() {
                log.atInfo().setMessage("Completed").log();
            }
        });

        final Input input1 = Input.newBuilder().setString("Request 1").build();
        log.atInfo().setMessage("Send request").addKeyValue("input", input1).log();
        input.onNext(input1);

        final Input input2 = Input.newBuilder().setString("Request 2").build();
        log.atInfo().setMessage("Send request").addKeyValue("input", input2).log();
        input.onNext(input2);

        final Input input3 = Input.newBuilder().setString("Request 3").build();
        log.atInfo().setMessage("Send request").addKeyValue("input", input3).log();
        input.onNext(input3);
    }
}
