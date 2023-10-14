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

    public List<String> crawlForTerm(String keyword) {
        List<String> foundUrls = new ArrayList<>();
        String baseUrl = System.getenv("BASE_URL");

        //TODO remover esse bloco antes de enviar;
        if(baseUrl == null){
            baseUrl = "http://hiring.axreng.com/";
        }

        if(baseUrl == null || baseUrl.isEmpty()){
            throw new IllegalStateException("BASE_URL environment variable is not set");
        }

        keyword = keyword.toLowerCase(); // Convertendo a palavra-chave para lowercase

        try {
            URL url = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();

            // Verificando se o conteúdo contém a palavra-chave (case insensitive)
            if (content.toString().toLowerCase().contains(keyword)) {
                foundUrls.add(baseUrl);
            }

            // Usando uma expressão regular simples para extrair links
            Pattern pattern = Pattern.compile("href=\"(.*?)\"");
            Matcher matcher = pattern.matcher(content.toString());
            while (matcher.find()) {
                String link = matcher.group(1);
                if (!link.startsWith("http")) {
                    link = baseUrl + link; // Convertendo link relativo para absoluto
                }
                if (link.startsWith(baseUrl)) {
                    foundUrls.add(link);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return foundUrls;
    }


}
