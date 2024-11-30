package tn.esprit.pif.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pif.entity.Transaction;
import tn.esprit.pif.repository.TransactionRepository;

import java.util.List;
@Service
@AllArgsConstructor
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    // Méthode pour créer une nouvelle transaction
    public Transaction createTransaction(Transaction transaction) {
        transaction.processTransaction();
        transaction.setTotalAmount(transaction.getPrice() * transaction.getQuantity());// Traite la transaction avant de l'enregistrer
        return transactionRepository.save(transaction);
    }
    // Méthode pour récupérer toutes les transactions
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    // Méthode pour récupérer une transaction par ID
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    // Méthode pour mettre à jour une transaction
    public Transaction updateTransaction(Long id, Transaction transactionDetails) {
        Transaction transaction = getTransactionById(id);
        if (transaction != null) {
            transaction.setPrice(transactionDetails.getPrice());
            transaction.setCurrency(transactionDetails.getCurrency());
            transaction.setQuantity(transactionDetails.getQuantity());
            transaction.setType(transactionDetails.getType());
            transaction.setStatus(transactionDetails.getStatus());
            transaction.setTransactionDate(transactionDetails.getTransactionDate());
            transaction.processTransaction(); // Traiter la transaction avant de la sauvegarder
            return transactionRepository.save(transaction);
        }
        return null;
    }

    // Méthode pour supprimer une transaction
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}
