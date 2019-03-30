package ru.astafev.test.cbr.repository;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.WriteResult;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.stereotype.Repository;
import ru.astafev.test.cbr.domain.Document;
import ru.astafev.test.cbr.domain.JobState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Repository
public class DocumentRepositoryImpl implements DocumentRepository {
    private final ObjectRepository<Document> repo;

    public DocumentRepositoryImpl(Nitrite db) {
        this.repo = db.getRepository(Document.class);
    }

    @Override
    public Document findOne(Long id) {
        Document obj = repo.getById(NitriteId.createId(id));
        log.debug("found the following obj {} for id", obj, id);
        return obj;
    }

    @Override
    public void save(Document job) {
        job.setId(NitriteId.newId().getIdValue());
        WriteResult insert = repo.insert(job);
        log.debug("inserted a new document: {}, with result: {}", job, insert);
    }

    @Override
    public void update(Document job) {
        WriteResult insert = repo.update(job);
        log.debug("updating document: {}, with result: {}", job, insert);
    }

    @Override
    public List<Document> getAll() {
        return cursorToList(repo.find());
    }

    @Override
    public List<Document> getPrintedDocs(String field, String direction) {
        ObjectFilter filter = ObjectFilters.eq("jobState", JobState.DONE);
        Cursor<Document> cursor;
        if (field != null) {
            SortOrder sortOrder;
            switch (direction) {
                case "DESC":
                case "desc":
                    sortOrder = SortOrder.Descending;
                    break;
                case "ASC":
                case "asc":
                    sortOrder = SortOrder.Ascending;
                    break;
                default:
                    log.warn("unknown direction literal: {}", direction);
                    sortOrder = SortOrder.Ascending;
            }

            cursor = repo.find(filter, new FindOptions(field, sortOrder));
        } else {
            cursor = repo.find(filter);
        }
        return cursorToList(cursor);
    }

    private List<Document> cursorToList(Cursor<Document> cursor) {
        Iterator<Document> iterator = cursor.iterator();
        List<Document> result = new ArrayList<>(cursor.totalCount());
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }
}
