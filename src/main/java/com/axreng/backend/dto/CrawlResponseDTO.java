package com.axreng.backend.dto;

import com.axreng.backend.enums.SearchStatus;
import com.google.gson.annotations.JsonAdapter;

import java.util.List;

public class CrawlResponseDTO {
    private final String id;

    private final SearchStatus status;
    private final List<String> urls;

    public CrawlResponseDTO(String id, SearchStatus status, List<String> urls) {
        this.id = id;
        this.status = status;
        this.urls = urls;
    }

    public String getId() {
        return id;
    }

    public SearchStatus getStatus() {
        return status;
    }

    public List<String> getUrls() {
        return urls;
    }
}
