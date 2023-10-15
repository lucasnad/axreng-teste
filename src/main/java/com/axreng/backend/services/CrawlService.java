package com.axreng.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlService {
    private static final Logger logger = LoggerFactory.getLogger(CrawlService.class);
    private static final String LINK_REGEX = "href=\"([^\"]*)\"|href='([^']*)'";
    private static final Pattern LINK_PATTERN = Pattern.compile(LINK_REGEX, Pattern.CASE_INSENSITIVE);

    public List<String> crawlForTerm(String keyword) {
        logger.info("Starting crawlForTerm with keyword: {}", keyword);
        Set<String> visitedUrls = new HashSet<>();

        // Obter a URL base do ambiente ou usar um valor padrão
        String baseUrl = getBaseUrl();

        // Inicializar a lista que armazenará as URLs encontradas que contêm a palavra-chave
        List<String> foundUrls = new ArrayList<>();

        Set<String> queue = new HashSet<>();
        queue.add(baseUrl);

        while (!queue.isEmpty()) {
            String currentUrl = queue.iterator().next();
            queue.remove(currentUrl);

            if (visitedUrls.contains(currentUrl)) {
                continue;
            }

            logger.info("Visiting URL: {}", currentUrl);
            visitedUrls.add(currentUrl);

            logger.info("Fetching content from URL: {}", currentUrl);
            String content = fetchContent(currentUrl);
            if(content == null){
                continue;
            }

            logger.info("Fetched content length: {}", content.length());

            // Verificar se o conteúdo contém a palavra-chave; se sim, adicionar à lista de URLs encontradas
            if (content.toLowerCase().contains(keyword.toLowerCase())) {
                logger.info("Keyword found. Adding URL: {}", currentUrl);
                foundUrls.add(currentUrl);
            }

            // Extrair todos os links da página atual sem duplicatas
            Set<String> links = extractLinks(content);

            for (String link : links) {
                if (!visitedUrls.contains(link) && !queue.contains(link)) {
                    queue.add(link);
                }
            }
        }

        logger.info("Finished crawlForTerm with keyword: {}", keyword);
        // Retornar a lista de URLs encontradas que contêm a palavra-chave
        return foundUrls;
    }


    private String getBaseUrl() {
        String baseUrl = System.getenv("BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://hiring.axreng.com/";
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
            //logger.error("Failed to fetch content from: " + url, e);
            return null;
        }
    }

    private Set<String> extractLinks(String content) {
        Set<String> links = new HashSet<>();
        Matcher matcher = LINK_PATTERN.matcher(content);
        String baseUrl = getBaseUrl();
        while (matcher.find()) {
            String link = matcher.group(1);
            link = normalizeLink(baseUrl, link);
            if(link.startsWith(baseUrl)){
                links.add(link);
            }
        }
        return links;
    }

    public String normalizeLink(String baseUrl, String link) {
        if (link.startsWith("/")) {
            return baseUrl + link;
        } else if (link.startsWith("http") || link.startsWith("www")) {
            return link;
        } else if (link.startsWith("../")) {
            return baseUrl + link.replace("..", "");
        } else {
            return baseUrl + "/" + link;
        }
    }
}
