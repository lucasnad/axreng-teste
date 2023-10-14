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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CrawlAPI {

    //usando uma estrutura thread-safe para evitar problemas de concorrência.
    private static final Map<String, List<String>> searchResults = new ConcurrentHashMap<>();
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

            //validating if the 'keyword' field is between 4 and 32 characters
            if (keyword == null || keyword.length() < 4 || keyword.length() > 32) {
                res.status(400);
                return new Gson().toJson(new ApiResponse(400, "field 'keyword' is required (from 4 up to 32 chars)"));
            }

            String id = IdGenerator.generateRandomId();
            List<String> urls = crawlService.crawlForTerm(keyword);
            searchResults.put(id, urls);

            return new Gson().toJson(new SearchResultDTO(id));
        });

        get("/crawl/:id", (req, res) -> {
            String id = req.params(":id");
            List<String> urls = searchResults.get(id);

            if (urls == null) {
                res.status(404);
                return "crawl not found: " + id;
            }

            CrawlResponseDTO response = new CrawlResponseDTO(id, SearchStatus.ACTIVE, urls);
            return new Gson().toJson(response);
        });
    }

}
