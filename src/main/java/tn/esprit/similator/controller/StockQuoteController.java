package tn.esprit.similator.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.similator.entity.FinancialNews;
import tn.esprit.similator.service.StockQuoteService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/API")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class StockQuoteController {

    private final StockQuoteService stockQuoteService;

    //get the stock data
    @GetMapping("/GLOBAL_QUOTE/{symbol}") //search symbol in the order form
    public Map getStockQuote(@PathVariable String symbol) {
        return stockQuoteService.getStockQuote(symbol);
    }

    // Get Options data
    @GetMapping("/HISTORICAL_OPTIONS/{symbol}")
    public Map getHistoricalOptions(@PathVariable String symbol,
                                    @RequestParam(required = false) String date) {
        return stockQuoteService.getHistoricalOptions(symbol, date);
    }

    // Get oil prices commodities
    @GetMapping("/Oil_Commodities")
    public Map getBrentCrudePrices(@RequestParam(required = false) String interval) {
        Set<String> allowedIntervals = Set.of("daily", "weekly", "monthly");
        String validatedInterval = allowedIntervals.contains(interval) ? interval : "monthly";
        return stockQuoteService.getBrentCrudePrices(validatedInterval);
    }

    @GetMapping("/MARKET_STATUS")//search if Market open or closed
    public Map getMarketStatus() {
        return stockQuoteService.getMarketStatus();
    }

    @GetMapping("/SYMBOL_SEARCH")
    public Map searchStockSymbols(@RequestParam String keywords) {
        return stockQuoteService.searchStockSymbols(keywords);
    }

    @GetMapping("/dailyTimeSeries")
    public Map<String, Object> getDailyTimeSeries(@RequestParam String symbol) {
        return stockQuoteService.getDailyTimeSeries(symbol);
    }

    @GetMapping("/financial-news/{ticker}")
    public List<FinancialNews> getFinancialNews(@PathVariable String ticker,
                                                @RequestParam(defaultValue = "10") int limit,
                                                @RequestParam(defaultValue = "0") int offset) {
        try {
            return stockQuoteService.getFinancialNews(ticker, limit, offset);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching financial news", e);
        }
    }
/*

    @GetMapping("/get/stocks")
    public String getStockData(@RequestParam String symbol, @RequestParam String interval) {
        return stockService.getStockData(symbol, interval);
    }

 */


}
