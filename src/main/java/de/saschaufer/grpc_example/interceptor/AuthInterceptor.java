package de.saschaufer.grpc_example.interceptor;

import de.saschaufer.grpc_example.config.AppProperties;
import de.saschaufer.grpc_example.proto.auth.AuthServiceGrpc;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor implements ServerInterceptor {

    private final AppProperties appProperties;

    public static final Metadata.Key<String> AUTHORIZATION = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <I, O> ServerCall.Listener<I> interceptCall(final ServerCall<I, O> serverCall, final Metadata metadata, final ServerCallHandler<I, O> serverCallHandler) {

        final String fullMethodName = serverCall.getMethodDescriptor().getFullMethodName();
        final String serviceName = MethodDescriptor.extractFullServiceName(fullMethodName);

        log.atInfo().setMessage("AuthInterceptor called")
                .addKeyValue("method", fullMethodName)
                .addKeyValue("service", serviceName)
                .log();

        if (!serviceName.equals(AuthServiceGrpc.SERVICE_NAME)) {
            return serverCallHandler.startCall(serverCall, metadata);
        }

        final String secret = metadata.get(AUTHORIZATION);

        final Status status = secret != null && secret.equals(appProperties.password()) ? Status.OK : Status.UNAUTHENTICATED.withDescription("Password wrong");

        if (status.isOk()) {
            return Contexts.interceptCall(Context.current(), serverCall, metadata, serverCallHandler);
        }

        serverCall.close(status, metadata);
        return new ServerCall.Listener<I>() {
        };
    }
}
