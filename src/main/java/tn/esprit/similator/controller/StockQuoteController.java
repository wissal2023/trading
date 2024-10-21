package tn.esprit.similator.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.similator.service.StockQuoteService;

import java.util.Map;

@RestController
@RequestMapping("/API/Quote")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class StockQuoteController {

    private final StockQuoteService stockQuoteService;
    @GetMapping("/search") //search if Market open or closed
    public Map<String, Object> searchStockSymbols(@RequestParam String keywords) {
        return stockQuoteService.searchStockSymbols(keywords);
    }
    @GetMapping("/StockQuote/{symbol}") //search symbol in the order form
    public Map getStockQuote(@PathVariable String symbol) {
        return stockQuoteService.getStockQuote(symbol);
    }




/*

    @GetMapping("/get/stocks")
    public String getStockData(@RequestParam String symbol, @RequestParam String interval) {
        return stockService.getStockData(symbol, interval);
    }

 */


}
