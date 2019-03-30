package ru.astafev.test.cbr.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.astafev.test.cbr.domain.DocType;
import ru.astafev.test.cbr.domain.Document;
import ru.astafev.test.cbr.domain.JobState;
import ru.astafev.test.cbr.repository.DocumentRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration test for {@link PrintDispatcher}
 * <p>
 * The test uses waits, so it can take some time to execute.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PrintDispatcherServiceTest {
    @Autowired
    private PrintDispatcher printDispatcher;
    @Autowired
    private DocumentRepository repository;

    private List<Document> dbBeforeTheTest;

    @Before
    public void setUp() {
        dbBeforeTheTest = repository.getAll();
    }

    @After
    public void tearDown() throws InterruptedException {
        printDispatcher.stopAll();
        Thread.sleep(DocType.DOC_TYPE_1.getPrintTime() + DocType.FAST.getPrintTime());
    }

    @Test
    public void testSubmit() throws Exception {
        Document submitted = printDispatcher.submit("test submit", DocType.DOC_TYPE_1, new byte[10]);
        assertEquals(submitted.getJobState(), JobState.SUBMITTED);
        List<Document> all = repository.getAll();
        assertTrue(all.toString(), all.contains(submitted));
    }

    @Test
    public void testSubmitAndWaitForPrint() throws Exception {
        Document submitted = printDispatcher.submit("test 1", DocType.FAST, new byte[10]);
        assertEquals(submitted.getJobState(), JobState.SUBMITTED);
        Thread.sleep(submitted.getDocType().getPrintTime() * 2);
        assertEquals(submitted.getJobState(), JobState.DONE);
    }

    @Test
    public void testGetPrintedDocs() throws Exception {
        final Document submitted = printDispatcher.submit("test 1", DocType.FAST, new byte[10]);
        checkFor(submitted.getDocType().getPrintTime() * 10, new Runnable() {
            @Override
            public void run() {
                List<Document> printedDocs = repository.getPrintedDocs(null, null);
                assertTrue(printedDocs.toString(), printedDocs.contains(submitted));
            }
        });
    }

    @Test
    public void testGetAveragePrintingDuration() throws Exception {
        final int FAST_DOCS = 10;

        for (int i = 0; i < FAST_DOCS; i++) {
            printDispatcher.submit("testGetAveragePrintingDuration fast " + i, DocType.FAST,
                    new byte[10]);
        }
        printDispatcher.submit("testGetAveragePrintingDuration doc type 1 ", DocType.DOC_TYPE_1, new byte[10]);

        final AtomicLong existingDuration = new AtomicLong();
        for (Document printedAlready : dbBeforeTheTest) {
            try {
                existingDuration.addAndGet(printedAlready.getPrintDuration());
            } catch (IllegalStateException e) {
                // db is in corrupted state, ignore, other tests should detect it
            }
        }
        int expectedAdded = DocType.FAST.getPrintTime() * FAST_DOCS + DocType.DOC_TYPE_1.getPrintTime();
        final long expectedTotal = existingDuration.get() + expectedAdded;

        checkFor(
                expectedAdded * 10, new Runnable() {
                    @Override
                    public void run() {

                        double averagePrintingDuration = printDispatcher.getAveragePrintingDuration();
                        // the real time will be a bit higher, let's add 5 ms and check with some margin
                        double expectedAverage = (double) expectedTotal / (dbBeforeTheTest.size() + FAST_DOCS + 1) + 5;
                        assertEquals(
                                "existing: " + existingDuration.get() + " for " +
                                        dbBeforeTheTest.size() + " documents.",
                                expectedAverage, averagePrintingDuration,
                                10.0);
                    }
                }
        );
    }

    @Test
    public void testStopAll() throws Exception {
        printDispatcher.submit("testStopAll 1", DocType.FAST, new byte[10]);
        Document doc1 = printDispatcher.submit("testStopAll 2", DocType.FAST, new byte[10]);
        Document doc2 = printDispatcher.submit("testStopAll 3", DocType.FAST, new byte[10]);

        printDispatcher.stopAll();

        assertEquals(doc1.toString(), doc1.getJobState(), JobState.CANCELLED);
        assertEquals(doc2.toString(), doc1.getJobState(), JobState.CANCELLED);
    }

    @Test
    public void testCancel() throws Exception {
        printDispatcher.submit("testCancel 1", DocType.FAST, new byte[10]);
        Document doc1 = printDispatcher.submit("testCancel 2", DocType.FAST, new byte[10]);

        printDispatcher.cancel(doc1);

        assertEquals(doc1.toString(), doc1.getJobState(), JobState.CANCELLED);
    }

    @Test(expected = IllegalStateException.class)
    public void testInProcessCanNotBeCancelled() throws Exception {
        Document doc = printDispatcher.submit("testCancel 1", DocType.FAST, new byte[10]);
        Thread.sleep(DocType.FAST.getPrintTime() / 2);

        printDispatcher.cancel(doc);
    }

    @Test
    public void testGetPrintedWithSorting() throws Exception {
        Document docA = printDispatcher.submit("testGetPrintedWithSorting A", DocType.FAST, new byte[10]);
        Document docC = printDispatcher.submit("testGetPrintedWithSorting C", DocType.FAST, new byte[10]);
        Document docB = printDispatcher.submit("testGetPrintedWithSorting B", DocType.FAST, new byte[10]);

        Thread.sleep(DocType.FAST.getPrintTime() / 4);

        List<Document> printedDocuments = printDispatcher.getPrintedDocuments("title", "asc");
        // check that the order is expected
        int idx = 0;
        for (Document doc : printedDocuments) {
            if (doc.equals(docA)) {
                assertEquals("found doc A at idx " + idx, 0, idx++);
            }
            if (doc.equals(docB)) {
                assertEquals("found doc B at idx " + idx, 1, idx++);
            }
            if (doc.equals(docC)) {
                assertEquals("found doc C at idx " + idx, 2, idx++);
            }
        }

        printedDocuments = printDispatcher.getPrintedDocuments("title", "desc");
        // check that the order is expected
        idx = 0;
        for (Document doc : printedDocuments) {
            if (doc.equals(docA)) {
                assertEquals("found doc A at idx " + idx, 2, idx++);
            }
            if (doc.equals(docB)) {
                assertEquals("found doc B at idx " + idx, 1, idx++);
            }
            if (doc.equals(docC)) {
                assertEquals("found doc C at idx " + idx, 0, idx++);
            }
        }

    }


    /**
     * @param runnable function that performs checks and throws AssertionError if the checks didn't pass
     */
    static void checkFor(int time, Runnable runnable) {
        for (int i = 0; i < 9; i++) {
            try {
                Thread.sleep(time / 10);
                runnable.run();
                return;
            } catch (AssertionError e) {
                // ignore for now
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        runnable.run();
    }


}