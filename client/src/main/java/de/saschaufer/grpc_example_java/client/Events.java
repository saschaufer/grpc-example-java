package de.saschaufer.grpc_example_java.client;

import de.saschaufer.grpc_example_java.client.service.BidirectionalStreamingService;
import de.saschaufer.grpc_example_java.client.service.ClientStreamingService;
import de.saschaufer.grpc_example_java.client.service.ServerStreamingService;
import de.saschaufer.grpc_example_java.client.service.UnaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Events {

    private final UnaryService unaryService;
    private final ServerStreamingService serverStreamingService;
    private final ClientStreamingService clientStreamingService;
    private final BidirectionalStreamingService bidirectionalStreamingService;

    @EventListener
    void handleApplicationReadyEvent(final ApplicationReadyEvent event) {

        unaryService.run();
        serverStreamingService.run();
        clientStreamingService.run();
        bidirectionalStreamingService.run();
    }
}
