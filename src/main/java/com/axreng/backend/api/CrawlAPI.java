package com.axreng.backend.api;

import static spark.Spark.*;

import com.axreng.backend.dto.SearchResultDTO;
import com.axreng.backend.dto.CrawlResponseDTO;
import com.axreng.backend.enums.SearchStatus;
import com.axreng.backend.models.ApiResponse;
import com.axreng.backend.services.CrawlService;
import com.axreng.backend.utils.IdGenerator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrawlAPI {
    private static final Logger logger = LoggerFactory.getLogger(CrawlAPI.class);


    //Usando ConcurrentHashMap para evitar problemas de concorrência.
    private static final Map<String, List<String>> searchResults = new ConcurrentHashMap<>();
    private static final Map<String, SearchStatus> searchStatuses = new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newFixedThreadPool(10);

    private final CrawlService crawlService;

    public CrawlAPI(CrawlService crawlService) {
        this.crawlService = crawlService;
    }

    public void init() {

        post("/crawl", (req, res) -> {
            res.type("application/json");

            String body = req.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String keyword = jsonObject.get("keyword").getAsString();

            if (keyword == null || keyword.length() < 4 || keyword.length() > 32) {
                res.status(400);
                return new Gson().toJson(new ApiResponse(400, "field 'keyword' is required (from 4 up to 32 chars)"));
            }

            String id = IdGenerator.generateRandomId();
            searchStatuses.put(id, SearchStatus.ACTIVE);

            executor.submit(() -> {
                List<String> urls = crawlService.crawlForTerm(keyword);
                searchResults.put(id, new CopyOnWriteArrayList<>(urls));
                searchStatuses.put(id, SearchStatus.DONE);
                logger.info("End of crawl for id: " + id);
            });

            return new Gson().toJson(new SearchResultDTO(id));
        });

        get("/crawl/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            List<String> urls = searchResults.get(id);

            if (urls == null) {
                res.status(404);
                return new Gson().toJson(new ApiResponse(404, "crawl not found: " + id));
            }

            SearchStatus status = searchStatuses.getOrDefault(id, SearchStatus.ACTIVE);
            CrawlResponseDTO response = new CrawlResponseDTO(id, status, urls);
            return new Gson().toJson(response);
        });
    }
}
