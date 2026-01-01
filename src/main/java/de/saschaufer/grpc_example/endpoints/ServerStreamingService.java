package de.saschaufer.grpc_example.endpoints;

import de.saschaufer.grpc_example.proto.server_streaming.Input;
import de.saschaufer.grpc_example.proto.server_streaming.Output;
import de.saschaufer.grpc_example.proto.server_streaming.ServerStreamingServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ServerStreamingService extends ServerStreamingServiceGrpc.ServerStreamingServiceImplBase {

    @Override
    public void call(final Input request, final StreamObserver<Output> responseObserver) {

        log.atInfo().setMessage("Request received").addKeyValue("endpoint", "server streaming").addKeyValue("request", request).log();

        for (int i = 0; i < 10; i++) {
            final Output output = Output.newBuilder()
                    .setNumber(i + 1)
                    .build();
            log.atInfo().setMessage("Send response").log();
            responseObserver.onNext(output);
            log.atInfo().setMessage("Sent response").log();
        }

        log.atInfo().setMessage("Complete stream").log();
        responseObserver.onCompleted();
        log.atInfo().setMessage("Completed stream").log();
    }
}
