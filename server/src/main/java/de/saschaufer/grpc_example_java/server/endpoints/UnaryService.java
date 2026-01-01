package de.saschaufer.grpc_example_java.server.endpoints;

import de.saschaufer.grpc_example_java.stubs.proto.unary.Input;
import de.saschaufer.grpc_example_java.stubs.proto.unary.Output;
import de.saschaufer.grpc_example_java.stubs.proto.unary.UnaryServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UnaryService extends UnaryServiceGrpc.UnaryServiceImplBase {

    @Override
    public void call(final Input request, final StreamObserver<Output> responseObserver) {

        log.atInfo().setMessage("Request received").addKeyValue("endpoint", "unary").addKeyValue("request", request).log();

        final long integer = request.getNumber() * 2L;
        final String string = new StringBuilder(request.getString()).reverse().toString();

        final Output output = Output.newBuilder()
                .setNumber(integer)
                .setString(string)
                .build();

        log.atInfo().setMessage("Send response").log();
        responseObserver.onNext(output);
        log.atInfo().setMessage("Sent response").log();
        log.atInfo().setMessage("Complete stream").log();
        responseObserver.onCompleted();
        log.atInfo().setMessage("Completed stream").log();
    }
}
