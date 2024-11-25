package tn.esprit.similator.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import tn.esprit.pif.entity.Challenge;
import tn.esprit.pif.repository.ChallengeRepository;
import tn.esprit.pif.repository.TransactionRepository;
import tn.esprit.similator.entity.Transaction;
import tn.esprit.similator.service.ITransactionService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Transaction class")
@RestController
@AllArgsConstructor
@RequestMapping("/transaction")
@CrossOrigin(origins = "*")
public class TransactionController {

    ITransactionService transactionServ;
    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // Endpoint pour créer une nouvelle transaction


    @PostMapping("/{challengeId}/transactions")
    public ResponseEntity<?> createTransaction(
            @PathVariable Long challengeId,
            @RequestBody tn.esprit.pif.entity.Transaction transaction) {

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
        tn.esprit.pif.entity.Transaction savedTransaction = transactionRepository.save(transaction);

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
    public List<tn.esprit.pif.entity.Transaction> getAllTransactions() {
        return transactionServ.getAllTransactions();
    }

    // Endpoint pour récupérer une transaction par ID
    @GetMapping("/get-transaction/{id}")
    public ResponseEntity<tn.esprit.pif.entity.Transaction> getTransactionById(@PathVariable Long id) {
        tn.esprit.pif.entity.Transaction transaction = transactionServ.getTransactionById(id);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transaction);
    }

    // Endpoint pour mettre à jour une transaction
    @PutMapping("/update-transaction/{id}")
    public ResponseEntity<tn.esprit.pif.entity.Transaction> updateTransaction(@PathVariable Long id, @RequestBody tn.esprit.pif.entity.Transaction transactionDetails) {
        tn.esprit.pif.entity.Transaction updatedTransaction = transactionServ.updateTransaction(id, transactionDetails);
        if (updatedTransaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedTransaction);
    }

    // Endpoint pour supprimer une transaction
    @DeleteMapping("/delete-transaction/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionServ.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/Get-all-transactions")
    public List<Transaction> getTransactions() {
        return transactionServ.retrieveAllTransactions();
    }
    
    @GetMapping("/Get-transaction/{transaction-id}")
    public Transaction retrieveTransaction(@PathVariable("transaction-id") Long transactionId) {
        return transactionServ.retrieveTransaction(transactionId);

    }
    @GetMapping("/Get-transactions-by-portfolio/{portfolioId}")
    public List<Transaction> getTransactionsByPortfolio(@PathVariable Long portfolioId) {
        return transactionServ.getTransactionsByPortfolioId(portfolioId);
    }

/*
    @PostMapping("/AddTransaction/{placingOrderId}")
    public ResponseEntity<Transaction> addTransaction(@PathVariable Long placingOrderId,
                                                      @RequestBody Transaction transaction) {

        Transaction createdTransaction = transactionServ.addTransaction(placingOrderId, transaction);
        return ResponseEntity.ok(createdTransaction);
    }

 */

    @PutMapping("/modify-transaction")
    public Transaction modifyTransaction(@RequestBody Transaction asst) {
        return transactionServ.modifyTransaction(asst);
    }

    @DeleteMapping("/remove-transaction/{transaction-id}")
    public void removeTransaction(@PathVariable("transaction-id") Long transactionId) {
        transactionServ.removeTransaction(transactionId);
    }


}
