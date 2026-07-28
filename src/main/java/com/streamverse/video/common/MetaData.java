package com.streamverse.video.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetaData {

    private long totalElements;
    private int totalPages;

    public MetaData(final int totalPages, final long totalElements) {
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}
