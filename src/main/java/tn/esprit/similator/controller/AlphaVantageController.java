package tn.esprit.similator.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.similator.service.AlphaVantageService;

@RestController
@RequestMapping("/api")
public class AlphaVantageController {

    private final AlphaVantageService alphaVantageService;

    public AlphaVantageController(AlphaVantageService earningsCalendarService) {
        this.alphaVantageService = earningsCalendarService;
    }

    @GetMapping("/earnings-calendar")
    public JsonNode getEarningsCalendar() {
        return alphaVantageService.getEarningsCalendar(); // Retournez JsonNode directement
    }
}
