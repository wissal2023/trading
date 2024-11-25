package tn.esprit.similator.service;

import tn.esprit.similator.entity.Transaction;

import java.util.List;

public interface ITransactionService {
    public List<Transaction> retrieveAllTransactions();
    public Transaction retrieveTransaction(Long transId);
    //public Transaction addTransaction(Long placingOrderId, Transaction transaction);
    public List<Transaction> getTransactionsByPortfolioId(Long portfolioId);
    public void removeTransaction(Long transId);
    public Transaction modifyTransaction(Transaction transaction);
}
