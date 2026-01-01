package de.saschaufer.grpc_example_java.client.service;

import de.saschaufer.grpc_example_java.client.config.AppProperties;
import de.saschaufer.grpc_example_java.stubs.proto.server_streaming.Input;
import de.saschaufer.grpc_example_java.stubs.proto.server_streaming.Output;
import de.saschaufer.grpc_example_java.stubs.proto.server_streaming.ServerStreamingServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Service;

import java.util.Iterator;

@Slf4j
@Service
public class ServerStreamingService extends GrpcService {

    private final ServerStreamingServiceGrpc.ServerStreamingServiceBlockingStub stub;

    public ServerStreamingService(final AppProperties appProperties, final GrpcChannelFactory channelFactory) {
        super(appProperties, channelFactory);
        stub = ServerStreamingServiceGrpc.newBlockingStub(getChannel());
    }

    @Override
    public void run() {

        final Input input = Input.newBuilder().build();

        log.atInfo().setMessage("Send request").addKeyValue("input", input).log();

        final Iterator<Output> output = stub.call(input);

        while (output.hasNext()) {
            log.atInfo().setMessage("Received response").addKeyValue("output", output.next()).log();
        }
    }
}
