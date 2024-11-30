package tn.esprit.pif.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AlphaVantageService {

    @Value("${alphavantage.api.key}")
    private String apiKey = "2JRL4MNXP4NW5L3Q";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode getEarningsCalendar() {
        String url = "https://www.alphavantage.co/query?function=EARNINGS_CALENDAR&apikey=" + apiKey;
        try {
            String response = restTemplate.getForObject(url, String.class);
            return objectMapper.readTree(response); // Convertit la réponse en JsonNode
        } catch (HttpClientErrorException e) {
            // Gérer les erreurs HTTP ici
            System.out.println("Erreur lors de l'appel à l'API : " + e.getResponseBodyAsString());
            return null;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
