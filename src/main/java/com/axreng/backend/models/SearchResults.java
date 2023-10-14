package com.axreng.backend.models;

import java.util.List;

public class SearchResults {
    private Long id;
    private List<String> urls;

    public SearchResults(Long id, List<String> urls) {
        this.id = id;
        this.urls = urls;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

}
