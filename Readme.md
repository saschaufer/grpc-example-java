# gRPC Examples

Example gRPC services for Spring Boot.
The modules implement the server and client side as well as gRPC stubs.
The server includes services for the four basic concepts ...

| Type                    | Structure                       |
|-------------------------|---------------------------------|
| Unary                   | simple request, simple response |
| Server Streaming        | simple request, stream response |
| Client Streaming        | stream request, simple response |
| Bidirectional Streaming | stream request, stream response |

... and services for uploading and downloading files as well as a service which requires authentication.

The server includes unit tests for each service.
The client implements the client site of the four basic concepts.

# Tutorials

Here are some helpful sources to read:

- gRPC: https://grpc.io/
- Spring gRPC: https://docs.spring.io/spring-grpc/reference/
- Spring Boot gRPC Examples: https://github.com/spring-projects/spring-grpc/tree/main/samples
- Medium: https://medium.com/@dinesharney/building-a-simple-grpc-client-and-server-with-spring-boot-3-4672c1e4fab7
- File Upload: https://blog.vinsguru.com/grpc-file-upload-client-streaming/
- Protobuf: https://protobuf.dev/

# CURLs

Here are some helpful CURLs to test the services.
The requests are sent via grpcurl: https://snapcraft.io/install/grpcurl/ubuntu

## List Services

```bash
grpcurl -plaintext localhost:9090 list
```

## Unary

```bash
grpcurl -d '{"number": 2, "string":"abc"}' -plaintext localhost:9090 de.saschaufer.grpc_example_java.stubs.proto.unary.UnaryService.call
```

## Server Streaming

```bash
grpcurl -d '{}' -plaintext localhost:9090 de.saschaufer.grpc_example_java.stubs.proto.server_streaming.ServerStreamingService.call
```

## Client Streaming

```bash
grpcurl -d '{"string":"a"}{"string":"b"}{"string":"c"}' -plaintext localhost:9090 de.saschaufer.grpc_example_java.stubs.proto.client_streaming.ClientStreamingService.call
```

## Bidirectional Streaming

```bash
grpcurl -d '{"string":"ABc"}{"string":"dEf"}{"string":"ghI"}' -plaintext localhost:9090 de.saschaufer.grpc_example_java.stubs.proto.bidirectional_streaming.BidirectionalStreamingService.call
```

## Auth Service

```bash
grpcurl -H 'Authorization: 1234' -d '{"string":"World"}' -plaintext localhost:9090 de.saschaufer.grpc_example_java.stubs.proto.auth.AuthService.call
```
