package com.axreng.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlService {
    private static final Logger logger = LoggerFactory.getLogger(CrawlService.class);
    private static final String LINK_REGEX = "href=[\"']([^\"']+)[\"']";
    private static final Pattern LINK_PATTERN = Pattern.compile(LINK_REGEX, Pattern.CASE_INSENSITIVE);

    private Set<String> visitedUrls = new HashSet<>();

    public List<String> crawlForTerm(String keyword) {
        String baseUrl = getBaseUrl();
        List<String> foundUrls = new ArrayList<>();
        searchDFS(baseUrl, keyword, foundUrls);
        return foundUrls;
    }

    private void searchDFS(String currentUrl, String keyword, List<String> foundUrls) {
        if (visitedUrls.contains(currentUrl)) {
            return;
        }
        visitedUrls.add(currentUrl);

        String content = fetchContent(currentUrl);
        if (content.toLowerCase().contains(keyword.toLowerCase())) {
            foundUrls.add(currentUrl);
        }

        List<String> links = extractLinks(content, currentUrl);
        for (String link : links) {
            searchDFS(link, keyword, foundUrls);
        }
    }

    private String getBaseUrl() {
        String baseUrl = System.getenv("BASE_URL");

        if(baseUrl == null){
            baseUrl = "http://hiring.axreng.com/";
        }

        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalStateException("BASE_URL environment variable is not set");
        }

        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String fetchContent(String url) {
        try {
            URL targetUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                return content.toString();
            }
        } catch (Exception e) {
            logger.error("Failed to fetch content from: " + url, e);
            return ""; // retornando conteúdo vazio em caso de erro
        }
    }

    /**
     *
     * @param content
     * @param baseUrl
     * @return
     */
    private List<String> extractLinks(String content, String baseUrl) {
        List<String> links = new ArrayList<>();
        Matcher matcher = LINK_PATTERN.matcher(content);
        while (matcher.find()) {
            String link = matcher.group(1);
            if (!link.startsWith("http") && !link.startsWith(baseUrl)) {
                link = baseUrl + (link.startsWith("/") ? "" : "/") + link;
            }
            if (link.startsWith(baseUrl)) {
                links.add(link);
            }
        }
        return links;
    }
}
