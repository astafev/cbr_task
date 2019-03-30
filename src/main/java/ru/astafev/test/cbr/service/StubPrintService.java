package ru.astafev.test.cbr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.astafev.test.cbr.domain.Document;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class StubPrintService extends AbstractExecutorPrintService {

    public StubPrintService(ApplicationEventPublisher applicationEventPublisher) {
        super(applicationEventPublisher);
    }

    @Override
    protected void print(Document doc) throws IOException, InterruptedException {
        int size = 0;
        try (InputStream is = doc.getInputStream()) {
            while (is.read() != -1) {
                size++;
            }
        }
        log.info("printing {} doc of size {} bytes", doc.getTitle(), size);
        Thread.sleep(doc.getDocType().getPrintTime());
    }
}
