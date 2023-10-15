package com.axreng.backend;

import com.axreng.backend.api.CrawlAPI;
import com.axreng.backend.enums.SearchStatus;
import com.axreng.backend.services.CrawlService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

public class Main {
    public static void main(String[] args) {
        Gson gson = new GsonBuilder().
                registerTypeAdapter(SearchStatus.class, (JsonSerializer<SearchStatus>) (src, typeOfSrc, context) -> new JsonPrimitive(src.name().toLowerCase()))
                .create();
        new CrawlAPI(new CrawlService(), gson).init();
    }
}
