package com.axreng.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlService {
    private static final Logger logger = LoggerFactory.getLogger(CrawlService.class);
    private static final String LINK_REGEX = "href=[\"']([^\"']+)[\"']";
    private static final Pattern LINK_PATTERN = Pattern.compile(LINK_REGEX, Pattern.CASE_INSENSITIVE);

    public List<String> crawlForTerm(String keyword) {
        String baseUrl = getBaseUrl();
        String content = fetchContent(baseUrl);
        return extractLinks(content, baseUrl, keyword.toLowerCase());
    }

    private String getBaseUrl() {
        String baseUrl = System.getenv("BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalStateException("BASE_URL environment variable is not set");
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String fetchContent(String baseUrl) {
        try {
            URL url = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                return content.toString();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Failed to fetch content from: " + baseUrl, e);
        }
    }

    private List<String> extractLinks(String content, String baseUrl, String keyword) {
        List<String> foundUrls = new ArrayList<>();
        Matcher matcher = LINK_PATTERN.matcher(content);
        while (matcher.find()) {
            String link = matcher.group(1);
            if (!link.startsWith("http") && !link.startsWith(baseUrl)) {
                link = baseUrl + (link.startsWith("/") ? "" : "/") + link;
            }
            if (link.startsWith(baseUrl) && content.contains(keyword)) {
                foundUrls.add(link);
            }
        }
        return foundUrls;
    }

}
