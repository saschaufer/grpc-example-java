package de.saschaufer.grpc_example_java.server.endpoints;

import de.saschaufer.grpc_example_java.stubs.proto.client_streaming.ClientStreamingServiceGrpc;
import de.saschaufer.grpc_example_java.stubs.proto.client_streaming.Input;
import de.saschaufer.grpc_example_java.stubs.proto.client_streaming.Output;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClientStreamingService extends ClientStreamingServiceGrpc.ClientStreamingServiceImplBase {

    @Override
    public StreamObserver<Input> call(final StreamObserver<Output> responseObserver) {

        final StringBuilder sb = new StringBuilder();

        return new StreamObserver<>() {

            @Override
            public void onNext(final Input input) {
                log.atInfo().setMessage("Request received").addKeyValue("endpoint", "client streaming").addKeyValue("request", input).log();
                sb.append(input.getString());
            }

            @Override
            public void onError(final Throwable throwable) {
                throwable.printStackTrace();
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                log.atInfo().setMessage("Send response").log();
                responseObserver.onNext(Output.newBuilder().setString(sb.toString()).build());
                log.atInfo().setMessage("Sent response").log();
                log.atInfo().setMessage("Complete stream").log();
                responseObserver.onCompleted();
                log.atInfo().setMessage("Completed stream").log();
            }
        };
    }
}
