package ru.astafev.test.cbr.repository;

import ru.astafev.test.cbr.domain.Document;

import java.util.List;

public interface DocumentRepository {
    Document findOne(Long id);

    void save(Document job);

    void update(Document job);

    List<Document> getAll();

    List<Document> getPrintedDocs(String field, String direction);
}
