package com.axreng.backend.services;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlService {
    private static final Logger logger = LoggerFactory.getLogger(CrawlService.class);
    private static final String LINK_REGEX = "href=\"([^\"]*)\"|href='([^']*)'";
    private static final Pattern LINK_PATTERN = Pattern.compile(LINK_REGEX, Pattern.CASE_INSENSITIVE);
    public void crawlForTerm(String keyword, List<String> urls) {
        Set<String> visitedUrls = new HashSet<>();
        String baseUrl = getBaseUrl();
        Set<String> queue = new LinkedHashSet<>();  // Mantém a ordem de inserção
        queue.add(baseUrl);

        String keywordLower = keyword.toLowerCase();
        while (!queue.isEmpty()) {
            Iterator<String> iterator = queue.iterator();
            String currentUrl = iterator.next();
            iterator.remove();  // Remoção eficiente do primeiro elemento

            // Adição eficiente e verificação em uma única chamada
            if (!visitedUrls.add(currentUrl)) {
                continue;
            }

            //logger.info("Fetching content from URL: {}", currentUrl);
            String content = fetchContent(currentUrl);
            if(content == null){
                continue;
            }

            //logger.info("Fetched content length: {}", content.length());

            // Verificar se o conteúdo contém a palavra-chave; se sim, adicionar à lista de URLs encontradas
            if (content.toLowerCase().contains(keywordLower)) {
                //logger.info("Keyword found. Adding URL: {}", currentUrl);
                urls.add(currentUrl);  // Adicione o URL à lista em tempo real
            }

            // Extrair todos os links da página atual sem duplicatas
            Set<String> links = extractLinks(content);

            for (String link : links) {
                if (!visitedUrls.contains(link)) {
                    queue.add(link);
                }
            }
        }

        logger.info("Finished crawlForTerm with keyword: {}", keyword);
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
