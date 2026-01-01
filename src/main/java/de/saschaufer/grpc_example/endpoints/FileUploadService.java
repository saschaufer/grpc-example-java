package de.saschaufer.grpc_example.endpoints;

import com.google.protobuf.ByteString;
import de.saschaufer.grpc_example.config.AppProperties;
import de.saschaufer.grpc_example.proto.file_upload.FileUploadRequest;
import de.saschaufer.grpc_example.proto.file_upload.FileUploadResponse;
import de.saschaufer.grpc_example.proto.file_upload.FileUploadServiceGrpc;
import de.saschaufer.grpc_example.proto.file_upload.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService extends FileUploadServiceGrpc.FileUploadServiceImplBase {

    private final AppProperties appProperties;

    @Override
    public StreamObserver<FileUploadRequest> upload(final StreamObserver<FileUploadResponse> responseObserver) {

        return new StreamObserver<>() {

            OutputStream writer;
            Status status = Status.IN_PROGRESS;

            @Override
            public void onNext(final FileUploadRequest fileUploadRequest) {
                try {
                    if (fileUploadRequest.hasMetadata()) {
                        writer = getFilePath(fileUploadRequest);
                    } else {
                        writeFile(writer, fileUploadRequest.getFile().getContent());
                    }
                } catch (final IOException e) {
                    this.onError(e);
                }
            }

            @Override
            public void onError(final Throwable throwable) {
                status = Status.FAILED;
                log.error("Error uploading file", throwable);
                this.onCompleted();
            }

            @Override
            public void onCompleted() {
                closeFile(writer);
                status = status.equals(Status.IN_PROGRESS) ? Status.SUCCESS : status;
                final FileUploadResponse response = FileUploadResponse.newBuilder()
                        .setStatus(status)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    private OutputStream getFilePath(final FileUploadRequest request) throws IOException {
        final String fileName = "%s.%s".formatted(request.getMetadata().getName(), request.getMetadata().getType());
        Files.createDirectories(appProperties.filesPath());
        if (Files.exists(appProperties.filesPath().resolve(fileName))) {
            throw new IOException("File already exists");
        }
        return Files.newOutputStream(appProperties.filesPath().resolve(fileName), StandardOpenOption.CREATE_NEW, StandardOpenOption.APPEND);
    }

    private void writeFile(final OutputStream writer, final ByteString content) throws IOException {
        writer.write(content.toByteArray());
        writer.flush();
    }

    private void closeFile(final OutputStream writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (final IOException e) {
                log.error("Error closing file", e);
            }
        }
    }
}
