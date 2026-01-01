package de.saschaufer.grpc_example_java.server.endpoints;

import de.saschaufer.grpc_example_java.stubs.proto.auth.AuthServiceGrpc;
import de.saschaufer.grpc_example_java.stubs.proto.auth.Input;
import de.saschaufer.grpc_example_java.stubs.proto.auth.Output;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService extends AuthServiceGrpc.AuthServiceImplBase {

    @Override
    public void call(final Input request, final StreamObserver<Output> responseObserver) {
        responseObserver.onNext(Output.newBuilder().setString("Hello " + request.getString()).build());
        responseObserver.onCompleted();
    }
}
