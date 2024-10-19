package tn.esprit.similator.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class StockQuoteService {

    @Value("${alphavantage.api.key}")
    private String API_KEY;
    private static final String BASE_URL = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol={symbol}&apikey={apikey}";
    private final String INTRADAY = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY";


    public Map<String, Object> getStockQuote(String symbol) {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL.replace("{symbol}", symbol).replace("{apikey}", API_KEY);

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        return response.getBody();
    }

    public String getStockData(String symbol, String interval) {
        String url = String.format("%s&symbol=%s&interval=%s&apikey=%s", INTRADAY, symbol, interval, API_KEY);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        return response.getBody();
    }

    // Fetch real-time price for a given asset symbol
   /* public double getRealTimePrice(String symbol) throws JSONException {
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
