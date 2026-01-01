package de.saschaufer.grpc_example_java.client.service;

import de.saschaufer.grpc_example_java.client.config.AppProperties;
import io.grpc.Channel;
import lombok.Getter;
import org.springframework.grpc.client.GrpcChannelFactory;

@Getter
public abstract class GrpcService {

    private final Channel channel;

    protected GrpcService(final AppProperties appProperties, final GrpcChannelFactory channelFactory) {
        channel = channelFactory.createChannel(appProperties.uri());
    }

    public abstract void run();
}
