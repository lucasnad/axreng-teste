package com.axreng.backend;

import com.axreng.backend.api.CrawlAPI;
import com.axreng.backend.services.CrawlService;

public class Main {
    public static void main(String[] args) {
        new CrawlAPI(new CrawlService()).init();
    }
}
