package de.saschaufer.grpc_example.endpoints;

import de.saschaufer.grpc_example.proto.bidirectional_streaming.BidirectionalStreamingServiceGrpc;
import de.saschaufer.grpc_example.proto.bidirectional_streaming.Input;
import de.saschaufer.grpc_example.proto.bidirectional_streaming.Output;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BidirectionalStreamingService extends BidirectionalStreamingServiceGrpc.BidirectionalStreamingServiceImplBase {

    @Override
    public StreamObserver<Input> call(final StreamObserver<Output> outputObserver) {
        return new StreamObserver<>() {

            @Override
            public void onNext(final Input input) {

                log.atInfo().setMessage("Request received").addKeyValue("endpoint", "bidirectional streaming").addKeyValue("request", input).log();

                final String string = input.getString();

                final StringBuilder sb = new StringBuilder();
                for (char c : string.toCharArray()) {
                    if (Character.isUpperCase(c)) {
                        sb.append(Character.toLowerCase(c));
                    } else {
                        sb.append(Character.toUpperCase(c));
                    }
                }

                log.atInfo().setMessage("Send response").log();
                outputObserver.onNext(Output.newBuilder().setString(sb.toString()).build());
                log.atInfo().setMessage("Sent response").log();
            }

            @Override
            public void onError(final Throwable throwable) {
                outputObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                log.atInfo().setMessage("Complete stream").log();
                outputObserver.onCompleted();
                log.atInfo().setMessage("Completed stream").log();
            }
        };
    }
}
