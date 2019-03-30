package ru.astafev.test.cbr.service;

import ru.astafev.test.cbr.domain.Document;

import java.util.concurrent.BlockingQueue;

public interface PrintService {
    void start(BlockingQueue<Document> printJobs);

    void stop();
}
