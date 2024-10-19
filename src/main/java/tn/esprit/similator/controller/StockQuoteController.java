package tn.esprit.similator.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.similator.service.StockQuoteService;

import java.util.Map;

@RestController
@RequestMapping("/API/Quote")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class StockQuoteController {

    private final StockQuoteService stockQuoteService;
    //private final TIME_SERIES_INTRADAY stockService;

    @GetMapping("/get/{symbol}")
    public Map<String, Object> getStockQuote(@PathVariable String symbol) {
        return stockQuoteService.getStockQuote(symbol);
    }
/*
    @GetMapping("/get/stocks")
    public String getStockData(@RequestParam String symbol, @RequestParam String interval) {
        return stockService.getStockData(symbol, interval);
    }

 */


}
