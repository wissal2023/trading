package tn.esprit.similator.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.similator.entity.*;
import tn.esprit.similator.repository.HoldingRepo;
import tn.esprit.similator.repository.PlacingOrderRepo;
import tn.esprit.similator.repository.TransactionRepo;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class TransactionServImpl implements ITransactionService {

    TransactionRepo transactionRepo;
    PlacingOrderRepo placingOrderRepo;
    HoldingRepo holdingRepo;

    public List<Transaction> retrieveAllTransactions() {
        return transactionRepo.findAll();
    }
    public Transaction retrieveTransaction(Long transactionId) {
        return transactionRepo.findById(transactionId).get();
    }

    @Override
    public void removeTransaction(Long transId) {

    }

    @Override
    public Transaction modifyTransaction(Transaction transaction) {
        return null;
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
