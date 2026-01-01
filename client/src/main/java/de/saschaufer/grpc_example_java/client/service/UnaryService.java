package de.saschaufer.grpc_example_java.client.service;

import de.saschaufer.grpc_example_java.client.config.AppProperties;
import de.saschaufer.grpc_example_java.stubs.proto.unary.Input;
import de.saschaufer.grpc_example_java.stubs.proto.unary.Output;
import de.saschaufer.grpc_example_java.stubs.proto.unary.UnaryServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UnaryService extends GrpcService {

    private final UnaryServiceGrpc.UnaryServiceBlockingStub stub;

    public UnaryService(final AppProperties appProperties, final GrpcChannelFactory channelFactory) {
        super(appProperties, channelFactory);
        stub = UnaryServiceGrpc.newBlockingStub(getChannel());
    }

    @Override
    public void run() {

        final Input input = Input.newBuilder()
                .setNumber(1)
                .setString("Hello World")
                .build();

        log.atInfo().setMessage("Send request").addKeyValue("input", input).log();

        final Output output = stub.call(input);

        log.atInfo().setMessage("Received response").addKeyValue("output", output).log();
    }
}
