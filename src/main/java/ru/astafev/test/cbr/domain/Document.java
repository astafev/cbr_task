package ru.astafev.test.cbr.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data()
@Indices({
        @Index(value = "jobState", type = IndexType.NonUnique),
        @Index(value = "docType", type = IndexType.NonUnique),
        @Index(value = "submittedAt", type = IndexType.NonUnique),
        @Index(value = "startedProcessingAt", type = IndexType.NonUnique),
        @Index(value = "finishedAt", type = IndexType.NonUnique),
})
@ToString(exclude = "inputStreamProvider")
@EqualsAndHashCode(of = "id")
public class Document implements Serializable {
    @Id
    private Long id;

    private String title;
    private DocType docType;

    private JobState jobState;

    private Date submittedAt;
    private Date startedProcessingAt;
    private Date finishedAt;
    private Map<String, String> additionalProperties;

    // we don't want to share getter on that object
    @Getter(AccessLevel.PRIVATE)
    private transient InputStreamProvider inputStreamProvider;

    public InputStream getInputStream() {
        return inputStreamProvider.getInputStream();
    }


    public synchronized JobState getJobState() {
        return jobState;
    }

    public synchronized void setJobState(JobState jobState) {
        this.jobState = jobState;
    }

    public static Document submitANewDocument(String title, DocType docType, InputStreamProvider inputStreamProvider,
                                              Map<String, String> additionalProperties) {
        Document document = new Document();
        document.setTitle(title);
        document.setDocType(docType);
        document.setInputStreamProvider(inputStreamProvider);
        document.setSubmittedAt(new Date());
        document.setAdditionalProperties(additionalProperties);
        document.setJobState(JobState.SUBMITTED);
        return document;
    }

    public void printed() {
        if (getJobState() != JobState.PROCESSING) {
            throw new IllegalStateException("seems like an error in the lifecycle, the job is " + getJobState() +
                    "and can't be marked ss Done now");
        }
        setJobState(JobState.DONE);
        setFinishedAt(new Date());
    }

    public void startProcessing() {
        if (getJobState() != JobState.SUBMITTED) {
            throw new IllegalStateException("the job can't be processed, it's already in state " + getJobState());
        }
        setJobState(JobState.PROCESSING);
        setStartedProcessingAt(new Date());
    }

    public void cancel() {
        if (getJobState() == JobState.CANCELLED) {
            // ignore
            return;
        }
        if (getJobState() != JobState.SUBMITTED) {
            throw new IllegalStateException("it's too late, the job is already " + getJobState().name());
        }
        setJobState(JobState.CANCELLED);
        setFinishedAt(new Date());
    }


    @RequiredArgsConstructor
    public static class ByteArrayInputStreamProvider implements InputStreamProvider {
        private final byte[] content;

        public ByteArrayInputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }
    }

    public long getPrintDuration() {
        if (finishedAt != null && startedProcessingAt != null) {
            return finishedAt.getTime() - startedProcessingAt.getTime();
        } else {
            throw new IllegalStateException("not finished yet " + this.toString());
        }
    }

}
