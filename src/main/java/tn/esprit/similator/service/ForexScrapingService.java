package tn.esprit.similator.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
@RequestMapping("/api/forex")
@CrossOrigin(origins = "http://localhost:4200")
public class ForexScrapingService {
    public String scrapeForexData() {
        try {
            Document doc = Jsoup.connect("https://www.forex.com/en/market-analysis/latest-research/").get();
            // Par exemple, trouver les éléments avec une classe spécifique
            Element dataElement = doc.selectFirst(".some-class");
            return dataElement.text();  // Extrais et retourne les données intéressantes
        } catch (IOException e) {
            e.printStackTrace();
            return "Erreur lors du scraping des données";
        }
    }
}
