package de.saschaufer.grpc_example_java.server.endpoints;

import com.google.protobuf.ByteString;
import de.saschaufer.grpc_example_java.server.config.AppProperties;
import de.saschaufer.grpc_example_java.stubs.proto.file_upload.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.grpc.test.autoconfigure.LocalGrpcPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@DirtiesContext
@Import(FileUploadServiceTest.TestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {FileUploadService.class})
class FileUploadServiceTest {

    @TempDir
    private static Path tempDir;

    @Autowired
    private FileUploadServiceGrpc.FileUploadServiceStub stub;

    @TestConfiguration
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        AppProperties appProperties() {
            return new AppProperties("1234", tempDir);
        }

        @Bean
        @Lazy
        FileUploadServiceGrpc.FileUploadServiceStub stub(final GrpcChannelFactory channels, @LocalGrpcPort final int port) {
            return FileUploadServiceGrpc.newStub(channels.createChannel("0.0.0.0:" + port));
        }
    }

    @Test
    void upload() throws IOException, InterruptedException {

        final TestObserver<FileUploadResponse> testObserver = new TestObserver<>(1, false);

        final Path testfileSource = tempDir.resolve("testfile-source.txt");
        final Path testfileTarget = tempDir.resolve("testfile-target.txt");
        final String filenameTarget = "testfile-target";
        final String extensionTarget = "txt";

        createLargeFile(testfileSource);

        final long startTime = System.currentTimeMillis();
        final StreamObserver<FileUploadRequest> streamObserver = stub.upload(testObserver);

        // Send metadata
        final FileUploadRequest metadata = FileUploadRequest.newBuilder()
                .setMetadata(Metadata.newBuilder()
                        .setName(filenameTarget)
                        .setType(extensionTarget).build())
                .build();
        streamObserver.onNext(metadata);

        // Send bytes
        try (final InputStream inputStream = Files.newInputStream(testfileSource)) {
            final byte[] bytes = new byte[4096];
            int size;
            while ((size = inputStream.read(bytes)) > 0) {
                final FileUploadRequest uploadRequest = FileUploadRequest.newBuilder()
                        .setFile(File.newBuilder().setContent(ByteString.copyFrom(bytes, 0, size)).build())
                        .build();
                streamObserver.onNext(uploadRequest);
            }
        }
        streamObserver.onCompleted();

        testObserver.getLatch().await(10, TimeUnit.SECONDS);

        final long endTime = System.currentTimeMillis();
        System.out.println("Time taken to upload file: " + (endTime - startTime) + " ms");

        assertThat(testObserver.getError(), nullValue());
        assertThat(Files.mismatch(testfileSource, testfileTarget), is(-1L));
    }

    private void createLargeFile(final Path path) throws IOException {
        final RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");
        raf.setLength(1024 * 1024 * 1024); // Creates a 1 GB file
        raf.close();
    }
}
