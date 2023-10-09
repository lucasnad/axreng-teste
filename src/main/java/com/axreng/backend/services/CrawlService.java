package com.axreng.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CrawlService {
    private static final Logger logger = LoggerFactory.getLogger(CrawlService.class);

    public List<String> crawlForTerm(String keyword) {
        List<String> foundUrls = new ArrayList<>();
        String baseUrl = System.getenv("BASE_URL");

        try {
            URL url = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            if (content.toString().toLowerCase().contains(keyword.toLowerCase())) {
                foundUrls.add(baseUrl);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return foundUrls;
    }
}
