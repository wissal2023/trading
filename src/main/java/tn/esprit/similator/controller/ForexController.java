package tn.esprit.similator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pif.service.ForexScrapingService;
@RestController
@RequestMapping("/api/forex")
public class ForexController {
    private final ForexScrapingService scrapingService;

    public ForexController(ForexScrapingService scrapingService) {
        this.scrapingService = scrapingService;
    }

    @GetMapping("/stats")
    public String getForexStats() {
        return scrapingService.scrapeForexData();
    }
}
