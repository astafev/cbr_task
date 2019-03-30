package ru.astafev.test.cbr.domain;

import lombok.Getter;

@Getter
public enum DocType {
    FAST(50, PageSize.A5),
    DOC_TYPE_1(500, PageSize.A4),
    DOC_TYPE_2(1_000, PageSize.A4),
    DOC_TYPE_3(5_000, PageSize.A3);


    /**
     * продолжительность печати в мс... смысл этого числа мне не очень понятен на данный момент
     */
    private final int printTime;
    private final PageSize pageSize;

    DocType(int printTime, PageSize pageSize) {
        this.printTime = printTime;
        this.pageSize = pageSize;
    }
}
