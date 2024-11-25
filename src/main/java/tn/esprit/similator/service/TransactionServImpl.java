package tn.esprit.similator.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.similator.entity.*;
import tn.esprit.similator.repository.TransactionRepo;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class TransactionServImpl implements ITransactionService {
    TransactionRepo transactionRepo;


    // Méthode pour créer une nouvelle transaction
    public Transaction createTransaction(Transaction transaction) {
        transaction.processTransaction();
        transaction.setTotalAmount(transaction.getPrice() * transaction.getQuantity());// Traite la transaction avant de l'enregistrer
        return transactionRepo.save(transaction);
    }
    // Méthode pour récupérer toutes les transactions
    public List<Transaction> getAllTransactions() {
        return transactionRepo.findAll();
    }

    // Méthode pour récupérer une transaction par ID
    public Transaction getTransactionById(Long id) {
        return transactionRepo.findById(id).orElse(null);
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
            return transactionRepo.save(transaction);
        }
        return null;
    }

    // Méthode pour supprimer une transaction
    public void deleteTransaction(Long id) {
        transactionRepo.deleteById(id);
    }
    public List<Transaction> retrieveAllTransactions() {
        return transactionRepo.findAll();
    }
    public Transaction retrieveTransaction(Long transactionId) {
        return transactionRepo.findById(transactionId).get();
    }
    public List<Transaction> getTransactionsByPortfolioId(Long portfolioId) {
        return transactionRepo.findByPortfolioId(portfolioId);
    }
    public void removeTransaction(Long transId) {
        transactionRepo.deleteById(transId);
    }
    public Transaction modifyTransaction(Transaction transaction) {
        return transactionRepo.save(transaction);
    }




    /*
    public Transaction addTransaction(Long placingOrderId, Transaction transaction) {
        PlacingOrder placingOrder = placingOrderRepo.findById(placingOrderId).get();
        if (placingOrder.getStatus() == status.OPEN) {
            transaction.setPlacingOrder(placingOrder);
            Transaction savedTransaction = transactionRepo.save(transaction);

            // Now update or create the holding
            Portfolio portfolio = placingOrder.getPortfolio();
            Holding holding = holdingRepo.findBySymbolAndPortfolio(transaction.getSymbol(), portfolio);

            if (placingOrder.getActionType() == actionType.BUY) {
                if (holding == null) {
                    // Create a new holding if it doesn't exist
                    holding = new Holding();
                    holding.setSymbol(transaction.getSymbol());
                    holding.setQty(transaction.getQuantity());
                    holding.setAvgPrice(transaction.getPrice());
                    holding.setAcquisitionDate(new Date());
                    holding.setPortfolio(portfolio);
                } else {
                    // Update existing holding
                    Double newQty = holding.getQty() + transaction.getQuantity();
                    Float newAvgPrice = (float) (((holding.getQty() * holding.getAvgPrice()) +
                                                (transaction.getQuantity() * transaction.getPrice())) / newQty);

                    holding.setQty(newQty);
                    holding.setAvgPrice(newAvgPrice);
                }
            } else if (placingOrder.getActionType() == actionType.SELL) {
                if (holding != null) {
                    Double newQty = holding.getQty() - transaction.getQuantity();

                    if (newQty <= 0) {
                        // Remove the holding if quantity becomes zero or less
                        holdingRepo.delete(holding);
                    } else {
                        holding.setQty(newQty);
                    }
                } else {
                    throw new RuntimeException("Cannot sell. No holdings for this symbol in the portfolio.");
                }
            }
            holdingRepo.save(holding);
            return savedTransaction;
        } else {
            throw new RuntimeException("Cannot add transaction. The placing order status is not OPEN.");
        }
    }
    public void removeTransaction(Long transactionId) {
        transactionRepo.deleteById(transactionId);
    }
    public Transaction modifyTransaction(Transaction transaction) {
        return transactionRepo.save(transaction);
    }

     */
}
