package ru.astafev.test.cbr.service.events;

import ru.astafev.test.cbr.domain.Document;

public class DocumentPrintedEvent extends GeneralDocumentLifecycleEvent {
    public DocumentPrintedEvent(Object source, Document document) {
        super(source, document);
    }
}
