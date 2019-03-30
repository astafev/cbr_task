package ru.astafev.test.cbr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import ru.astafev.test.cbr.domain.Document;
import ru.astafev.test.cbr.domain.JobState;
import ru.astafev.test.cbr.service.events.DocumentIsBeingPrintedEvent;
import ru.astafev.test.cbr.service.events.DocumentPrintedEvent;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractExecutorPrintService implements PrintService {
    private final ApplicationEventPublisher applicationEventPublisher;
    protected ExecutorService executors = Executors.newSingleThreadExecutor();

    protected abstract void print(Document doc) throws IOException, InterruptedException;

    public void start(final BlockingQueue<Document> printQueue) {
        executors.submit(new Runner(printQueue));
    }


    public void stop() {
        executors.shutdown();
    }

    @RequiredArgsConstructor
    private class Runner implements Runnable {
        private final BlockingQueue<Document> printQueue;

        @Override
        public void run() {
            while (true) {
                try {
                    Document doc = printQueue.take();
                    if (doc.getJobState() == JobState.CANCELLED) {
                        continue;
                    }
                    doc.startProcessing();
                    applicationEventPublisher.publishEvent(
                            new DocumentIsBeingPrintedEvent(AbstractExecutorPrintService.this, doc));

                    print(doc);
                    log.info("printed {} doc!", doc.getTitle());

                    doc.printed();
                    applicationEventPublisher.publishEvent(
                            new DocumentPrintedEvent(AbstractExecutorPrintService.this, doc));
                } catch (InterruptedException | IOException e) {
                    log.error("error occured", e);
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
