package tn.esprit.pif.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import tn.esprit.pif.entity.Challenge;
import tn.esprit.pif.entity.Transaction;
import tn.esprit.pif.repository.ChallengeRepository;
import tn.esprit.pif.repository.TransactionRepository;
import tn.esprit.pif.service.TransactionService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // Endpoint pour créer une nouvelle transaction


    @PostMapping("/{challengeId}/transactions")
    public ResponseEntity<?> createTransaction(
            @PathVariable Long challengeId,
            @RequestBody Transaction transaction) {

        // Vérifiez si le challenge existe avec cet ID
        Optional<Challenge> existingChallenge = challengeRepository.findById(challengeId);
        if (existingChallenge.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Vérifiez si la transaction concerne une cryptomonnaie
        if (transaction.getCurrency() != null && isCryptoCurrency(transaction.getCurrency())) {
            // Appel à l'API CoinGecko pour obtenir le prix actuel de la cryptomonnaie
            Double currentPrice = getCurrentCryptoPrice(transaction.getCurrency());
            if (currentPrice != null) {
                transaction.setPrice(currentPrice); // Mettre à jour le prix avec celui de l'API
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération du prix de la cryptomonnaie.");
            }
        }

        // Assigner le challenge à la transaction
        transaction.setChallenge(existingChallenge.get());

        // Enregistrer la transaction
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Retournez une réponse avec la transaction sauvegardée
        return ResponseEntity.ok(savedTransaction);
    }

    // Fonction pour vérifier si une monnaie est une cryptomonnaie
    private boolean isCryptoCurrency(String currency) {
        List<String> cryptoCurrencies = Arrays.asList("bitcoin", "ethereum", "litecoin", "dogecoin"); // Ajouter d'autres cryptos si nécessaire
        return cryptoCurrencies.contains(currency.toLowerCase());
    }

    // Fonction pour récupérer le prix actuel d'une cryptomonnaie depuis l'API CoinGecko
    private Double getCurrentCryptoPrice(String cryptoId) {
        String apiUrl = "https://api.coingecko.com/api/v3/simple/price?ids=" + cryptoId + "&vs_currencies=usd";
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(apiUrl, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> priceMap = (Map<String, Object>) response.getBody().get(cryptoId);
                return (Double) priceMap.get("usd");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Gérer l'erreur correctement dans un environnement de production
        }
        return null;
    }

    // Endpoint pour récupérer toutes les transactions
    @GetMapping()
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    // Endpoint pour récupérer une transaction par ID
    @GetMapping("/get-transaction/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionService.getTransactionById(id);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transaction);
    }

    // Endpoint pour mettre à jour une transaction
    @PutMapping("/update-transaction/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction transactionDetails) {
        Transaction updatedTransaction = transactionService.updateTransaction(id, transactionDetails);
        if (updatedTransaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedTransaction);
    }

    // Endpoint pour supprimer une transaction
    @DeleteMapping("/delete-transaction/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
