package ru.astafev.test.cbr.service;

import ru.astafev.test.cbr.domain.DocType;
import ru.astafev.test.cbr.domain.Document;
import ru.astafev.test.cbr.domain.InputStreamProvider;

import java.util.List;
import java.util.Map;

public interface PrintDispatcher {

    /**
     * Остановка диспетчера. Печать документов в очереди отменяется.
     * На выходе должен быть список ненапечатанных документов.
     */
    List<Document> stopAll();

    /**
     * Принять документ на печать. Метод не должен блокировать выполнение программы.
     */
    Document submit(String title, DocType docType,
                    InputStreamProvider inputStreamProvider,
                    Map<String, String> additionalProperties) throws InterruptedException;

    /**
     * see {@link #submit(String, DocType, InputStreamProvider, Map)}
     */
    Document submit(String title, DocType docType,
                    byte[] content) throws InterruptedException;


    /**
     * Отменить печать принятого документа, если он еще не был напечатан.
     */
    void cancel(Document doc);


    /**
     * Получить отсортированный список напечатанных документов. Список может быть отсортирован на выбор: по порядку
     * печати, по типу документов, по продолжительности печати, по размеру бумаги.
     */
    List<Document> getPrintedDocuments(String field, String direction);

    /**
     * Рассчитать среднюю продолжительность печати напечатанных
     */
    double getAveragePrintingDuration();

}
