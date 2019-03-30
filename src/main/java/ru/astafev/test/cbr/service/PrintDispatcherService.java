package ru.astafev.test.cbr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import ru.astafev.test.cbr.domain.DocType;
import ru.astafev.test.cbr.domain.Document;
import ru.astafev.test.cbr.domain.InputStreamProvider;
import ru.astafev.test.cbr.domain.JobState;
import ru.astafev.test.cbr.repository.DocumentRepository;
import ru.astafev.test.cbr.service.events.GeneralDocumentLifecycleEvent;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrintDispatcherService implements PrintDispatcher, ApplicationListener<GeneralDocumentLifecycleEvent> {
    private final BlockingQueue<Document> queue = new LinkedBlockingQueue<>();
    private final DocumentRepository documentRepository;
    private final PrintService printService;

    @PostConstruct
    public void startPrintService() {
        printService.start(queue);
    }

    public List<Document> stopAll() {
        List<Document> drain = new ArrayList<>(queue.size());
        queue.drainTo(drain);
        for (Document doc : drain) {
            this.cancel(doc);
        }
        return drain;
    }

    public Document submit(String title, DocType docType,
                           InputStreamProvider inputStreamProvider,
                           Map<String, String> additionalProperties) throws InterruptedException {
        Document job = Document.submitANewDocument(title, docType,
                inputStreamProvider,
                additionalProperties);
        documentRepository.save(job);
        queue.put(job);
        return job;
    }

    @Override
    public Document submit(String title, DocType docType, byte[] content) throws InterruptedException {
        return submit(title, docType, new Document.ByteArrayInputStreamProvider(content), null);
    }

    public void cancel(Document doc) {
        doc.cancel();
        documentRepository.update(doc);
    }

    public List<Document> getPrintedDocuments(String field, String direction) {
        return documentRepository.getPrintedDocs(field, direction);
    }

    public double getAveragePrintingDuration() {
        List<Document> printedDocuments = getPrintedDocuments(null, null);
        long total = 0;
        for (Document doc : printedDocuments) {
            total += doc.getPrintDuration();
        }
        return (double) total / printedDocuments.size();
    }


    @Override
    public void onApplicationEvent(GeneralDocumentLifecycleEvent event) {
        try {
            Document document = event.getDocument();
            documentRepository.update(document);
        } catch (RuntimeException e) {
            log.error("error processing printed for document: {}", event.getDocument(), e);
            throw e;
        }
    }
}
