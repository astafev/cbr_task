package ru.astafev.test.cbr.service.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.astafev.test.cbr.domain.Document;

public class GeneralDocumentLifecycleEvent extends ApplicationEvent {
    @Getter
    private final Document document;

    public GeneralDocumentLifecycleEvent(Object source, Document document) {
        super(source);
        this.document = document;
    }
}
