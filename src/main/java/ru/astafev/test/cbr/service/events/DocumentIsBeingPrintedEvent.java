package ru.astafev.test.cbr.service.events;

import ru.astafev.test.cbr.domain.Document;

public class DocumentIsBeingPrintedEvent extends GeneralDocumentLifecycleEvent {

    public DocumentIsBeingPrintedEvent(Object source, Document document) {
        super(source, document);
    }
}
