package de.saschaufer.grpc_example_java.server.endpoints;

import com.google.protobuf.ByteString;
import de.saschaufer.grpc_example_java.server.config.AppProperties;
import de.saschaufer.grpc_example_java.stubs.proto.file_download.File;
import de.saschaufer.grpc_example_java.stubs.proto.file_download.FileDownloadServiceGrpc;
import de.saschaufer.grpc_example_java.stubs.proto.file_download.Metadata;
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
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DirtiesContext
@Import(FileDownloadServiceTest.TestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {FileDownloadService.class})
class FileDownloadServiceTest {

    @TempDir
    private static Path tempDir;

    @Autowired
    private FileDownloadServiceGrpc.FileDownloadServiceStub stub;

    @TestConfiguration
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        AppProperties appProperties() {
            return new AppProperties("1234", tempDir);
        }

        @Bean
        @Lazy
        FileDownloadServiceGrpc.FileDownloadServiceStub stub(final GrpcChannelFactory channels, @LocalGrpcPort final int port) {
            return FileDownloadServiceGrpc.newStub(channels.createChannel("0.0.0.0:" + port));
        }
    }

    @Test
    void download() throws InterruptedException, IOException {

        final Path testfileSource = tempDir.resolve("testfile-source.txt");
        final Path testfileTarget = tempDir.resolve("testfile-target.txt");
        final String filenameSource = "testfile-source";
        final String extensionSource = "txt";

        createLargeFile(testfileSource);

        final Metadata metaData = Metadata.newBuilder().setName(filenameSource).setType(extensionSource).build();

        final CountDownLatch latch = new CountDownLatch(1);
        final long startTime = System.currentTimeMillis();

        stub.download(metaData, new StreamObserver<>() {

            OutputStream writer = null;

            @Override
            public void onNext(final File file) {
                try {
                    if (writer == null) {
                        writer = getFilePath(testfileTarget);
                    }
                    writeFile(writer, file.getContent());
                } catch (final IOException e) {
                    onError(e);
                }
            }

            @Override
            public void onError(final Throwable throwable) {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
                throwable.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (final IOException e) {
                        onError(e);
                    }
                }
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        final long endTime = System.currentTimeMillis();
        System.out.println("Time taken to download file: " + (endTime - startTime) + " ms");

        assertThat(Files.mismatch(testfileSource, testfileTarget), is(-1L));
    }

    private OutputStream getFilePath(final Path path) throws IOException {
        Files.createDirectories(path.getParent());
        if (Files.exists(path)) {
            throw new IOException("File already exists");
        }
        return Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.APPEND);
    }

    private void writeFile(final OutputStream writer, final ByteString content) throws IOException {
        writer.write(content.toByteArray());
        writer.flush();
    }

    private void createLargeFile(final Path path) throws IOException {
        final RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");
        raf.setLength(1024 * 1024 * 1024); // Creates a 1 GB file
        raf.close();
    }
}
