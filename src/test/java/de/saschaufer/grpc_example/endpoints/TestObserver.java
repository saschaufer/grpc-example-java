package de.saschaufer.grpc_example.endpoints;

import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Getter
public class TestObserver<T> implements StreamObserver<T> {

    private final CountDownLatch latch;
    private final boolean collectResults;

    private final List<T> results = new ArrayList<>();
    private Throwable error = null;

    public TestObserver(final int latchCount, final boolean collectResults) {
        this.latch = new CountDownLatch(latchCount);
        this.collectResults = collectResults;
    }

    @Override
    public void onNext(final T value) {
        log.atInfo().setMessage("Response received").log();
        if (collectResults) {
            results.add(value);
        }
    }

    @Override
    public void onError(final Throwable t) {
        error = t;
    }

    @Override
    public void onCompleted() {
        latch.countDown();
        log.atInfo().setMessage("Stream closed").log();
    }
}
