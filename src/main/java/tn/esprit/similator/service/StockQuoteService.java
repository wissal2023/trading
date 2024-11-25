package tn.esprit.similator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import tn.esprit.similator.entity.FinancialNews;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StockQuoteService {
    @Value("${alphavantage.api.key}")
    private String API_KEY;
    private static final String ALPHA_VANTAGE_URL = "https://www.alphavantage.co/query";

    @Value("${financial.news.api.token}")
    private String apiToken;

    private static final String EODHD_BASE_URL = "https://eodhd.com/api/news";

    //get stocks API
    public Map getStockQuote(String symbol) { // open , high , price
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(ALPHA_VANTAGE_URL)
                .queryParam("function", "GLOBAL_QUOTE")
                .queryParam("symbol", symbol)
                .queryParam("apikey", API_KEY)
                .queryParam("datatype", "json");

        return restTemplate.getForObject(uriBuilder.toUriString(), Map.class);
    }
    //get Options API
    public Map<String, Object> getHistoricalOptions(String symbol, String date) {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(ALPHA_VANTAGE_URL)
                .queryParam("function", "HISTORICAL_OPTIONS")
                .queryParam("symbol", symbol)
                .queryParam("apikey", API_KEY);

        if (date != null && !date.isEmpty()) {
            uriBuilder.queryParam("date", date);
        }

        uriBuilder.queryParam("datatype", "json");

        String url = uriBuilder.toUriString();
        log.info("Fetching historical options data from URL: {}", url);

        try {
            // Retrieve the data as a Map
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Error fetching historical options data", e);
            throw new RuntimeException("Failed to fetch historical options data");
        }
    }

    //get oil commodities API
    public Map getBrentCrudePrices(String interval) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(ALPHA_VANTAGE_URL)
                .queryParam("function", "BRENT")
                .queryParam("interval", interval != null ? interval : "monthly")
                .queryParam("apikey", API_KEY)
                .queryParam("datatype", "json");

        return restTemplate.getForObject(uriBuilder.toUriString(), Map.class);
    }




    //search if Market open or closed
    public Map searchStockSymbols(String keywords) { // symbol, open, closed
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(ALPHA_VANTAGE_URL)
                .queryParam("function", "SYMBOL_SEARCH")
                .queryParam("keywords", keywords)
                .queryParam("apikey", API_KEY)
                .queryParam("datatype", "json");

        return restTemplate.getForObject(uriBuilder.toUriString(), Map.class);
    }
    public Map getMarketStatus() {// global Mkt status transition in angular
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(ALPHA_VANTAGE_URL)
                .queryParam("function", "MARKET_STATUS")
                .queryParam("apikey", API_KEY)
                .queryParam("datatype", "json");
        return restTemplate.getForObject(uriBuilder.toUriString(), Map.class);
    }


    public Double getLatestPrice(String symbol) {
        Map<String, Object> stockQuote = getStockQuote(symbol);
        return Double.valueOf(((Map<String, String>) stockQuote.get("Global Quote")).get("05. price"));
    }


    // TIME_SERIES_DAILY for the chart
    public Map<String, Object> getDailyTimeSeries(String symbol) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(ALPHA_VANTAGE_URL)
                .queryParam("function", "TIME_SERIES_DAILY")
                .queryParam("symbol", symbol)
                .queryParam("apikey", API_KEY)
                .queryParam("datatype", "json");

        return restTemplate.getForObject(uriBuilder.toUriString(), Map.class);
    }

    // Fetches financial news for a specific ticker
    public List<FinancialNews> getFinancialNews(String ticker, int limit, int offset) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(EODHD_BASE_URL)
                .queryParam("s", ticker)
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .queryParam("api_token", apiToken)
                .queryParam("fmt", "json");

        try {
            // Change Map.class to List<FinancialNews> class
            ResponseEntity<List<FinancialNews>> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<FinancialNews>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error while fetching financial news from EODHD API", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching news", e);
        }
    }

/*
    public String getStockData(String symbol, String interval) {
        String url = String.format("%s&symbol=%s&interval=%s&apikey=%s", INTRADAY, symbol, interval, API_KEY);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        return response.getBody();
    }

    // Fetch real-time price for a given asset symbol
    public double getRealTimePrice(String symbol) throws JSONException {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL.replace("{symbol}", symbol).replace("{apikey}", API_KEY);
        // Make a request to the API and get the response as a string
        String response = restTemplate.getForObject(url, String.class);
        // Parse the response to extract the real-time price
        JSONObject jsonResponse = new JSONObject(response);
        JSONObject globalQuote = jsonResponse.getJSONObject("Global Quote");
        // Get the price from the "05. price" field
        double price = globalQuote.getDouble("05. price");
        return price;
    }
*/





}
